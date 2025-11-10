
package github.ag777.util.remote.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * netty构造服务端工具类
 * @author ag777
 */
public class NettyServerUtils {

    public static void main(String[] args) throws InterruptedException {
        webSocket(8456, "/ws",
                ()->new SimpleChannelInboundHandler<TextWebSocketFrame>() {

                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
                        System.out.println("收到消息:"+frame.text());
                        ctx.writeAndFlush(new TextWebSocketFrame("您发送的消息是：" + frame.text())); // 将消息返回给客户端
                    }
                    @Override
                    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
                        System.out.println("Client connected: " + ctx.channel().remoteAddress());
                    }
                    @Override
                    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
                        System.out.println("Client disconnected: " + ctx.channel().remoteAddress());
                        ctx.close();
                    }
                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                        cause.printStackTrace();
                        ctx.close();
                    }

                },
                (future)-> System.out.println("服务启动"));
    }

    /**
     * 创建任意服务端
     * @param port 服务端端口号
     * @param childHandler 除妖为了配置处理流程
     * @param afterStart 服务启动后执行
     * @throws InterruptedException 中断
     */
    public static void custom(int port, ChannelHandler childHandler, Consumer<ChannelFuture> afterStart) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(childHandler);
            ChannelFuture future = b.bind("localhost", port)
                    .sync();
            if (afterStart != null) {
                afterStart.accept(future);
            }
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * 创建http服务端
     * @param port 服务端端口号
     * @param httpHandler 处理器，值所以用Supplier是因为处理器不能重复用，除非外部加了@ChannelHandler.Sharable注解
     * @param afterStart 服务启动后执行
     * @throws InterruptedException 中断
     */
    public static void http(int port, Supplier<SimpleChannelInboundHandler<FullHttpRequest>> httpHandler, Consumer<ChannelFuture> afterStart) throws InterruptedException {
        custom(port, new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                //1,http请求消息解码器
                ch.pipeline().addLast("http-decoder", new HttpRequestDecoder());
                //2，HttpObjectAggregator将多个消息转换为单一的FullHttpRequest或者FullHttpResponse
                ch.pipeline().addLast("http-aggregator", new HttpObjectAggregator(65536));
                //3,http响应编码器
                ch.pipeline().addLast("http-encoder", new HttpResponseEncoder());
                //4,ChunkedWriteHandler 支持异步发送大的码流
                ch.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
                //5,业务处理
                ch.pipeline().addLast("httpHandler", httpHandler.get());
            }
        }, afterStart);
    }

    /**
     * 创建websocket服务端
     * @param port 服务端端口号
     * @param url websocket地址, 如/ws
     * @param msgHandler 处理器，值所以用Supplier是因为处理器不能重复用，除非外部加了@ChannelHandler.Sharable注解
     * @param afterStart 服务启动后执行
     * @throws InterruptedException 中断
     */
    public static void webSocket(int port, String url, Supplier<SimpleChannelInboundHandler<TextWebSocketFrame>> msgHandler, Consumer<ChannelFuture> afterStart) throws InterruptedException {
        custom(port, new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {

                ch.pipeline()
                        // 因为基于http协议，使用http的编码和解码器
                        .addLast(new HttpServerCodec())
                        // 以块方式写，添加ChunkedWriteHandler处理器
                        .addLast(new ChunkedWriteHandler())
                        /*
                          说明
                          1. http数据在传输过程中是分段, HttpObjectAggregator ，就是可以将多个段聚合
                          2. 这就就是为什么，当浏览器发送大量数据时，就会发出多次http请求
                        */
                        .addLast(new HttpObjectAggregator(8192))
                        /* 说明
                          1. 对应websocket ，它的数据是以 帧(frame) 形式传递
                          2. 可以看到WebSocketFrame 下面有六个子类
                          3. 浏览器请求时 ws://localhost:7000/msg 表示请求的uri
                          4. WebSocketServerProtocolHandler 核心功能是将 http协议升级为 ws协议 , 保持长连接
                          5. 是通过一个 状态码 101
                        */
                        .addLast(new WebSocketServerProtocolHandler(url))
                        // 自定义的handler ，处理业务逻辑
                        .addLast(msgHandler.get());
            }
        }, afterStart);
    }
}
