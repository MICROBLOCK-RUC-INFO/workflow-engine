#!/bin/bash
#
# Copyright IBM Corp. All Rights Reserved.
#
# SPDX-License-Identifier: Apache-2.0
#
username=$1
jq --version > /dev/null 2>&1
if [ $? -ne 0 ]; then
	echo "Please Install 'jq' https://stedolan.github.io/jq/ to execute this script"
	echo
	exit 1
fi

starttime=$(date +%s)

# Print the usage message
function printHelp () {
  echo "Usage: "
  echo "  ./testAPIs.sh -l golang|node"
  echo "    -l <language> - chaincode language (defaults to \"golang\")"
}
# Language defaults to "golang"
LANGUAGE="golang"

# Parse commandline args
while getopts "h?l:" opt; do
  case "$opt" in
    h|\?)
      printHelp
      exit 0
    ;;
    l)  LANGUAGE=$OPTARG
    ;;
  esac
done

##set chaincode path
function setChaincodePath(){
	LANGUAGE=`echo "$LANGUAGE" | tr '[:upper:]' '[:lower:]'`
	case "$LANGUAGE" in
		"golang")
		CC_SRC_PATH="github.com/wfscc"
		;;
		"node")
		CC_SRC_PATH="$PWD/artifacts/src/github.com/example_cc/node"
		;;
		*) printf "\n ------ Language $LANGUAGE is not supported yet ------\n"$
		exit 1
	esac
}

setChaincodePath

echo "POST request Enroll on Org1  ..."
echo
ORG1_TOKEN=$(curl -s -X POST \
 http://localhost:4000/users \
 -H "content-type: application/x-www-form-urlencoded" \
 -d 'username=Jim&orgName=Org1')
echo $ORG1_TOKEN
export ORG1_TOKEN=$(echo $ORG1_TOKEN | jq ".token" | sed "s/\"//g")
echo $ORG1_TOKEN>token.txt
echo "ORG1 token is $ORG1_TOKEN"
echo
echo "POST request Enroll on Org2 ..."
echo
ORG2_TOKEN=$(curl -s -X POST \
 http://localhost:4000/users \
 -H "content-type: application/x-www-form-urlencoded" \
 -d 'username=Barry&orgName=Org2')
echo $ORG2_TOKEN
export ORG2_TOKEN=$(echo $ORG2_TOKEN | jq ".token" | sed "s/\"//g")
echo
echo "ORG2 token is $ORG2_TOKEN"
echo


echo "POST request Enroll on Org3 ..."
echo
ORG3_TOKEN=$(curl -s -X POST \
 http://localhost:4000/users \
 -H "content-type: application/x-www-form-urlencoded" \
 -d 'username=Sun&orgName=Org3')
echo $ORG3_TOKEN
export ORG3_TOKEN=$(echo $ORG3_TOKEN | jq ".token" | sed "s/\"//g")
echo
echo "ORG3 token is $ORG3_TOKEN"

echo
echo "POST request Create channel  ..."
echo
curl -s -X POST \
  http://localhost:4000/channels \
  -H "authorization: Bearer $ORG1_TOKEN" \
  -H "content-type: application/json" \
  -d '{
	"channelName":"workflowchannel",
	"channelConfigPath":"../artifacts/channel/workflowchannel.tx"
}'
echo
echo





sleep 20
echo "POST request Join channel on Org1"
echo
curl -s -X POST \
  http://localhost:4000/channels/workflowchannel/peers \
  -H "authorization: Bearer $ORG1_TOKEN" \
  -H "content-type: application/json" \
  -d '{
	"peers": ["peer0.org1.workflow.com"]
}'
echo
echo



echo "POST request Join channel on Org2"
echo
curl -s -X POST \
 http://localhost:4000/channels/workflowchannel/peers \
 -H "authorization: Bearer $ORG2_TOKEN" \
 -H "content-type: application/json" \
 -d '{
	"peers": ["peer0.org2.workflow.com"]
}'
echo
echo

echo "POST request Join channel on Org3"
echo
curl -s -X POST \
 http://localhost:4000/channels/workflowchannel/peers \
 -H "authorization: Bearer $ORG3_TOKEN" \
 -H "content-type: application/json" \
 -d '{
	"peers": ["peer0.org3.workflow.com"]
}'
echo
echo

