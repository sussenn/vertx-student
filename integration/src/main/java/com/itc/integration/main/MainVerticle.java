package com.itc.integration.main;

import com.itc.integration.db.DbVerticle;
import com.itc.integration.util.Runner;
import com.itc.integration.web.HttpVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

/**
 * @ClassName MainVerticle
 * @Author sussenn
 * @Version 1.0.0
 * @Date 2022/7/22
 */
public class MainVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

    public static void main(String[] args) {
        Runner.runExample(MainVerticle.class);
    }

    @Override
    public void start(Promise<Void> startPromise) {
        // 用于接收DbVerticle返回的结果
        Promise<String> dbDeployment = Promise.promise();
        vertx.deployVerticle(new DbVerticle(), dbDeployment);

        dbDeployment.future().compose(id -> {
            Promise<String> httpDeployment = Promise.promise();
            // 部署HttpVerticle
            vertx.deployVerticle(new HttpVerticle(), new DeploymentOptions().setInstances(1), httpDeployment);
            return httpDeployment.future();
        }).onComplete(ar -> {
            if (ar.succeeded()) {
                // 响应结果的异步获取
                startPromise.complete();
            } else {
                logger.error("数据库实例异常. err:", ar.cause());
                startPromise.fail(ar.cause());
            }
        });
    }
}
