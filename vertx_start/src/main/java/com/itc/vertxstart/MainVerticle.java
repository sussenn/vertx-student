package com.itc.vertxstart;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.mysqlclient.impl.MySQLConnectionFactory;
import io.vertx.sqlclient.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 官网start 入门
 */
public class MainVerticle extends AbstractVerticle {

  // 路由器
  private Router router;
  // MySQL驱动
  MySQLConnectOptions connectOptions = new MySQLConnectOptions()
    .setPort(3306)
    .setHost("127.0.0.1")
    .setDatabase("vertx_stu")
    .setUser("root")
    .setPassword("123456");

  // 数据库连接池
  PoolOptions poolOptions = new PoolOptions()
    .setMaxSize(10); //连接数 默认10

  // 数据库对象
  MySQLPool client;


  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    router = Router.router(vertx);
    // 获取请求体参数必须配置此项
    router.route().handler(BodyHandler.create());

    // MySQL相关-----------------------------------------------------------
    client = MySQLPool.pool(vertx, connectOptions, poolOptions);
    router.get("/db/get/:id/:age").handler(req -> {
      String id = req.request().getParam("id");
      String age = req.request().getParam("age");
      client.getConnection(ar1 -> {
        if (ar1.succeeded()) {
          SqlConnection conn = ar1.result();
          conn.preparedQuery("select id,name,age from user where id = ? and age = ?")
            //设置查询条件参数 可传多个
            .execute(Tuple.of(id, age), ar2 -> {
              conn.close();
              if (ar2.succeeded()) {
                JsonObject userJson = new JsonObject();
                RowSet<Row> rows = ar2.result();
                rows.forEach(row -> {
                  userJson.put("id", row.getInteger("id"));
                  userJson.put("name", row.getString("name"));
                  userJson.put("age", row.getInteger("age"));
                });
                req.response().putHeader("content-type", "application/json")
                  .putHeader("charset", "utf-8")
                  .end(userJson.toString());
              } else {
                req.response().putHeader("content-type", "text/plain")
                  .end("查询失败:" + ar2.cause().toString());
              }
            });
        } else {
          System.err.println("数据库连接失败2:" + ar1.cause().getMessage());
        }
      });
    });

    router.get("/db/list").handler(req -> {
      // 初始化数据库对象
      client.getConnection(ar1 -> {
        // 连接数据库
        if (ar1.succeeded()) {
          SqlConnection conn = ar1.result();
          // 查询
          conn.query("select id,name,age from user")
            .execute(ar2 -> {
              // 关闭连接
              conn.close();
              if (ar2.succeeded()) {
                // 将查询出的list遍历转对象
                List<JsonObject> userList = new ArrayList<>();
                ar2.result().forEach(user -> {
                  JsonObject userJson = new JsonObject();
                  userJson.put("id", user.getInteger("id"));
                  userJson.put("name", user.getValue("name"));
                  userJson.put("age", user.getInteger("age"));
                  userList.add(userJson);
                });
                // 成功的响应
                req.response().putHeader("content-type", "application/json")
                  .putHeader("charset", "utf-8")
                  .end(userList.toString());
              } else {
                // 失败的响应
                req.response().putHeader("content-type", "text/plain")
                  .end("查询失败:" + ar2.cause().toString());
              }
            });
        } else {
          System.err.println("数据库连接失败1:" + ar1.cause().getMessage());
        }
      });
    });

    // 路由相关-------------------------------------------------------------
    // 获取 /get?id=100
    router.get("/get").handler(req -> {
      String id = req.request().getParam("id");

      req.response()
        .putHeader("content-type", "application/json")
        .end(new JsonObject().put("data", "经典:" + id).toString());
    });

    // 获取 /get/200
    router.get("/get/:id").handler(req -> {
      String id = req.request().getParam("id");

      req.response()
        .putHeader("content-type", "application/json")
        .end(new JsonObject().put("data", "restFul:" + id).toString());
    });

    // 获取 post请求x-www 请求体参数
    router.post("/post/x3w").handler(req -> {
      String name = req.request().getFormAttribute("name");
      req.response()
        .putHeader("content-type", "application/json")
        .end(new JsonObject().put("data", "x-www:" + name).toString());
    });

    // 获取 post请求json 请求体参数
    router.post("/post/json").handler(req -> {
      JsonObject bodyAsJson = req.getBodyAsJson();
      int age = (int) bodyAsJson.getValue("age");
      req.response()
        .putHeader("content-type", "application/json")
        .end(new JsonObject().put("data", "json:" + age).toString());
    });

    //将路由和HttpServer绑定
    vertx.createHttpServer().requestHandler(router).listen(8888, http -> {
      if (http.succeeded()) {
        startPromise.complete();
      } else {
        startPromise.fail(http.cause());
      }
    });
  }
}
