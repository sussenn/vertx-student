package com.itc.integration.web;

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
 * @Date 2022/7/22
 */
public class HttpVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(HttpVerticle.class);

    public static String ADDRESS_WEB = "adder_http";

    @Override
    public void start(Promise<Void> startPromise) {
        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.get("/db/findById/:id").handler(this::findById);
        router.get("/db/findAll").handler(this::findAll);
        router.get("/db/findPage/:pageNum/:pageSize").handler(this::findPage);
        router.post("/db/add").handler(this::add);

        server.requestHandler(router).listen(8722, ar -> {
            if (ar.succeeded()) {
                logger.info("HTTP server running on port: [8722]");
                startPromise.complete();
            } else {
                logger.error("HTTP server not start,err: ", ar.cause());
                startPromise.fail(ar.cause());
            }
        });
    }

    private void findPage(RoutingContext context) {
        int pageSize = Integer.parseInt(context.request().getParam("pageSize"));
        int pageNum = Integer.parseInt(context.request().getParam("pageNum"));
        int skip = (pageNum - 1) * pageSize;
        JsonObject page = new JsonObject().put("pageNum", pageNum)
                .put("pageSize", pageSize)
                .put("skip", skip);

        DeliveryOptions options = new DeliveryOptions().addHeader("action", "getPage");
        vertx.eventBus().request(ADDRESS_WEB, page, options, reply -> {
            if (reply.succeeded()) {
                context.response().putHeader("content-type", "application/json")
                        .putHeader("charset", "utf-8")
                        .end(new JsonObject()
                                .put("code", 200)
                                .put("msg", "分页查询数据成功")
                                .put("data", reply.result().body()).encode());
            } else {
                logger.error("findPage() 分页查询数据异常. err: ", reply.cause());
                context.fail(reply.cause());
            }
        });
    }

    private void findById(RoutingContext context) {
        JsonObject id = new JsonObject().put("id", context.request().getParam("id"));
        // 给adder_http发送消息
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
        JsonObject data = context.body().asJsonObject();
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
