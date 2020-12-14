package com.itc.start;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.*;

/**
 * 文档demo学习: 配置文件读取 和 mysql查询
 */
public class MainVerticle extends AbstractVerticle {
    // netty 框架的
    // private final InternalLogger log = Log4JLoggerFactory.getInstance(MainVerticle.class);
    // vert.x 自带的
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
        logger.warn("begin...");
        // config读取器
        confRet = ConfigRetriever.create(vertx);
        confRet.getConfig(ar -> {
            // 若读取失败
            if (ar.failed()) {
                logger.error("json配置文件读取失败. err: [{}]", ar.cause());
            } else {
                JsonObject jsonConf = ar.result();
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
                    client.getConnection(ar1 -> {
                        if (ar1.succeeded()) {
                            SqlConnection conn = ar1.result();
                            conn.preparedQuery("select id,name,age from user where id = ?")
                                    .execute(Tuple.of(id), ar2 -> {
                                        conn.close();
                                        if (ar2.succeeded()) {
                                            JsonObject userJson = new JsonObject();
                                            RowSet<Row> rows = ar2.result();
                                            rows.forEach(row -> {
                                                userJson.put("id", row.getInteger("id"));
                                                userJson.put("name", row.getString("name"));
                                                userJson.put("age", row.getInteger("age"));
                                            });
                                            req.response().putHeader("content-type", "application/json")
                                                    .putHeader("charset", "utf-8")
                                                    .end(userJson.toString());
                                        } else {
                                            req.response()
                                                    .putHeader("content-type", "text/plain")
                                                    .end(ar2.cause().getMessage());
                                        }
                                    });
                        } else {
                            logger.error("数据库连接失败. err: [{}]", ar1.cause());
                        }
                    });
                });

                // 路由绑定
                vertx.createHttpServer().requestHandler(router).listen(8212);
            }
        });
    }
}
