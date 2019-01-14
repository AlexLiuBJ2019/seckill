package com.upload;

import org.apache.commons.net.ftp.*;

import java.io.*;

public class FtpClientEntity {

    public FTPClient getConnectionFTP(String hostName, int port, String username, String password, String location) throws IOException {
        //创建FTPClient对象
        FTPClient ftp = new FTPClient();
        try {
            //连接FTP服务器
            ftp.connect(hostName, port);
            ftp.setControlEncoding("utf-8");
            //下面三行代码必须要，而且不能改变编码格式，否则不能正确下载中文文件
//            ftp.setControlEncoding("GBK");
//            FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_NT);
//            conf.setServerLanguageCode("zh");
            // 登录ftp
            ftp.login(username, password);
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.changeWorkingDirectory(location);
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                ftp.disconnect();
                System.out.println("连接服务器失败");
            }
            System.out.println("登陆服务器成功");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ftp;
    }



    // /**
    // * 关闭连接FTP方式
    // * @param ftp FTPClient对象
    // * @return boolean
    // */
    public boolean closeFTP(FTPClient ftp) {
        if (ftp.isConnected()) {
            try {
                ftp.disconnect();
                System.out.println("ftp已经关闭");
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean uploadFile(FTPClient ftp, String path, String fileName, InputStream inputStream) throws IOException {
        boolean success = false;
        try {
            //判断墓库是否存在
            Boolean flag = ftp.changeWorkingDirectory(path);
            if(!flag){
                //目录不存在则创建
                String pathArr[] = path.split("/");
                for( String pathNew : pathArr){
                    if(!ftp.changeWorkingDirectory(pathNew)){
                    //分层创建目录
                    ftp.makeDirectory(pathNew);
                    }
                }
            }
            //转移到指定FTP服务器目录
             FTPFile[] fs = ftp.listFiles();
            // 得到目录的相应文件列表
             fileName = FtpClientEntity.changeName(fileName, fs);
             fileName = new String(fileName.getBytes("utf-8"),"ISO-8859-1");
             path = new String(path.getBytes("utf-8"), "ISO-8859-1");
            // 转到指定上传目录
             ftp.changeWorkingDirectory(path);
            // 将上传文件存储到指定目录
             ftp.setFileType(FTP.BINARY_FILE_TYPE);
            // 如果缺省该句 传输txt正常 但图片和其他格式的文件传输出现乱码
             ftp.storeFile(fileName, inputStream);
            // 关闭输入流
             inputStream.close();
            // 退出ftp
             ftp.logout();
            // 表示上传成功
             success = true;
             System.out.println("上传成功。。。。。。");
        } catch (Exception e) {
                e.printStackTrace();
        }
        return success;
    }

    public boolean deleteFile(FTPClient ftp, String path, String fileName) {
        boolean success = false;
        try {
            ftp.changeWorkingDirectory(path);
            //转移到指定FTP服务器目录
            fileName = new String(fileName.getBytes("GBK"), "UTF-8");
            path = new String(path.getBytes("GBK"), "UTF-8");
            ftp.deleteFile(fileName);
            ftp.logout();
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    public boolean downFile(FTPClient ftp, String path, String fileName, String localPath) {
        boolean success = false;
        try {
            ftp.changeWorkingDirectory(path);
            //转移到FTP服务器目录
            FTPFile[] fs = ftp.listFiles();
            //得到目录的相应文件列表
            for (FTPFile ff : fs) {
                if (ff.getName().equals(fileName)) {
                    File localFile = new File(localPath + "\\" + ff.getName());
                    OutputStream outputStream = new FileOutputStream(localFile);
                    //将文件保存到输出流outputStream中
                    ftp.retrieveFile(new String(ff.getName().getBytes("GBK"), "UTF-8"), outputStream);
                    outputStream.flush();
                    outputStream.close();
                    System.out.println("下载成功");
                }
            }
            ftp.logout();
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    public static boolean isFileExist(String fileName, FTPFile[] fs) {
        for (int i = 0; i < fs.length; i++) {
            FTPFile ff = fs[i];
            if (ff.getName().equals(fileName)) {
                return true;
                //如果存在返回 正确信号
                }
        }
        return false;
        //如果不存在返回错误信号
    }

    public static String changeName(String fileName, FTPFile[] fs) {
        int n = 0;
        //      fileName = fileName.append(fileName);
        while (isFileExist(fileName.toString(), fs)) {
            n++;
            String a = "[" + n + "]";
            int b = fileName.lastIndexOf(".");
            //最后一出现小数点的位置
            int c = fileName.lastIndexOf("[");
            //最后一次"["出现的位置
            if (c < 0) {
                c = b;
            }
            StringBuffer name = new StringBuffer(fileName.substring(0, c));
            //文件的名字
            StringBuffer suffix = new StringBuffer(fileName.substring(b + 1));
            //后缀的名称
            fileName = name.append(a) + "." + suffix;
        }
        return fileName.toString();
    }

    public static void main(String[] args) throws IOException {
        String path = "/user/user/2018/10";
//        String f1 = "C:\\Users\\xiaok\\Desktop\\image\\105.jpg";
        File f1 = new File("C:\\Users\\xiaok\\Desktop\\image\\105.jpg");
        String filename = f1.getName();
        System.out.println(filename);
        FtpClientEntity a = new FtpClientEntity();
        InputStream input = new FileInputStream(f1);
        FTPClient ftp = a.getConnectionFTP("114.215.78.222", 21, "root", "Lxgj889900","/usr/java/tomcat/webapps/ROOT");

        a.uploadFile(ftp, path, filename, input);
        a.closeFTP(ftp);
    }
}
