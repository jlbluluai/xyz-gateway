package com.xyz.gateway.outbound.netty;

import com.xyz.gateway.filter.HttpResponseFilterChain;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Slf4j
public class NettyHttpClientOutboundHandler extends ChannelInboundHandlerAdapter {

    private final HttpResponseFilterChain filterChain = new HttpResponseFilterChain();

    private final FullHttpRequest serverFullRequest;
    private final ChannelHandlerContext serverCtx;

    public NettyHttpClientOutboundHandler(FullHttpRequest serverFullRequest, ChannelHandlerContext serverCtx) {
        this.serverFullRequest = serverFullRequest;
        this.serverCtx = serverCtx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
//        System.out.println("msg->" + msg);

        if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;
            ByteBuf buf = content.content();
            String body = buf.toString(CharsetUtil.UTF_8);
            buf.release();

            FullHttpResponse response = null;
            try {
                response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(body.getBytes()));
                response.headers().set("Content-Type", "application/json");
                response.headers().setInt("Content-Length", body.length());

                filterChain.filter(response);
            } catch (Exception e) {
                log.error("handleResponse error", e);
                response = new DefaultFullHttpResponse(HTTP_1_1, NO_CONTENT);
                exceptionCaught(ctx, e);
            } finally {
                if (serverFullRequest != null) {
                    if (!HttpUtil.isKeepAlive(serverFullRequest)) {
                        serverCtx.write(response).addListener(ChannelFutureListener.CLOSE);
                    } else {
                        serverCtx.write(response);
                    }
                }
                serverCtx.flush();
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}