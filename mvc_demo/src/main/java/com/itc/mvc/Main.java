package com.itc.mvc;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

/**
 * @ClassName Main
 * @Author sussenn
 * @Version 1.0.0
 * @Date 2020/12/18
 */
public class Main {

    public static void main(String[] args) {
        // vertxOptions用于配置vertx
        VertxOptions vertxOptions = new VertxOptions();
        vertxOptions.setWorkerPoolSize(40); // 默认10
        vertxOptions.setWarningExceptionTime(10L * 1000 * 1000000);  //block时间超过此值，打印代码堆栈         默认5s   单位ns
        vertxOptions.setBlockedThreadCheckInterval(2000); // 每隔2s，检查下是否block                         默认1s   单位s
        vertxOptions.setMaxEventLoopExecuteTime(2L * 1000 * 1000000); //允许eventloop block 的最长时间       默认2s   单位ns
        Vertx.vertx(vertxOptions).deployVerticle(MainVerticle.class.getName());
    }

}