echo "POST request Update anchor peers on Org1"
echo
curl -s -X POST \
  http://localhost:4000/channels/workflowchannel/anchorpeers \
  -H "authorization: Bearer $ORG1_TOKEN" \
  -H "content-type: application/json" \
  -d '{
	"configUpdatePath":"../artifacts/channel/Org1MSPanchors.tx"
}'
echo
echo

echo "POST request Update anchor peers on Org2"
echo
curl -s -X POST \
 http://localhost:4000/channels/workflowchannel/anchorpeers \
 -H "authorization: Bearer $ORG2_TOKEN" \
 -H "content-type: application/json" \
 -d '{
	"configUpdatePath":"../artifacts/channel/Org2MSPanchors.tx"
}'
echo
echo

echo "POST request Update anchor peers on Org3"
echo
curl -s -X POST \
 http://localhost:4000/channels/workflowchannel/anchorpeers \
 -H "authorization: Bearer $ORG3_TOKEN" \
 -H "content-type: application/json" \
 -d '{
	"configUpdatePath":"../artifacts/channel/Org3MSPanchors.tx"
}'
echo
echo

# echo "POST Install chaincode on Org1"
# echo
# curl -s -X POST \
#   http://localhost:4000/chaincodes \
#   -H "authorization: Bearer $ORG1_TOKEN" \
#   -H "content-type: application/json" \
#   -d "{
# 	\"peers\": [\"peer0.org1.workflow.com\"],
# 	\"chaincodeName\":\"nacos\",
# 	\"chaincodePath\":\"$CC_SRC_PATH\",
# 	\"chaincodeType\": \"$LANGUAGE\",
# 	\"chaincodeVersion\":\"v0\"
# }"
# echo
# echo

# echo "POST Install chaincode on Org2"
# echo
# curl -s -X POST \
#  http://localhost:4000/chaincodes \
#  -H "authorization: Bearer $ORG2_TOKEN" \
#  -H "content-type: application/json" \
#  -d "{
# 	\"peers\": [\"peer0.org2.workflow.com\"],
# 	\"chaincodeName\":\"nacos\",
# 	\"chaincodePath\":\"$CC_SRC_PATH\",
# 	\"chaincodeType\": \"$LANGUAGE\",
# 	\"chaincodeVersion\":\"v0\"
# }"
# echo
# echo

# echo "POST Install chaincode on Org3"
# echo
# curl -s -X POST \
#  http://localhost:4000/chaincodes \
#  -H "authorization: Bearer $ORG3_TOKEN" \
#  -H "content-type: application/json" \
#  -d "{
# 	\"peers\": [\"peer0.org3.workflow.com\"],
# 	\"chaincodeName\":\"nacos\",
# 	\"chaincodePath\":\"$CC_SRC_PATH\",
# 	\"chaincodeType\": \"$LANGUAGE\",
# 	\"chaincodeVersion\":\"v0\"
# }"
# echo
# echo

# echo "POST instantiate chaincode on Org1"
# echo
# curl -s -X POST \
#   http://localhost:4000/channels/workflowchannel/chaincodes \
#   -H "authorization: Bearer $ORG1_TOKEN" \
#   -H "content-type: application/json" \
#   -d "{
# 	\"chaincodeName\":\"nacos\",
# 	\"chaincodeVersion\":\"v0\",
# 	\"chaincodeType\": \"$LANGUAGE\",
# 	\"args\":[]
# }"
# echo
# echo



echo "GET query Installed chaincodes"
echo
curl -s -X GET \
  "http://localhost:4000/chaincodes?peer=peer0.org1.workflow.com" \
  -H "authorization: Bearer $ORG1_TOKEN" \
  -H "content-type: application/json"
echo
echo

echo "GET query Instantiated chaincodes"
echo
curl -s -X GET \
  "http://localhost:4000/channels/workflowchannel/chaincodes?peer=peer0.org1.workflow.com" \
  -H "authorization: Bearer $ORG1_TOKEN" \
  -H "content-type: application/json"
echo
echo

echo "GET query Channels"
echo
curl -s -X GET \
  "http://localhost:4000/channels?peer=peer0.org1.workflow.com" \
  -H "authorization: Bearer $ORG1_TOKEN" \
  -H "content-type: application/json"
echo
echo


echo "Total execution time : $(($(date +%s)-starttime)) secs ..."
