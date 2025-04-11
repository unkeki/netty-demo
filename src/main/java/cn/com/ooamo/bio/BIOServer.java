package cn.com.ooamo.bio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class BIOServer {

    public static void main(String[] args) throws IOException{
        // 创建服务嵌套字，绑定端口，监听client
        ServerSocket serverSocket = new ServerSocket(1234);
        // 等待客户端链接
        Socket accept = serverSocket.accept();
        // 拿到输入流
        InputStream in = accept.getInputStream();
        // 拿到输出流
        OutputStream out = accept.getOutputStream();
        while (true){
            byte[] bytes = new byte[32];
            int read = in.read(bytes);
            if (read == -1){
                throw new RuntimeException("连接已断开");
            }
            System.out.println("recv：" + new String(bytes, 0, read));
            // 将读出来的数据写回给client
            // 如果不使用偏移量，会将无效数据也写入
            out.write(bytes, 0, read);
        }
    }

}
