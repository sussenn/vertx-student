package com.itc.mvc;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.net.NetServer;

/**
 * @ClassName MultiFutureVerticle   多个Future结果的回调
 * @Author sussenn
 * @Version 1.0.0
 * @Date 2020/12/16
 */
public class MultiFutureVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        //Future<HttpServer> httpServerFuture = Future.future();
        //httpServer.listen(httpServerFuture.completer());

        //Future<NetServer> netServerFuture = Future.future();
        //netServer.listen(netServerFuture.completer());

        // CompositeFuture().all todo 将以上多个future并发执行
        //CompositeFuture.all(httpServerFuture, netServerFuture).setHandler(ar -> {
        //    if (ar.succeeded()) {
        //        // 所有服务器启动完成
        //    } else {
        //        // 有一个服务器启动失败
        //    }
        //});

        vertx.createHttpServer().requestHandler(req -> {
            req.response().putHeader("content-type","text/plain")
                    .end("Hello World");
        })
                .listen(8080);
    }
}
