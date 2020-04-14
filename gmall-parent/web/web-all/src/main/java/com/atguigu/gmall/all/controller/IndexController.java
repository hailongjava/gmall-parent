package com.atguigu.gmall.all.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * 首页管理
 */
@Controller
public class IndexController {

    //远程调用商品微服务
    @Autowired
    private ProductFeignClient productFeignClient;

    //初始化首页  hosts中配置 www.gmall.com
//    @GetMapping("/")
//    public String index(Model model){
//        List<Map> listMap = productFeignClient.getBaseCategoryList();
//        model.addAttribute("list", listMap);
//        return "index/index";
//
//    }
    //获取首页
    @GetMapping("/")
    public String index(){

        return "index";
    }

    //静态化技术
    @Autowired
    private TemplateEngine templateEngine;
    //使用静态化技术生成一个页面
    @GetMapping("/createHtml")
    @ResponseBody
    public Result createHtml(){
        //1：生成页面  数据 + 模板 == 输出
        //1:数据
        List<Map> listMap = productFeignClient.getBaseCategoryList();
        //数据
        Context context = new Context();
        //设置数据
        context.setVariable("list",listMap);
        //2:输出
        Writer out = null;
        try {
            //编码 ： 写也是 utf-8
            //String path = ClassUtils.getDefaultClassLoader().getResource("/") + "templates\\index.html";
            //request.getSession.getServletContext.getRealPath
            //out = new PrintWriter(new File("D:\\temp\\index.html"),"utf-8");
            out = new PrintWriter(new File("D:\\IdeaProjects\\gmall-191010\\gmall-parent\\web\\web-all\\target\\classes\\templates\\index.html"),"utf-8");
            //3:模板  读是UTF-8
            templateEngine.process("index/index",context,out);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(out != null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }




        return Result.ok();
    }
}
