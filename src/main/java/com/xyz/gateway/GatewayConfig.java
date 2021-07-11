package com.xyz.gateway;

public class GatewayConfig {

    /**
     * 服务端口
     */
    public final static int PORT = 8888;

    /**
     * 访问业务系统模式
     * <p>
     * netty || httpclient
     */
    public final static String CLIENT_MODE_NETTY = "netty";
    public final static String CLIENT_MODE_HTTPCLIENT = "httpclient";

}
