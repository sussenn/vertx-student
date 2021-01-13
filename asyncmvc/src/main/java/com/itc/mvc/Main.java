package com.itc.mvc;

import io.vertx.core.Vertx;

/**
 * @ClassName Main
 * @Author sussenn
 * @Version 1.0.0
 * @Date 2020/12/16
 */
public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(MainVerticle.class.getName());
    }
}
