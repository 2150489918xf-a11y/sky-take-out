package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 通用接口
 */
@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;
    /**
     * 文件上传接口
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @ApiOperation("文件上传接口")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传：{}", file);

        try {
            //获取原始文件名
            String originalFilename = file.getOriginalFilename();
            //获取文件后缀
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            //生成新的文件名，使用UUID随机生成一个字符串作为文件名，避免文件名重复导致文件覆盖
            String objectname = java.util.UUID.randomUUID().toString()+extension;

            //文件的请求路径
            String filePath = aliOssUtil.upload(file.getBytes(), objectname);
            return Result.success(filePath);
        } catch (IOException e) {
            log.error("文件上传失败：{}", e);
        }

        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
