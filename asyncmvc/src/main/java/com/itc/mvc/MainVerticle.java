package com.itc.mvc;

import com.itc.mvc.util.TimeOutApi;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.WorkerExecutor;

/**
 * @ClassName MainVerticle  WorkerPool 的配置
 * @Author sussenn
 * @Version 1.0.0
 * @Date 2020/12/16
 */
public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        // 搭配executeBlocking() 配置WorkerPool :todo 为了并行执行那些阻塞方法(前提是不关心它们的顺序)
        WorkerExecutor executor = vertx.createSharedWorkerExecutor("my-worker-pool", 10, 120000); //10个线程.最多执行2分钟
        // 配置executeBlocking() :todo 为了那些阻塞方法的执行及结果回调
        executor.executeBlocking(future -> {
            // 调用一些需要耗费显著执行时间返回结果的阻塞式API
            String result = TimeOutApi.blockingMethod("hello");
            future.complete(result);
        }, res -> {
            System.out.println("The result is: " + res.result());
        });

        // 使用完必须关闭!
        executor.close();


        vertx.createHttpServer().requestHandler(req -> {
            req.response().putHeader("content-type", "text/plain")
                    .end("Hello World");
        }).listen(8888, http -> {
            if (http.succeeded()) {
                startPromise.complete();
            } else {
                startPromise.fail(http.cause());
            }
        });
    }
}
