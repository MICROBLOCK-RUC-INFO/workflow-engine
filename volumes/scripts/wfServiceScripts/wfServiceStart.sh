#!/bin/bash
cd /usr/local/monitor && \
java -jar /usr/local/monitor/wfService.jar --server.port=8999 > /usr/local/monitor/log.out
