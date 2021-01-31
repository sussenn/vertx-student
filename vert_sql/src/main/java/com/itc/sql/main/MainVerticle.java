package com.itc.sql.main;

import com.itc.sql.db.DbVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

/**
 * <p>@ClassName MainVerticle
 * <p>@Author sussenn
 * <p>@Version 1.0.0
 * <p>@Date 2021/1/14
 */
public class MainVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start(Promise<Void> promise) {

        // 用于接收DbVerticle返回的结果
        Promise<String> dbDeployment = Promise.promise();
        vertx.deployVerticle(new DbVerticle(), dbDeployment);

        dbDeployment.future().compose(id -> {
            Promise<String> httpDeployment = Promise.promise();
            // 部署HttpVerticle
            vertx.deployVerticle("com.itc.sql.web.HttpVerticle", new DeploymentOptions().setInstances(1), httpDeployment);
            return httpDeployment.future();
        }).onComplete(ar -> {
            if (ar.succeeded()) {
                // 响应结果的异步获取
                promise.complete();
            } else {
                logger.error("数据库实例异常. err:", ar.cause());
                promise.fail(ar.cause());
            }
        });
    }
}
