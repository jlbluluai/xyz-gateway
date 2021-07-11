package com.xyz.gateway.filter;

import com.xyz.gateway.filter.response.HeaderHttpResponseFilter;
import com.xyz.gateway.filter.response.LogHttpResponseFilter;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class HttpResponseFilterChain {

    private final List<HttpResponseFilter> filters;

    {
        filters = new ArrayList<>();
        filters.add(new HeaderHttpResponseFilter());
        filters.add(new LogHttpResponseFilter());
    }

    public void filter(FullHttpResponse response) {
        for (HttpResponseFilter filter : filters) {
            filter.filter(response);
        }
    }

}
