#!/bin/bash -e
#
# Copyright IBM Corp. All Rights Reserved.
#
# SPDX-License-Identifier: Apache-2.0
#

#set -x

CHANNEL_NAME=$1
TOTAL_CHANNELS=$2
: ${CHANNEL_NAME:="mychannel"}
: ${TOTAL_CHANNELS:="2"}
#echo "Using CHANNEL_NAME prefix as $CHANNEL_NAME"

ROOT_DIR=$PWD
export FABRIC_CFG_PATH=$ROOT_DIR/channel
#ARCH=$(uname -s)

function generateCerts() {
	CRYPTOGEN=$ROOT_DIR/bin/cryptogen
	echo
	echo "##########################################################"
	echo "##### Generate certificates using cryptogen tool #########"
	echo "##########################################################"
  cd $FABRIC_CFG_PATH
  rm -Rf crypto-config
	$CRYPTOGEN generate --config=$FABRIC_CFG_PATH/cryptogen.yaml
	echo
}

## docker-compose template to replace private key file names with constants
function replacePrivateKey() {

  cd $ROOT_DIR
#  cp docker-compose-template.yaml docker-compose.yaml
#	cp network-config-template.yaml network-config.yaml

	cd $FABRIC_CFG_PATH/crypto-config/peerOrganizations/org1.example.com/ca/
	PRIV_KEY=$(ls *_sk)
	cd $ROOT_DIR
	sed -i "s/CA1_PRIVATE_KEY/${PRIV_KEY}/g" docker-compose.yaml
	cd $FABRIC_CFG_PATH/crypto-config/peerOrganizations/org2.example.com/ca/
	PRIV_KEY=$(ls *_sk)
	cd $ROOT_DIR
	sed -i "s/CA2_PRIVATE_KEY/${PRIV_KEY}/g" docker-compose.yaml

	cd $FABRIC_CFG_PATH/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/keystore/
	ADMIN_KEY=$(ls *_sk)
	#echo $ADMIN_KEY
	cd $ROOT_DIR
	sed -i "s/GFE_ADMIN_KEY/${ADMIN_KEY}/g" network-config.yaml
	cd $FABRIC_CFG_PATH/crypto-config/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp/keystore/
	ADMIN_KEY=$(ls *_sk)
	#echo $ADMIN_KEY
	cd $ROOT_DIR
	sed -i "s/DEKE_ADMIN_KEY/${ADMIN_KEY}/g" network-config.yaml
}

## Generate orderer genesis block , channel configuration transaction and anchor peer update transactions
function generateChannelArtifacts() {

	CONFIGTXGEN=$ROOT_DIR/bin/configtxgen


	echo "##########################################################"
	echo "#########  Generating Orderer Genesis block ##############"
	echo "##########################################################"
	# Note: For some unknown reason (at least for now) the block file can't be
	# named orderer.genesis.block or the orderer will fail to launch!
	$CONFIGTXGEN -profile TwoOrgsOrdererGenesis -outputBlock ./channel/genesis.block

	#for ((i = 1; i <= $TOTAL_CHANNELS; i = $i + 1)); do
		echo
		echo "#################################################################"
		echo "### Generating channel configuration transaction '$CHANNEL_NAME$.tx' ###"
		echo "#################################################################"
		$CONFIGTXGEN -profile TwoOrgsChannel -outputCreateChannelTx ./channel/$CHANNEL_NAME.tx -channelID $CHANNEL_NAME
		echo
	#done
}

generateCerts
replacePrivateKey
generateChannelArtifacts
cd ..
