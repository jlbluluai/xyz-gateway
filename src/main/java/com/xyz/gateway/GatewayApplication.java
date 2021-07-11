package com.xyz.gateway;


import com.xyz.gateway.server.NettyHttpServer;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public class GatewayApplication {

    public static void main(String[] args) {
        String proxyServers = System.getProperty("proxyServers", "http://localhost:8801,http://localhost:8802");

        log.info("gateway starting ...");
        NettyHttpServer nettyHttpServer = new NettyHttpServer(GatewayConfig.PORT, Arrays.asList(proxyServers.split(",")), GatewayConfig.CLIENT_MODE_NETTY);
        log.info("gateway started at http://127.0.0.1:{} for server:" + nettyHttpServer.toString());
        try {
            nettyHttpServer.run();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
