package cn.com.ooamo.bio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class BIOClient {

    public static void main(String[] args) throws IOException, InterruptedException {
        // 创建客户端嵌套字，绑定端口
        Socket socket = new Socket("localhost", 1234);
        // 拿到输入流
        InputStream in = socket.getInputStream();
        // 拿到输出流
        OutputStream out = socket.getOutputStream();
        byte[] bytes = "hello! this is a BIO".getBytes();
        while (true){
            out.write(bytes);
            byte[] bytes1 = new byte[32];
            int read = in.read(bytes1, 0, bytes.length);
            if (read == -1){
                throw new RuntimeException("连接已断开");
            }
            System.out.println("revc：" + new String(bytes, 0, read));
            Thread.sleep(1000);
        }
    }
}
