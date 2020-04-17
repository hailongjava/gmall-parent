package com.atguigu.gmall.gateway;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * 登录认证过滤器
 */

//@Order(0)
@Component
public class AuthLoginFilter implements GlobalFilter, Ordered {

    //URI解析实现类
    // URL :  https://passport.jd.com/new/login.aspx   域名 + 路径
    // URI : /new/login.aspx   路径
    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Autowired
    private RedisTemplate redisTemplate;

    //获取所有需要登录的路径
    @Value("${authUrls}")
    private String[] authUrls;

    //登录统一认证方法
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        //0:获取当前请求路径  /new/login.aspx
        String path = request.getURI().getPath();
        //1: 包含 /inner/ 不允许被路由    内部路径
        if (antPathMatcher.match("/**/inner/**", path)) {
            //不允许访问
            return out(response, ResultCodeEnum.PERMISSION);
        }
        //统一获取当前用户是否登录
        String userId = getUserId(request);

        //2: 包含 /auth 要求必须登录  以外其它URL路径 是不需要登录
        if (antPathMatcher.match("/**/auth/**", path)) {
            //判断没有登录
            if (StringUtils.isEmpty(userId)) {
                //不允许访问
                return out(response, ResultCodeEnum.LOGIN_AUTH);
            }
        }
        //3: 包含 /trade.html  pay.html myOrder.html  要求必须登录 其它页面可不登录
        //刷新页面  同步请求  未登录 处理方案就是重定向到登录页面去
        for (String url : authUrls) {
            //http://list.gmall.com/trade.html
            if(path.indexOf(url) != -1){
                //不登录  处理方案就是重定向到登录页面去
                if(StringUtils.isEmpty(userId)){
                    response.setStatusCode(HttpStatus.SEE_OTHER);//303
                    //去登录页面 URL
                    String loginUrl = null;
                    try {
                        loginUrl = "http://passport.gmall.com/login.html?originUrl=" +
                                URLEncoder.encode(request.getURI().getRawSchemeSpecificPart(),"utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    response.getHeaders().set(HttpHeaders.LOCATION,loginUrl);
                    return response.setComplete();
                }
            }
        }
        //请求来到网关  路径到后面的微服务  微服务想使用用户的ID
        //网关传递用户ID到后面的微服务
        if(!StringUtils.isEmpty(userId)){
            request.mutate().header("userId",userId);
        }
        //判断临时用户是否存在 传递临时用户给后面的微服务
        String userTempId = getUserTempId(request);
        if(!StringUtils.isEmpty(userTempId)){
            request.mutate().header("userTempId",userTempId);
        }
        //放行 允许进入路径 转发 微服务
        return chain.filter(exchange);
    }

    //统一返回值
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
        String jsonString = JSON.toJSONString(Result.build(null,resultCodeEnum));
        DataBufferFactory dataBufferFactory = response.bufferFactory();
        DataBuffer dataBuffer = dataBufferFactory.wrap(jsonString.getBytes());
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE,"application/json;charset=utf-8");
        return response.writeWith(Mono.just(dataBuffer));
    }

    //统一获取临时用户
    public String getUserTempId(ServerHttpRequest request){
        //1)获取请求头或Cookie中的令牌
        String userTempId = request.getHeaders().getFirst("userTempId");
        if (StringUtils.isEmpty(userTempId)) {
            //再从Cookie中获取
            HttpCookie httpCookie = request.getCookies().getFirst("userTempId");
            //K : V
            if (null != httpCookie) {
                userTempId = httpCookie.getValue();
            }
        }
       return userTempId;//UUID
    }

    //统一获取当前用户是否登录
    public String getUserId(ServerHttpRequest request) {
        //1)获取请求头或Cookie中的令牌
        String token = request.getHeaders().getFirst("token");
        if (StringUtils.isEmpty(token)) {
            //再从Cookie中获取
            HttpCookie httpCookie = request.getCookies().getFirst("token");
            //K : V
            if (null != httpCookie) {
                token = httpCookie.getValue();
            }
        }
        //从请求头或Cookke是否获取到令牌
        if (!StringUtils.isEmpty(token)) {
            //2)此令牌在缓存是否存在  、如果存在获取出来  如果不存在  null
            if (redisTemplate.hasKey("user:login:" + token)) {
                return (String) redisTemplate.opsForValue().get("user:login:" + token);
            }
        }
        return null;
    }


    //全局过滤器  执行顺序   默认 9大全局过滤器  + 1 自定义过滤器  == 10大全局过滤器
    // 负整数最大值 （最优先执行）   -1  0  1   正整数最大值（最后执行）
    // GatewayFilterChain 网关过滤器链  10大过滤器
    @Override
    public int getOrder() {
        return 0;
    }
}
