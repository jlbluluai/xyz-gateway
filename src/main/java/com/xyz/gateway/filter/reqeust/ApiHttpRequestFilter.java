package com.xyz.gateway.filter.reqeust;

import com.xyz.gateway.filter.HttpRequestFilter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.apache.commons.lang3.StringUtils;

public class ApiHttpRequestFilter implements HttpRequestFilter {

    private static final String[] LEGAL_APIS = new String[]{
            "/test", "/login"
    };


    @Override
    public boolean filter(FullHttpRequest fullRequest, ChannelHandlerContext ctx) {
        String uri = fullRequest.uri();

        // 检验Api合法性
        for (String legalApi : LEGAL_APIS) {
            if (StringUtils.startsWith(uri, legalApi)) {
                return true;
            }
        }

        return false;
    }

}
