package github.ag777.util.netty;

import com.ag777.util.gson.GsonUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.nio.charset.Charset;

/**
 * http请求处理类
 * @author ag777 <837915770@vip.qq.com>
 * @Date 2023/1/6 9:05
 */
@ChannelHandler.Sharable    // 没有这个注释handler复用不了
public abstract class SimpleHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest>  {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        // 验证解码
        if (!request.decoderResult().isSuccess()) {
            HttpResponse response = onErr(HttpResponseStatus.BAD_REQUEST, null);
            sendAndClose(ctx, response);
            return;
        }
        HttpResponse response;
        try {
            response = handle(request);
        } catch (Throwable t) {
            response = onErr(HttpResponseStatus.INTERNAL_SERVER_ERROR, t);
        }
        sendAndClose(ctx, response);
    }

    public abstract HttpResponse handle(FullHttpRequest request);
    public abstract HttpResponse onErr(HttpResponseStatus status, Throwable t);

    protected void sendAndClose(ChannelHandlerContext ctx, HttpResponse response) {
        ctx.writeAndFlush(response)
                .addListener(ChannelFutureListener.CLOSE);
    }

    protected HttpResponse responseHtml(HttpResponseStatus status, Charset charset, String body) {
        return response(status, "text/html", charset, body);
    }

    protected HttpResponse responseText(HttpResponseStatus status, Charset charset, String body) {
        return response(status, "text/plain", charset, body);
    }

    protected HttpResponse responseJson(HttpResponseStatus status, Charset charset, Object obj) {
        if (obj instanceof String) {
            return response(status, "application/json", charset, (String) obj);
        }
        return response(status, "application/json", charset, GsonUtils.get().toJson(obj));
    }

    protected HttpResponse response(HttpResponseStatus status, String contentType, Charset charset, String body) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer(body, charset));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType+";charset="+charset);
        return response;
    }
}
