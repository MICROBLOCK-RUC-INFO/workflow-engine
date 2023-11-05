package wfscc

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"net/url"
	"strings"

	"github.com/hyperledger/fabric/common/flogging"
	// "github.com/hyperledger/fabric/core/aclmgmt"
	"github.com/hyperledger/fabric/core/chaincode/shim"
	pb "github.com/hyperledger/fabric/protos/peer"
)

const (
	TESTDEPLOY        = "testdeploy"
	TESTINSTANTIATE   = "testinstantiate"
	TESTCOMPLETE      = "testcomplete"
	QUERYBPMN         = "queryBpmn"
	QUERYTASK         = "queryTask"
	QUERYTASKS        = "queryTasks"
	QUERYBUSINESSDATA = "querybusinessdata"
	DEPLOYMENTQUERY   = "front_deployment_query"
	INSTANCEQUERY     = "front_instance_query"
	TODOTASKQUERY     = "todotask_query"
)

type WorkflowSysCC struct {
	Port       string
	WfName     string
	WfInstance string
	WfTasks    string
	WfHistory  string
	// aclProvider aclmgmt.ACLProvider
}

var wfscclogger = flogging.MustGetLogger("wfscc")

type BpmnFile struct {
	Key         string `json:"key"`
	FileName    string `json:"fileName"`
	FileContent string `json:"fileContent"`
	FormDict    string `json:"formDict"` //字典，关联的外置表单
}
type BpmnInstance struct {
	Key          string `json:"key"`
	InstanceUser string `json:"instanceUser"`
	Operator     string `json:"operator"`
	FileName     string `json:"fileName"`
	TaskList     string `json:"taskList"` //activiti返回的新任务，包含内置表单和外置表单名
	Operation    string `json:"operation"`
	State        string `json:"state"`
	Timestamp    string `json:"timestamp"`
}
type resultDTO struct {
	Code string `json:"code"`
	Data string `json:"data"`
	Msg  string `json:"msg"`
}

type InsRet struct {
	TaskList      string `json:"taskList"`
	State         string `json:"state"`
	OperationList string `json:operationList`
}

// func New(port string, aclProvider aclmgmt.ACLProvider) *WorkflowSysCC {
// 	return &WorkflowSysCC{
// 		Port:        "8080",
// 		aclProvider: aclProvider,
// 	}
// }
var logger = shim.NewLogger("wfscc")

func main() {
	err := shim.Start(new(WorkflowSysCC))
	if err != nil {
		logger.Errorf("Error starting Simple chaincode: %s", err)
	}
}

func (wfscc *WorkflowSysCC) Name() string              { return "wfscc" }
func (wfscc *WorkflowSysCC) Path() string              { return "github.com/hyperledger/fabric/core/scc/wfscc" }
func (wfscc *WorkflowSysCC) InitArgs() [][]byte        { return nil }
func (wfscc *WorkflowSysCC) Chaincode() shim.Chaincode { return wfscc }
func (wfscc *WorkflowSysCC) InvokableExternal() bool   { return true }
func (wfscc *WorkflowSysCC) InvokableCC2CC() bool      { return true }
func (wfscc *WorkflowSysCC) Enabled() bool             { return true }

func (wfscc *WorkflowSysCC) Init(stub shim.ChaincodeStubInterface) pb.Response {
	wfscclogger.Info("Init wfscc")
	return shim.Success(nil)
}

