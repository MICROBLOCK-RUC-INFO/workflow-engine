#!/bin/bash
ipfs init && \
cp /root/swarm.key /root/.ipfs && \
ipfs bootstrap rm --all && \
nohup ipfs daemon & > ipfsLog.out
