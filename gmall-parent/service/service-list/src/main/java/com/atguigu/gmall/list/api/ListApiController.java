package com.atguigu.gmall.list.api;


import com.atguigu.gmall.list.service.ListService;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 搜索管理
 */
@RestController
@RequestMapping("/api/list")
public class ListApiController {


    @Autowired
    private ListService listService;

    //开始搜索
    //入参：SearchParam
    @PostMapping("/list")
    public SearchResponseVo list(@RequestBody SearchParam searchParam){
        return listService.list(searchParam);
    }

}
