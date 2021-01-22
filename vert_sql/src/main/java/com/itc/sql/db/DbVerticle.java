package com.itc.sql.db;

import com.itc.sql.web.HttpVerticle;
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
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.templates.SqlTemplate;
import io.vertx.sqlclient.templates.TupleMapper;

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

    MySQLConnectOptions connectOptions;
    PoolOptions poolOptions = new PoolOptions().setMaxSize(10); //连接数 默认10
    MySQLPool pool;

    @Override
    public void start(Promise<Void> promise) {
        ConfigRetriever confRet = ConfigRetriever.create(vertx);
        // 读取json配置文件信息
        getJsonConf(confRet).onComplete(confJson -> {
            if (confJson.succeeded()) {
                // SQL连接参数配置
                connectOptions = new MySQLConnectOptions()
                        .setPort(confJson.result().getInteger("port"))
                        .setHost(confJson.result().getString("host"))
                        .setDatabase(confJson.result().getString("database"))
                        .setUser(confJson.result().getString("user"))
                        .setPassword(confJson.result().getString("password"));
                // 连接池初始化
                pool = MySQLPool.pool(vertx, connectOptions, poolOptions);

                // 监听生产者发送的消息并调用 onMessage()处理
                vertx.eventBus().consumer(HttpVerticle.ADDRESS_WEB, this::onMessage);
                // 响应结果的推送
                promise.complete();
            } else {
                logger.error("数据库监听HTTP服务消息异常. err: ", confJson.cause());
                promise.fail(confJson.cause());
            }
        });
    }

    private void onMessage(Message<JsonObject> message) {
        String action = message.headers().get("action");
        switch (action) {
            case "getOne":
                getOne(message);
                break;
            case "getList":
                getList(message);
                break;
            case "insert":
                insert(message);
                break;
            default:
                message.fail(5002, "未知参数. action: " + action);
        }
    }

    private void getOne(Message<JsonObject> message) {
        SqlTemplate.forQuery(pool, "select id,name,age from user where id = #{id}")
                // 接收的结果映射为json类型
                .mapTo(Row::toJson)
                // 传参
                .execute(message.body().getMap())
                .onSuccess(res -> {
                    if (!res.iterator().hasNext()) {
                        message.reply("暂无数据");
                    } else {
                        message.reply(res.iterator().next());
                    }
                })
                .onFailure(err -> message.fail(5001, "数据处理失败. err: " + err.getMessage()));
    }

    private void getList(Message<JsonObject> message) {
        SqlTemplate.forQuery(pool, "select id, name, age from user")
                .mapTo(Row::toJson)
                .execute(null)
                .onSuccess(res -> {
                    List<JsonObject> userList = new ArrayList<>();
                    res.forEach(userList::add);
                    message.reply(new JsonArray(userList));
                })
                .onFailure(err -> message.fail(5001, "数据处理失败. err: " + err.getMessage()));

    }

    private void insert(Message<JsonObject> message) {
        SqlTemplate.forUpdate(pool, "INSERT INTO `vertx_stu`.`user` (`id`, `name`, `age`) VALUES (#{id}, #{name}, #{age})")
                .mapFrom(TupleMapper.jsonObject())
                .execute(message.body())
                .onSuccess(res -> message.reply("OK"))
                .onFailure(err -> message.fail(5001, "数据处理失败. err: " + err.getMessage()));
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

}
