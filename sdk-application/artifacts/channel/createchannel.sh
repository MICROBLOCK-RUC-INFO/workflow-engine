#!/bin/bash
CHANNEL_NAME="workflowchannel"
peer channel create -o orderer0.workflow-com:7050 -c $CHANNEL_NAME -f /opt/gopath/src/github.com/hyperledger/fabric/peer/channel-artifacts/workflowchannel.tx --tls --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/workflow-com/orderers/orderer0.workflow-com/msp/tlscacerts/tlsca.workflow-com-cert.pem

