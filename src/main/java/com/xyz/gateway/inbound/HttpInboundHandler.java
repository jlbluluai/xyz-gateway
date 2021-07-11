package com.xyz.gateway.inbound;

import com.xyz.gateway.GatewayConfig;
import com.xyz.gateway.filter.HttpRequestFilterChain;
import com.xyz.gateway.outbound.OutboundHandler;
import com.xyz.gateway.outbound.httpclient.HttpOutboundHandler;
import com.xyz.gateway.outbound.netty.NettyHttpClient;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class HttpInboundHandler extends ChannelInboundHandlerAdapter {

    private List<String> proxyServer;
    private OutboundHandler outboundHandler;
    private HttpRequestFilterChain filterChain = new HttpRequestFilterChain();

    public HttpInboundHandler(List<String> proxyServer, String clientMode) {
        this.proxyServer = proxyServer;

        if (clientMode.equals(GatewayConfig.CLIENT_MODE_HTTPCLIENT)) {
            // 初始化client模式为httpclient
            this.outboundHandler = new HttpOutboundHandler(this.proxyServer);
        } else if (clientMode.equals(GatewayConfig.CLIENT_MODE_NETTY)) {
            // 初始化client模式为netty
            this.outboundHandler = new NettyHttpClient(proxyServer);
        } else {
            // client模式错误
            throw new IllegalArgumentException("clientMode error");
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            FullHttpRequest fullRequest = (FullHttpRequest) msg;

            // 先过滤
            boolean filter = filterChain.filter(fullRequest, ctx);
            if (!filter) {
                ctx.close();
                return;
            }

            outboundHandler.handle(fullRequest, ctx);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
