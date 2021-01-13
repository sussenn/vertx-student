package com.itc.mvc;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

/**
 * @ClassName LoadMoreVerticle  异步加载各个类. (类似依赖注入)
 * @Author sussenn
 * @Version 1.0.0
 * @Date 2020/12/16
 */
public class LoadMoreVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        // 加载其他的一些verticle
        vertx.deployVerticle(new MainVerticle(), res -> {
            if (res.succeeded()) {
                startPromise.complete();
            } else {
                startPromise.fail(res.cause());
            }
        });
    }

}
