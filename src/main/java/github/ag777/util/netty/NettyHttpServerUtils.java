package github.ag777.util.netty;

import com.ag777.util.lang.collection.MapUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * netty构造http服务端工具类
 * @author ag777 <837915770@vip.qq.com>
 * @Date 2023/1/6 8:59
 */
public class NettyHttpServerUtils {

    public static void main(String[] args) throws InterruptedException {
        // 端口号
        int port = 8000;
        // 服务端返回文字编码
        Charset responseCharset = StandardCharsets.UTF_8;
        // 处理类，有ChannelHandler.Sharable 注解才能复用
        SimpleHttpServerHandler handler = new SimpleHttpServerHandler() {
            @Override
            public HttpResponse handle(FullHttpRequest request) {
                System.out.println("请求方法: " + request.method());
                System.out.println("uri: " + request.uri());
                return responseJson(HttpResponseStatus.OK, responseCharset, MapUtils.of("success", true));
            }

            @Override
            public HttpResponse onErr(HttpResponseStatus status, Throwable t) {
                if (t != null) {
                    t.printStackTrace();
                }
                return responseJson(status, responseCharset, MapUtils.of(
                        "success", false,
                        "message", status.toString()));
            }
        };
        build(port, ()->handler, channelFuture-> System.out.println("http文件目录服务器启动，网址是：" + "http://localhost:"+port));
    }

    public static void build(int port, Supplier<SimpleChannelInboundHandler<FullHttpRequest>> httpHandler, Consumer<ChannelFuture> afterStart) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
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
            });
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
}
