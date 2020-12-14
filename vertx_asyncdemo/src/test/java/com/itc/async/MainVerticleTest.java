package com.itc.async;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
class MainVerticleTest {
    Vertx vertx = Vertx.vertx();

    @Test
    void start_server() {
        vertx.createHttpServer()
                .requestHandler(req -> req.response().end("Ok"))
                .listen(16969, ar -> {
                    // (we can check here if the server started or not)
                });
    }

    //@BeforeEach
    //void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    //    vertx.deployVerticle(new HttpServerVerticle(), testContext.succeedingThenComplete());
    //}
    //
    //// Repeat this test 3 times
    //@RepeatedTest(3)
    //void http_server_check_response(Vertx vertx, VertxTestContext testContext) {
    //    HttpClient client = vertx.createHttpClient();
    //    client.request(HttpMethod.GET, 8080, "localhost", "/")
    //            .compose(req -> req.send().compose(HttpClientResponse::body))
    //            .onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
    //                assertThat(buffer.toString()).isEqualTo("Plop");
    //                testContext.completeNow();
    //            })));
    //}

}