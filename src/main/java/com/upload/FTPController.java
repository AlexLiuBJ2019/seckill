package com.upload;

import org.apache.commons.net.ftp.FTPClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Controller
@RequestMapping("/")
public class FTPController {

    @RequestMapping(value = "/toUpload")
    public String ftp(){
        return "ftp";
    }
    @RequestMapping(value = "/upload")
    public void saveImage(@RequestParam(value="file") MultipartFile file) throws Exception{
        if(file==null){
            System.out.println("------------上传文件为空-----------");
            return ;
        }
        //存在ftp图片服务器的路径
        String path = "openaccount/witness/IdCardImg/";
        String filename = file.getOriginalFilename();
        //获得原始的文件名
        InputStream input=file.getInputStream();
        System.out.println("------------上传文件名-----------"+filename);
        FtpClientEntity a = new FtpClientEntity();
        FTPClient ftp = a.getConnectionFTP("114.215.78.222", 21, "root", "Lxgj889900","/usr/java/tomcat/webapps/ROOT");
        a.uploadFile(ftp, path, filename, input);
        a.closeFTP(ftp);
    }//saveImage

}
