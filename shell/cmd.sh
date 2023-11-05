#!/bin/bash
service mysqld start && \
cd /home/sunweekstar/redis && \
redis-server redis.conf && \
sed -i '313c JAVA_OPTS="$JAVA_OPTS -Xms512m -Xmx6144m"' /usr/local/apache-tomcat-8.5.64/bin/catalina.sh && \
. /usr/local/apache-tomcat-8.5.64/bin/catalina.sh start && \
. /usr/local/shell/ipfsStart.sh && \
. /nacos/createNacosConfig.sh && \
peer node start
