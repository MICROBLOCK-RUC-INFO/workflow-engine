#!/bin/bash
service mysqld start && \
. /etc/profile && \
. /usr/local/scripts/wfServiceScripts/activitiService.sh && \
cd /home/sunweekstar/redis && \
redis-server redis.conf && \
# sed -i '313c JAVA_OPTS="$JAVA_OPTS -Xms512m -Xmx6144m"' /usr/local/apache-tomcat-8.5.64/bin/catalina.sh && \
# . /usr/local/apache-tomcat-8.5.64/bin/catalina.sh start && \
. /usr/local/scripts/ipfsScripts/ipfsStart.sh && \
. /usr/local/scripts/nacosScripts/createNacosConfig.sh && \
peer node start
