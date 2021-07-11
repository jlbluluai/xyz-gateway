package com.xyz.gateway.filter.response;

import com.xyz.gateway.filter.HttpResponseFilter;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogHttpResponseFilter implements HttpResponseFilter {

    @Override
    public void filter(FullHttpResponse response) {

        ByteBuf buf = response.content();
        String body = buf.toString(CharsetUtil.UTF_8);
        log.info("information flow, info = {}", body);

    }

}
