# jgroups-demo

jgroups-demo is a jgroups sample code build on top of spring boot.

### build
```bash
mvn clean package
```

### start the demo app on local machine
```bash
mvn spring-boot:run -Djava.net.preferIPv4Stack=true -Dserver.port=8081

mvn spring-boot:run -Djava.net.preferIPv4Stack=true -Dserver.port=8082

mvn spring-boot:run -Djava.net.preferIPv4Stack=true -Dserver.port=8083

```

### test on local machine
```bash
http --timeout=3600 POST :8081/send 'input=hello world'
```