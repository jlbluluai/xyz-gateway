package com.xyz.gateway.filter;

import com.xyz.gateway.filter.reqeust.ApiHttpRequestFilter;
import com.xyz.gateway.filter.reqeust.HeaderHttpRequestFilter;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Slf4j
public class HttpRequestFilterChain {

    private final List<HttpRequestFilter> filters;

    {
        filters = new ArrayList<>();
        filters.add(new HeaderHttpRequestFilter());
        filters.add(new ApiHttpRequestFilter());
    }

    public boolean filter(FullHttpRequest fullRequest, ChannelHandlerContext ctx) {
        for (HttpRequestFilter filter : filters) {
            boolean flag = filter.filter(fullRequest, ctx);
            if (!flag) {
                // 直接封装失败返回
                FullHttpResponse response = null;
                try {
                    String body = "authentication failed";

                    response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.FORBIDDEN, Unpooled.wrappedBuffer(body.getBytes()));
                    response.headers().set("Content-Type", "application/json");
                    response.headers().setInt("Content-Length", body.length());
                } catch (Exception e) {
                    log.error("handleResponse error", e);
                    response = new DefaultFullHttpResponse(HTTP_1_1, NO_CONTENT);
                } finally {
                    if (fullRequest != null) {
                        if (!HttpUtil.isKeepAlive(fullRequest)) {
                            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
                        } else {
                            ctx.write(response);
                        }
                    }
                    ctx.flush();
                }
                return false;
            }
        }
        return true;
    }

}
