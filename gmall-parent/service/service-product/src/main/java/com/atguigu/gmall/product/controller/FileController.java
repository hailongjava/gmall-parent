package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import org.apache.commons.io.FilenameUtils;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 上传文件  图片  短视频
 */
@RestController
@RequestMapping("/admin/product")
public class FileController {

    @Value("${image.url}")
    private String imageUrl;

    //上传图片 （SPU添加）
    @PostMapping("/fileUpload")
    public Result fileUpload(MultipartFile file) throws Exception{
        String allPath = ClassUtils.getDefaultClassLoader().getResource("tracker.conf").getPath();
        //0:初始化配置文件
        ClientGlobal.init(allPath);//IO 流  不认识相对路径
        //1: 连接Tracker跟踪器   获取存储节点的地址
        TrackerClient trackerClient = new TrackerClient();
        //Tracker跟踪器 服务器 返回的地址
        TrackerServer trackerServer = trackerClient.getConnection();
        //2:连接存储节点
        StorageClient1 storageClient1 = new StorageClient1(trackerServer,null);
        //3:保存文件  并返回file_id 将来可以来访问获取之前保存的文件
        //扩展名
        String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        String path = storageClient1.upload_file1(
                file.getBytes(), ext
                , null);
        System.out.println(path);
        return Result.ok(imageUrl + path);
    }
}
