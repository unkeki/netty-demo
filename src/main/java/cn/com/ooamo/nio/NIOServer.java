package cn.com.ooamo.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NIOServer {

    public static void main(String[] args) throws IOException {
        // 创建服务通道，绑定端口
        ServerSocketChannel socketChannel = ServerSocketChannel.open().bind(new InetSocketAddress(9999));
        // 设置为非阻塞方式
        socketChannel.configureBlocking(false);
        // 创建selector
        Selector selector = Selector.open();
        // 将channel注册到selector上
        socketChannel.register(selector, SelectionKey.OP_ACCEPT);
        while (true){
            //selector会帮我们去轮询，当前是否有我们感兴趣的事件发生，一直阻塞到有为止
            //select还有一个方法，可以指定阻塞时间，超过这个时间就会返回，此时可能返回的key个数为0
            selector.select();
            //若返回的key个数不为0，那么就可以一一处理这些事件
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()){
                SelectionKey next = iterator.next();
                //remove是为了下一次select的时候，重复处理这些已经处理过的事件
                //什么意思呢？其实selector.selectedKeys()返回来的set，就是其
                //内部操作的set，引用的是同一个set，所以我们如果不在外面remove已经
                //处理的事件，那么下一次，还会再次出现。需要注意的是，如果在外面对set
                //进行add操作，会抛异常，简单的说就是在外只删不增，在内只增不删。
                iterator.remove();
                if (next.isAcceptable()){
                    SocketChannel accept = ((ServerSocketChannel) next.channel()).accept();
                    accept.configureBlocking(false);
                    accept.register(selector, SelectionKey.OP_READ);
                } else if (next.isReadable()) {
                    SocketChannel readChannel = (SocketChannel) next.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(32);
                    int read = readChannel.read(buffer);
                    if (read == -1){
                        throw new RuntimeException("连接已断开");
                    }
                    byte[] bytes = new byte[read];
                    //这个操作可以举个例子
                    //例如read(buffer)的时候，其实内部是调用了buffer.put这个方法
                    //那么read结束，position的位置必定等于len
                    //所以我们必须重置一下position为0，才可以从头开始读，但是读到什么地方呢？
                    //那就需要设置limit = position，所以flip后，position=0， limit = len
                    buffer.flip();
                    buffer.get(bytes);
                    System.out.println("recv:" + new String(bytes, 0, read));
                    //注册写事件
                    next.interestOps(next.interestOps() | SelectionKey.OP_WRITE);
                } else if (next.isWritable()) {
                    SocketChannel writeChannel = (SocketChannel)next.channel();
                    //写数据，也要用Buffer来写
                    int len = writeChannel.write(ByteBuffer.wrap("hello".getBytes()));
                    if(len == -1){
                        throw  new RuntimeException("连接已断开");
                    }
                    //这里为什么要取消写事件呢？因为只要底层的写缓冲区不满，就会一直收到这个事件
                    //所以只有想写数据的时候，才要注册这个写事件
                    next.interestOps(next.interestOps() & ~SelectionKey.OP_WRITE);
                }
            }
        }
    }
}
