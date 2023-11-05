#!/bin/bash
cd /nacos && \
mkdir logs
cd /nacos/target && \
java  -server -XX:-OmitStackTraceInFastThrow -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/nacos/logs/java_heapdump.hprof -XX:-UseLargePages --class-path=/usr/local/jdk-11.0.13/jre/lib/ext:/usr/local/jdk-11.0.13/lib/ext -Xlog:gc:/nacos/logs/nacos_gc.log -verbose:gc -XX:+PrintGCDetails -Dloader.path=/nacos/plugins/health,/nacos/plugins/cmdb,/nacos/plugins/mysql -Dnacos.home=/nacos -jar /nacos/target/nacos-server.jar  --spring.config.location=classpath:/,classpath:/config/,file:./,file:./config/,file:/nacos/conf/ --logging.config=/nacos/conf/nacos-logback.xml --server.max-http-header-size=524288
