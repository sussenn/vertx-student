package com.itc.async;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.*;

/**
 * 异步调用
 */
public class MainVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

    // 路由
    private Router router;
    // MySQL
    MySQLConnectOptions connectOptions;
    PoolOptions poolOptions = new PoolOptions().setMaxSize(10); //连接数 默认10
    MySQLPool client;
    // Config
    ConfigRetriever confRet;

    @Override
    public void start() {
        // 初始化config读取器
        confRet = ConfigRetriever.create(vertx);
        // config 异步读取
        getJsonConf(confRet).onSuccess(jsonConf -> {
            connectOptions = new MySQLConnectOptions()
                    .setPort(jsonConf.getInteger("port"))
                    .setHost(jsonConf.getString("host"))
                    .setDatabase(jsonConf.getString("database"))
                    .setUser(jsonConf.getString("user"))
                    .setPassword(jsonConf.getString("password"));

            router = Router.router(vertx);
            router.route().handler(BodyHandler.create());
            client = MySQLPool.pool(vertx, connectOptions, poolOptions);

            router.get("/db/cof/get/:id").handler(req -> {
                String id = req.request().getParam("id");
                // MySQL连接 异步获取
                getConn().compose(conn -> getRows(conn, id) // MySQL查询 异步执行
                        .onSuccess(rows -> {
                            JsonObject userJson = new JsonObject();
                            rows.forEach(row -> {
                                userJson.put("id", row.getInteger("id"));
                                userJson.put("name", row.getString("name"));
                                userJson.put("age", row.getInteger("age"));
                            });
                            req.response().putHeader("content-type", "application/json")
                                    .putHeader("charset", "utf-8")
                                    .end(new JsonObject()
                                            .put("code", 200)
                                            .put("msg", "查询成功")
                                            .put("data", userJson).toString());
                        }));
                //.onFailure(err ->
                //        req.response().putHeader("content-type", "application/json")
                //                .end(new JsonObject().put("msg", err).toString()));
            });
            // 路由绑定
            vertx.createHttpServer().requestHandler(router).listen(8213);
        });
    }

    //config 的异步读取
    private Future<JsonObject> getJsonConf(ConfigRetriever confRet) {
        Promise<JsonObject> promise = Promise.promise();
        confRet.getConfig(ar -> {
            // 若读取失败
            if (ar.failed()) {
                logger.error("json配置文件读取失败. err:", ar.cause());
                promise.fail(ar.cause());
            } else {
                JsonObject jsonConf = ar.result();
                promise.complete(jsonConf);
            }
        });
        return promise.future();
    }

    // mysql连接 的异步获取
    private Future<SqlConnection> getConn() {
        Promise<SqlConnection> promise = Promise.promise();
        client.getConnection(ar1 -> {
            if (ar1.succeeded()) {
                SqlConnection conn = ar1.result();
                promise.complete(conn);
            } else {
                logger.error("获取MySQL连接失败. err:", ar1.cause());
                promise.fail(ar1.cause());
            }
        });
        return promise.future();
    }

    // mysql查询 的异步执行
    private Future<RowSet<Row>> getRows(SqlConnection conn, String id) {
        Promise<RowSet<Row>> promise = Promise.promise();
        conn.preparedQuery("select id,name,age from user where id = ?")
                .execute(Tuple.of(id), ar2 -> {
                    if (ar2.succeeded()) {
                        RowSet<Row> rows = ar2.result();
                        promise.complete(rows);
                    } else {
                        logger.error("mysql执行异常. err:", ar2.cause());
                        promise.fail(ar2.cause());
                    }
                    conn.close();
                });
        return promise.future();
    }
}
