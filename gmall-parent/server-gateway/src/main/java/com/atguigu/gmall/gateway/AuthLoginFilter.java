package com.atguigu.gmall.gateway;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.result.Result;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 *  登录认证过滤器
 */

//@Order(0)
@Component
public class AuthLoginFilter implements GlobalFilter,Ordered {

    //URI解析实现类
    // URL :  https://passport.jd.com/new/login.aspx   域名 + 路径
    // URI : /new/login.aspx   路径
    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    //登录统一认证方法
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        //0:获取当前请求路径  /new/login.aspx
        String path = request.getURI().getPath();
        //1: 包含 /inner/ 不允许被路由    内部路径
        if(antPathMatcher.match("/inner",path)){
            //不允许访问
            return out(response);
        }
        //2: 包含 /auth 要求必须登录  以外其它URL路径 是不需要登录
        //3: 包含 /trade.html  pay.html myOrder.html  要求必须登录 其它页面可不登录
        //放行 允许进入路径 转发 微服务
        return chain.filter(exchange);
    }

    //统一返回值
    public Mono<Void> out(ServerHttpResponse response){
        String jsonString = JSON.toJSONString(Result.fail().message("此路径不允许访问"));
        DataBufferFactory dataBufferFactory = response.bufferFactory();
        DataBuffer dataBuffer = dataBufferFactory.wrap(jsonString.getBytes());
        return response.writeWith(Mono.just(dataBuffer));
    }

    //全局过滤器  执行顺序   默认 9大全局过滤器  + 1 自定义过滤器  == 10大全局过滤器
    // 负整数最大值 （最优先执行）   -1  0  1   正整数最大值（最后执行）
    // GatewayFilterChain 网关过滤器链  10大过滤器
    @Override
    public int getOrder() {
        return 0;
    }
}
