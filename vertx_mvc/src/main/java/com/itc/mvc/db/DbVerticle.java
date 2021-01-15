package com.itc.mvc.db;

import com.itc.mvc.web.HttpVerticle;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName DbVerticle
 * @Author sussenn
 * @Version 1.0.0
 * @Date 2021/1/14
 */
public class DbVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(DbVerticle.class);

    // MySQL
    MySQLConnectOptions connectOptions;
    PoolOptions poolOptions = new PoolOptions().setMaxSize(10); //连接数 默认10
    MySQLPool client;

    @Override
    public void start(Promise<Void> promise) {
        ConfigRetriever confRet = ConfigRetriever.create(vertx);
        // 读取json配置文件信息
        getJsonConf(confRet).onComplete(confJson -> {
            if (confJson.succeeded()) {
                // MySQL连接参数配置
                connectOptions = new MySQLConnectOptions()
                        .setPort(confJson.result().getInteger("port"))
                        .setHost(confJson.result().getString("host"))
                        .setDatabase(confJson.result().getString("database"))
                        .setUser(confJson.result().getString("user"))
                        .setPassword(confJson.result().getString("password"));
                // MySQL客户端初始化
                client = MySQLPool.pool(vertx, connectOptions, poolOptions);

                // 监听生产者发送的消息并调用 onMessage()处理
                vertx.eventBus().consumer(HttpVerticle.ADDRESS_WEB, this::onMessage);
                // 异步获取响应结果
                promise.complete();
            } else {
                logger.error("数据库监听HTTP服务消息异常. err: ", confJson.cause());
                promise.fail(confJson.cause());
            }
        });
    }

    private void onMessage(Message<String> message) {
        String action = message.headers().get("action");
        switch (action) {
            case "getOne":
                getOne(message);
                break;
            case "getList":
                getList(message);
                break;
            default:
                message.fail(5002, "未知参数. action: " + action);
        }
    }

    private void getOne(Message<String> message) {
        // 获取MySQL连接    // 执行查询sql
        getConn().compose(conn -> findById(conn, message.body()).onComplete(rows -> {
            if (rows.succeeded()) {
                JsonObject userJson = new JsonObject();
                rows.result().forEach(row -> userJson.put("id", row.getInteger("id"))
                        .put("name", row.getString("name"))
                        .put("age", row.getInteger("age")));
                // 查询结果的封装
                message.reply(userJson);
            } else {
                logger.error("数据库查询信息处理失败. err: ", rows.cause());
                message.fail(5001, "数据处理失败");
            }
        }));
    }

    private void getList(Message<String> message) {
        getConn().compose(conn -> findAll(conn).onComplete(rows -> {
            if (rows.succeeded()) {
                List<JsonObject> userList = new ArrayList<>();
                rows.result().forEach(user -> {
                    JsonObject userJson = new JsonObject();
                    userJson.put("id", user.getInteger("id"));
                    userJson.put("name", user.getValue("name"));
                    userJson.put("age", user.getInteger("age"));
                    userList.add(userJson);
                });
                message.reply(new JsonArray(userList));
            } else {
                logger.error("数据库查询信息处理失败. err: ", rows.cause());
                message.fail(5001, "数据处理失败");
            }
        }));
    }


    // config 的异步读取
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
    private Future<RowSet<Row>> findById(SqlConnection conn, String id) {
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

    private Future<RowSet<Row>> findAll(SqlConnection conn) {
        Promise<RowSet<Row>> promise = Promise.promise();
        conn.query("select id,name,age from user")
                .execute(ar3 -> {
                    if (ar3.succeeded()) {
                        RowSet<Row> rows = ar3.result();
                        promise.complete(rows);
                    } else {
                        logger.error("mysql执行异常. err:", ar3.cause());
                        promise.fail(ar3.cause());
                    }
                    conn.close();
                });
        return promise.future();
    }
}
