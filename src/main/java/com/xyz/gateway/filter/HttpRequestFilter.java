package com.xyz.gateway.filter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public interface HttpRequestFilter {

    /**
     * @return false:不予以通行
     */
    boolean filter(FullHttpRequest fullRequest, ChannelHandlerContext ctx);

}
