#!/bin/bash
 ../bin/cryptogen generate --config=./cryptogen.yaml
 ../bin/configtxgen -profile ThreeOrgsOrdererGenesis -outputBlock genesis.block
 ../bin/configtxgen -profile ThreeOrgsChannel -outputCreateChannelTx workflowchannel.tx -channelID workflowchannel
 ../bin/configtxgen -profile ThreeOrgsChannel -outputAnchorPeersUpdate Org1MSPanchors.tx -channelID workflowchannel -asOrg Org1MSP
 ../bin/configtxgen -profile ThreeOrgsChannel -outputAnchorPeersUpdate Org2MSPanchors.tx -channelID workflowchannel -asOrg Org2MSP
 ../bin/configtxgen -profile ThreeOrgsChannel -outputAnchorPeersUpdate Org3MSPanchors.tx -channelID workflowchannel -asOrg Org3MSP

