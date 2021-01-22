package com.itc.mvc.main;

import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(VertxExtension.class)
public class MainVerticleTest {

    //@BeforeEach
    //void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    //    vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> testContext.completeNow()));
    //}


    @Test
    public void test00() {
        Map<String, Object> map = new HashMap<>();
        map.put("1", "baba");
        map.put("2", 18);
        map.put("3", false);

        System.out.println(map);
    }
}