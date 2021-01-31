package com.itc.sql.web;

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
 * <p>@ClassName HttpVerticle
 * <p>@Author sussenn
 * <p>@Version 1.0.0
 * <p>@Date 2021/1/14
 */
public class HttpVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(HttpVerticle.class);
    public static String ADDRESS_WEB = "add_http";

    @Override
    public void start(Promise<Void> promise) {
        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.get("/db/findById/:id").handler(this::findById);
        router.get("/db/findAll").handler(this::findAll);
        router.post("/db/add").handler(this::add);

        server.requestHandler(router).listen(8084, ar -> {
            if (ar.succeeded()) {
                logger.info("HTTP server running on port: [8084]");
                promise.complete();
            } else {
                logger.error("HTTP server not start,err: ", ar.cause());
                promise.fail(ar.cause());
            }
        });
    }

    private void findById(RoutingContext context) {
        JsonObject id = new JsonObject().put("id", context.request().getParam("id"));
        // 给add_http发送消息
        DeliveryOptions options = new DeliveryOptions().addHeader("action", "getOne");
        vertx.eventBus().request(ADDRESS_WEB, id, options, reply -> {
            if (reply.succeeded()) {
                context.response().putHeader("content-type", "application/json")
                        .putHeader("charset", "utf-8")
                        .end(new JsonObject()
                                .put("code", 200)
                                .put("msg", "查询成功")
                                .put("data", reply.result().body()).encode());
            } else {
                logger.error("findById() 查询用户信息异常. err: ", reply.cause());
                context.fail(reply.cause());
            }
        });
    }

    private void findAll(RoutingContext context) {
        DeliveryOptions options = new DeliveryOptions().addHeader("action", "getList");
        vertx.eventBus().request(ADDRESS_WEB, null, options, reply -> {
            if (reply.succeeded()) {
                context.response().putHeader("content-type", "application/json")
                        .putHeader("charset", "utf-8")
                        .end(new JsonObject()
                                .put("code", 200)
                                .put("msg", "查询所有数据成功")
                                .put("data", reply.result().body()).encode());
            } else {
                logger.error("findAll() 查询所有用户信息异常. err: ", reply.cause());
                context.fail(reply.cause());
            }
        });
    }

    private void add(RoutingContext context) {
        JsonObject data = context.getBodyAsJson();
        DeliveryOptions options = new DeliveryOptions().addHeader("action", "insert");
        vertx.eventBus().request(ADDRESS_WEB, data, options, reply -> {
            if (reply.succeeded()) {
                context.response().putHeader("content-type", "application/json")
                        .putHeader("charset", "utf-8")
                        .end(new JsonObject()
                                .put("code", 200)
                                .put("msg", "新增成功")
                                .put("data", reply.result().body()).encode());
            } else {
                logger.error("add() 新增用户信息异常. err: ", reply.cause());
                context.fail(reply.cause());
            }
        });
    }
}
