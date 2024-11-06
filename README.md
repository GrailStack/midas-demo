# mias-demo

## midas-demo演示应用

-javaagent:/Users/xujin/agent/midas-stick.jar=app.id=midas-demo-new-consumer,server.address=http://127.0.0.1:8080

-javaagent:/Users/xujin/agent/midas-stick.jar=app.id=midas-demo-new-provider,server.address=http://127.0.0.1:8080

-javaagent:/Users/xujin/agent/midas-stick.jar=app.id=midas-demo-old-consumer,server.address=http://127.0.0.1:8080

-javaagent:/Users/xujin/agent/midas-stick.jar=app.id=midas-demo-old-provider,server.address=http://127.0.0.1:8080

## JDK 17 支持
新增vm配置
```
-javaagent:/Users/xxx/agent/midas-stick.jar=app.id=midas-demo-new-provider,server.address=http://127.0.0.1:8080 --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.net=ALL-UNNAMED --add-opens java.base/java.math=ALL-UNNAMED --add-opens java.base/sun.net.www=ALL-UNNAMED --add-opens java.base/sun.net.www.protocol.http=ALL-UNNAMED
```