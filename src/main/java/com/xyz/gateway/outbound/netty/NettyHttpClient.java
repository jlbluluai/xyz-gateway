package com.xyz.gateway.outbound.netty;

import com.xyz.gateway.outbound.OutBoundExecutor;
import com.xyz.gateway.outbound.OutboundHandler;
import com.xyz.gateway.router.HttpEndpointRouter;
import com.xyz.gateway.router.RandomHttpEndpointRouter;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Slf4j
public class NettyHttpClient implements OutboundHandler {

    private List<String> backendUrls;
    private ExecutorService proxyService;

    HttpEndpointRouter router = new RandomHttpEndpointRouter();

    public NettyHttpClient(List<String> backends) {
        this.backendUrls = backends.stream().map(this::formatUrl).collect(Collectors.toList());
        this.proxyService = OutBoundExecutor.getProxyService();
    }

    @Override
    public void handle(FullHttpRequest fullRequest, ChannelHandlerContext ctx) {
        String backendUrl = router.route(this.backendUrls);

        final String url = backendUrl + fullRequest.uri();
        proxyService.execute(() -> connect(url, fullRequest, ctx));
    }

    public void connect(String url, FullHttpRequest serverFullRequest, ChannelHandlerContext serverCtx) {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // 取host和ip
            URL netUrl = new URL(url);
            String host = netUrl.getHost();
            int port = netUrl.getPort();

            Bootstrap b = new Bootstrap();
            b.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(host, port))
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new HttpRequestEncoder());
                            ch.pipeline().addLast(new HttpResponseDecoder());
                            ch.pipeline().addLast(new NettyHttpClientOutboundHandler(serverFullRequest, serverCtx));
                        }
                    });

            // Start the client.
            ChannelFuture cf = b.connect().sync();


            // 发送请求
            URI uri = new URI(url);
            FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri.toASCIIString());

            request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());

            cf.channel().write(request);
            cf.channel().flush();


            cf.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("connect error", e);
        } finally {
            workerGroup.shutdownGracefully();
        }

    }

}