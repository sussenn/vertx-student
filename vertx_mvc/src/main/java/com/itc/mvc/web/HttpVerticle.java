package com.itc.mvc.web;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * @ClassName HttpVerticle
 * @Author sussenn
 * @Version 1.0.0
 * @Date 2021/1/14
 */
public class HttpVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(HttpVerticle.class);

    @Override
    public void start(Promise<Void> promise) {
        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.get("/db/cof/get/:id").handler(this::findById);

        server.requestHandler(router).listen(8083, ar -> {
            if (ar.succeeded()) {
                logger.info("HTTP服务启动成功,port: [8083]");
                promise.complete();
            } else {
                logger.error("HTTP服务启动失败,err: ", ar.cause());
                promise.fail(ar.cause());
            }
        });
    }

    private void findById(RoutingContext context) {
        String id = context.request().getParam("id");

        // 给add_http发送消息
        DeliveryOptions options = new DeliveryOptions();
        vertx.eventBus().request("add_http", id, options, reply -> {
            if (reply.succeeded()) {
                context.response().end(new JsonObject()
                        .put("code", 200)
                        .put("msg", "查询成功")
                        .put("data", reply.result().body()).encode(), "utf-16");
            } else {
                logger.error("findById() 查询用户信息异常. err: ", reply.cause());
                context.fail(reply.cause());
            }
        });
    }
}
