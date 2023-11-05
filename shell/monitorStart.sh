#!/bin/bash
cd /usr/local/monitor && \
java -jar /usr/local/monitor/monitor.jar --server.port=8999 > /usr/local/monitor/log.out