// Invoke implements lifecycle functions "deploy", "start", "stop", "upgrade".
// Deploy's arguments -  {[]byte("deploy"), []byte(<chainname>), <unmarshalled pb.ChaincodeDeploymentSpec>}
//
// Invoke also implements some query-like functions
// Get chaincode arguments -  {[]byte("getid"), []byte(<chainname>), []byte(<chaincodename>)}
func (wfscc *WorkflowSysCC) Invoke(stub shim.ChaincodeStubInterface) pb.Response {
	all_args := stub.GetArgs()
	if len(all_args) < 1 {
		return shim.Error(fmt.Sprintf("Incorrect number of arguments, %d", len(all_args)))
	}

	function, args := stub.GetFunctionAndParameters()

	// Handle ACL:
	// 1. get the signed proposal
	//sp, err := stub.GetSignedProposal()
	/*
		if err != nil {
			return shim.Error(fmt.Sprintf("Failed retrieving signed proposal on executing %s with error %s", function, err))
		}
	*/
	switch function {
	case DEPLOYMENTQUERY:
		result, err := httpGet("http://127.0.0.1:8080/wfEngine/front_deployment_query")
		if err != nil {
			return shim.Error(err.Error())
		}
		return shim.Success(result)

	case INSTANCEQUERY:
		result, err := httpGet("http://127.0.0.1:8080/wfEngine/front_instance_query")
		if err != nil {
			return shim.Error(err.Error())
		}
		return shim.Success(result)

	case TODOTASKQUERY:
		result, err := httpGet("http://127.0.0.1:8080/wfEngine/todotask_query")
		if err != nil {
			return shim.Error(err.Error())
		}
		return shim.Success(result)

	case TESTDEPLOY:
		var request = make(map[string]string)
		err := json.Unmarshal([]byte(args[0]), &request)
		if err != nil {
			return shim.Error(err.Error())
		}
		fileName := request["fileName"]
		fileContent := request["fileContent"]
		// prefix := "file"
		// wfName := strings.Split(args[0], ".")[0]
		// deployer := args[1]
		// timestamp := args[2]
		// fileContent := args[3]
		// formDict := args[4]
		// key_str := prefix + ":" + wfName + ":" + deployer + ":" + timestamp + ".bpmn"
		postValue := url.Values{
			"fileName":    {fileName},
			"fileContent": {fileContent},
		}
		// post_value := map[string]string{"fileName": fileName, "fileContent": fileContent}
		// post_json, err := json.Marshal(post_value)
		// if err != nil {
		// 	return shim.Error(err.Error())
		// }
		result, err := httpPostForm("http://127.0.0.1:8080/wfEngine/testwfDeploy", postValue)
		if err != nil {
			return shim.Error(err.Error())
		}
		// result, err := httpPostForm("http://172.88.0.5:8080/wfEngine/testwfDeploy", postValue)
		// if err != nil {
		// 	return shim.Error(err.Error())
		// }
		// var rdto resultDTO
		// err = json.Unmarshal([]byte(result), &rdto)
		// if err != nil {
		// 	return shim.Error(err.Error())
		// }
		// if rdto.Code == "500" {
		// 	return shim.Error(rdto.Data)
		// }
		// var key, _ = stub.CreateCompositeKey(prefix, []string{wfName, deployer, timestamp})
		// var bpmnFile = BpmnFile{Key: key_str, FileName: wfName, FileContent: fileContent, FormDict: formDict}
		// bpmnAsBytes, err := json.Marshal(bpmnFile)
		// if err != nil {
		// 	return shim.Error(err.Error())
		// }
		// err = stub.PutState(key, bpmnAsBytes)
		// if err != nil {
		// 	return shim.Error(err.Error())
		// }

		return shim.Success(result)
	case TESTINSTANTIATE:
		// if len(args) < 3 {
		// 	return shim.Error(fmt.Sprintf("Deploy incorrect number of arguments, %d", len(args)))
		// }
		var request = make(map[string]string)
		err := json.Unmarshal([]byte(args[0]), &request)
		if err != nil {
			return shim.Error(err.Error())
		}
		insName := request["insName"]
		fileName := request["fileName"]

		// prefix := "ins"
		// bpmnKey := args[0]
		// operator := args[1]
		// timestamp := args[2]
		// bpmnKeyList := strings.Split(bpmnKey, ":")
		// wfName := bpmnKeyList[1]
		// insPreKey := bpmnKey[5:]
		// key_str := prefix + ":" + insPreKey + ":" + operator + ":" + timestamp
		post_value := map[string]string{"fileName": fileName, "insName": insName}
		post_json, err := json.Marshal(post_value)
		if err != nil {
			return shim.Error(err.Error())
		}
		result, err := httpPostJson("http://127.0.0.1:8080/wfEngine/testwfInstance", post_json)
		if err != nil {
			return shim.Error(err.Error())
		}
		// var rdto resultDTO
		// err = json.Unmarshal([]byte(result), &rdto)
		// if err != nil {
		// 	return shim.Error(err.Error())
		// }
		// if rdto.Code == "500" {
		// 	return shim.Error(rdto.Data)
		// }
		// var insret InsRet
		// err = json.Unmarshal([]byte(rdto.Data), &insret)
		// if err != nil {
		// 	return shim.Error(err.Error())
		// }
		// var key, _ = stub.CreateCompositeKey(prefix, []string{bpmnKeyList[1], bpmnKeyList[2], bpmnKeyList[3], operator, timestamp})
		// if err != nil {
		// 	return shim.Error(err.Error())
		// }
		// var bpmnIns = BpmnInstance{Key: key_str, InstanceUser: operator, Operator: operator, FileName: wfName, TaskList: insret.TaskList, Operation: insret.OperationList, State: insret.State, Timestamp: timestamp}
		// bpmnAsBytes, err := json.Marshal(bpmnIns)
		// if err != nil {
		// 	return shim.Error(err.Error())
		// }
		// err = stub.PutState(key, bpmnAsBytes)
		// if err != nil {
		// 	return shim.Error(err.Error())
		// }

		return shim.Success(result)

	case QUERYBUSINESSDATA:
		var request = make(map[string]string)
		err := json.Unmarshal([]byte(args[0]), &request)
		if err != nil {
			return shim.Error(err.Error())
		}
		insName := request["insName"]
		result, err := stub.GetState(insName)
		return shim.Success(result)

	case TESTCOMPLETE:
		var request = make(map[string]string)
		err := json.Unmarshal([]byte(args[0]), &request)
		if err != nil {
			return shim.Error(err.Error())
		}
		insName := request["insName"]
		operation := request["operation"]
		formVal := request["formVal"]
		priString := request["priString"]
		cert := request["cert"]
		serviceArgs := request["serviceArgs"]
		business_data := request["business_data"]
		old_data, err := stub.GetState(insName)
		if old_data == nil {
			stub.PutState(insName, []byte(business_data)) //将业务数据存在区块链上
		} else {
			var business_data_map = make(map[string]string)
			err1 := json.Unmarshal([]byte(business_data), &business_data_map)
			if err1 != nil {
				return shim.Error(err1.Error())
			}
			var old_data_map = make(map[string]string)
			err2 := json.Unmarshal(old_data, &old_data_map)
			if err2 != nil {
				return shim.Error(err2.Error())
			}
			for k, v := range business_data_map {
				old_data_map[k] = v
			}
			new_data, err := json.Marshal(old_data_map)
			if err != nil {
				return shim.Error(err.Error())
			}
			stub.DelState(insName)
			stub.PutState(insName, new_data)
		} //更新链上的数据

		post_value := map[string]string{
			"insName":     insName,
			"operation":   operation,
			"formVal":     formVal,
			"priString":   priString,
			"cert":        cert,
			"serviceArgs": serviceArgs,
		}

		post_json, err := json.Marshal(post_value)
		if err != nil {
			return shim.Error(err.Error())
		}
		result, err := httpPostJson("http://127.0.0.1:8080/wfEngine/testwfComplete", post_json)
		if err != nil {
			return shim.Error(err.Error())
		}
		// var rdto resultDTO
		// err = json.Unmarshal([]byte(result), &rdto)
		// if err != nil {
		// 	return shim.Error(err.Error())
		// }
		// if rdto.Code == "500" {
		// 	return shim.Error(rdto.Data)
		// }
		// var insret InsRet
		// err = json.Unmarshal([]byte(rdto.Data), &insret)
		// if err != nil {
		// 	return shim.Error(err.Error())
		// }
		// var key, _ = stub.CreateCompositeKey(prefix, []string{insKeyList[1], insKeyList[2], insKeyList[3], insKeyList[4], insKeyList[5]})
		// if err != nil {
		// 	return shim.Error(err.Error())
		// }
		// var bpmnIns = BpmnInstance{Key: insAllKey, InstanceUser: insKeyList[4], FileName: wfName, TaskList: insret.TaskList, Operation: insret.OperationList, State: insret.State, Timestamp: timestamp}
		// bpmnAsBytes, err := json.Marshal(bpmnIns)
		// if err != nil {
		// 	return shim.Error(err.Error())
		// }
		// err = stub.PutState(key, bpmnAsBytes)
		// if err != nil {
		// 	return shim.Error(err.Error())
		// }

		return shim.Success(result)

	case QUERYBPMN:
		prefix := "file"
		bpmnKey := args[0]
		bpmnKeyList := strings.Split(bpmnKey, ":")
		wfName := bpmnKeyList[1]
		deployer := bpmnKeyList[2]
		timestamp := bpmnKeyList[3]
		resultsIterator, err := stub.GetStateByPartialCompositeKey(prefix, []string{wfName, deployer, timestamp})
		if err != nil {
			return shim.Error(err.Error())
		}
		defer resultsIterator.Close()

		var buffer bytes.Buffer
		bArrayMemberAlreadyWritten := false

		for resultsIterator.HasNext() {
			responseRange, err := resultsIterator.Next()
			if err != nil {
				return shim.Error(err.Error())
			}

			if bArrayMemberAlreadyWritten == true {
				break
			}
			buffer.WriteString(string(responseRange.Value))
			bArrayMemberAlreadyWritten = true
		}

		return shim.Success(buffer.Bytes())

	case QUERYTASK:
		if len(args) < 2 {
			return shim.Error(fmt.Sprintf("Deploy incorrect number of arguments, %d", len(args)))
		}
		prefix := "file"
		wfName := args[0]
		deployer := args[1]
		timeMap := args[2]
		key_str := prefix + ":" + wfName + ":" + deployer + ":" + timeMap
		var key, _ = stub.CreateCompositeKey(prefix, []string{wfName, deployer, timeMap})
		var bpmnFile = BpmnFile{Key: key_str, FileName: wfName, FileContent: deployer, FormDict: timeMap}
		bpmnAsBytes, err := json.Marshal(bpmnFile)
		if err != nil {
			return shim.Error(err.Error())
		}
		err = stub.PutState(key, bpmnAsBytes)
		if err != nil {
			return shim.Error(err.Error())
		}
		return shim.Success([]byte("key_str"))
	case QUERYTASKS:
		if len(args) < 2 {
			return shim.Error(fmt.Sprintf("Deploy incorrect number of arguments, %d", len(args)))
		}
		prefix := "file"
		wfName := args[0]
		deployer := args[1]
		timeMap := args[2]
		key_str := prefix + ":" + wfName + ":" + deployer + ":" + timeMap
		var key, _ = stub.CreateCompositeKey(prefix, []string{wfName, deployer, timeMap})
		var bpmnFile = BpmnFile{Key: key_str, FileName: wfName, FileContent: deployer, FormDict: timeMap}
		bpmnAsBytes, err := json.Marshal(bpmnFile)
		if err != nil {
			return shim.Error(err.Error())
		}
		err = stub.PutState(key, bpmnAsBytes)
		if err != nil {
			return shim.Error(err.Error())
		}
		return shim.Success([]byte("key_str"))
	}

	return shim.Error("Invalid function")
}

func httpPostJson(url string, request_json []byte) ([]byte, error) {
	req, _ := http.NewRequest("POST", url, bytes.NewBuffer(request_json))
	req.Header.Set("Content-Type", "application/json")
	client := &http.Client{}
	response, err := client.Do(req)
	if err != nil {
		return nil, err
	}
	defer response.Body.Close()
	response_body, err := ioutil.ReadAll(response.Body)
	return response_body, nil
}

func httpPostForm(url string, postValue url.Values) ([]byte, error) {
	resp, err := http.PostForm(url, postValue)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()
	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return nil, err
	}
	return body, nil
}

func httpGet(url string) ([]byte, error) {
	response, err := http.Get(url)
	if err != nil {
		return nil, err
	}
	defer response.Body.Close()
	body, err := ioutil.ReadAll(response.Body)
	if err != nil {
		return nil, err
	}
	return body, nil
}
