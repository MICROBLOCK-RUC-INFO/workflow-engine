package main

import (
    "time"
    "io/ioutil"
    "net/http"
    "bytes"
    "encoding/json"
    "fmt"
    "github.com/hyperledger/fabric/core/chaincode/shim"
    pb "github.com/hyperledger/fabric/protos/peer"
    "regexp"
    "strconv"
//"reflect"
    "strings"
)

var GlobalCacheMap = map[string]string{}

//数据格式
type SimpleChaincode struct {
}

//将utf-8八进制转为可显示的汉字编码
func convertOctonaryUtf8(in string) string {
    s := []byte(in)
    reg := regexp.MustCompile(`\\[0-7]{3}`)

    out := reg.ReplaceAllFunc(s,
        func(b []byte) []byte {
            i, _ := strconv.ParseInt(string(b[1:]), 8, 0)
            return []byte{byte(i)}
        })
    return string(out)
}


// =========================================
//       Init - initializes chaincode
// =========================================
func (t *SimpleChaincode) Init(stub shim.ChaincodeStubInterface) pb.Response {
    return shim.Success(nil)
}

// ======================================================
//       Invoke - Our entry point for Invocations
// ======================================================
func (t *SimpleChaincode) Invoke(stub shim.ChaincodeStubInterface) pb.Response {
    function, args := stub.GetFunctionAndParameters()
    fmt.Println("invoke is running " + function)
    if function == "QueryByKey" {
        return t.QueryByKey(stub, args)
    } else if function == "HistoryQuery" {
        return t.HistoryQuery(stub, args)
    } else if function == "RangeQuery" {
        return t.RangeQuery(stub, args)
    } else if function == "RichQuery" {
        return t.RichQuery(stub, args)
    } else if function == "Delete" {
        return t.Delete(stub, args)
    } else if function == "Put" {
        return t.Put(stub, args)
    } else if function == "InvokeTrace" {
        return t.InvokeTrace(stub, args)
    } else if function == "QueryByServiceName" {
        return t.QueryByServiceName(stub, args)
    } else if function == "QueryByServiceNameAndTime" {
        return t.QueryByServiceNameAndTime(stub, args)
    } else if function == "QueryByTime" {
        return t.QueryByTime(stub, args)
    } else {
        return shim.Error("Error func name!")
    }

}

//通过Key值查询
func (t *SimpleChaincode)QueryByKey(stub shim.ChaincodeStubInterface, args []string) pb.Response {
    if len(args) !=1 {
        return shim.Error("Incorrect arguments. Expecting a key and a value")
    }
    key := args[0]
    bstatus, err := stub.GetState(key)
    if err != nil||bstatus==nil {
        return shim.Error("Query form status fail, form number:" + key)
    }

    return shim.Success([]byte(bstatus))
}

//富查询
func (t *SimpleChaincode)RichQuery(stub shim.ChaincodeStubInterface, args []string) pb.Response {
    if len(args) !=4 {
        return shim.Error("Incorrect arguments. Expecting a key and a value")
    }
    serviceName := args[0]
    serviceAddress:=args[1]
    time:=args[2]
    monitorAddress:=args[3]
    queryString := fmt.Sprintf(`{"selector":{"_id":{"$regex":"springcloud.monitor.data.%s.%s@@%s@@%s"}}}`, serviceName,serviceAddress,time,monitorAddress)
    resultsIterator, err := stub.GetQueryResult(queryString)
    if err != nil {
        return shim.Error("Rich query failed"+queryString)
    }
    res,err:=getListResult(resultsIterator)
    if err!=nil{
        return shim.Error("getListResult failed")
    }
    return shim.Success(res)
}

//通过服务名查询，具体的查询语法可以参看mongo query
func (t *SimpleChaincode)QueryByServiceName(stub shim.ChaincodeStubInterface, args []string) pb.Response {
    serviceName := args[0]
    queryString := fmt.Sprintf(`{"selector":{"serviceName": "%s"}}`, serviceName)
    resultsIterator, err := stub.GetQueryResult(queryString)
    if err != nil {
        return shim.Error("Rich query failed"+queryString)
    }
    res,err:=getListResult(resultsIterator)
    if err!=nil{
        return shim.Error("getListResult failed")
    }
    return shim.Success(res)
}

//通过服务名和时间查询
func (t *SimpleChaincode)QueryByServiceNameAndTime(stub shim.ChaincodeStubInterface, args []string) pb.Response {
    serviceName := args[0]
    startTime := args[1]
    endTime := args[2]
    queryString := fmt.Sprintf(`{"selector":{"serviceName": "%s","startTime":{"$gt": "%s","$lt": "%s"}}}`, serviceName, startTime, endTime)
    resultsIterator, err := stub.GetQueryResult(queryString)
    if err != nil {
        return shim.Error("Rich query failed"+queryString)
    }
    res,err:=getListResult(resultsIterator)
    if err!=nil{
        return shim.Error("getListResult failed")
    }
    return shim.Success(res)
}

//通过服务名查询
func (t *SimpleChaincode)QueryByTime(stub shim.ChaincodeStubInterface, args []string) pb.Response {
    startTime := args[0]
    endTime := args[1]
    queryString := fmt.Sprintf(`{"selector":{"startTime":{"$gt": "%s","$lt": "%s"}}}`, startTime, endTime)
    resultsIterator, err := stub.GetQueryResult(queryString)
    if err != nil {
        return shim.Error("Rich query failed"+queryString)
    }
    res,err:=getListResult(resultsIterator)
    if err!=nil{
        return shim.Error("getListResult failed")
    }
    return shim.Success(res)
}

//历史数据查询
func (t *SimpleChaincode) HistoryQuery(stub shim.ChaincodeStubInterface, args []string) pb.Response{
    if len(args) !=1 {
        return shim.Error("Incorrect arguments. Expecting a key and a value")
    }
    key:=args[0]
    it,err:= stub.GetHistoryForKey(key)
    if err!=nil{
        return shim.Error(err.Error())
    }
    var result,_= getHistoryListResult(it)
    return shim.Success(result)
}

func getHistoryListResult(resultsIterator shim.HistoryQueryIteratorInterface) ([]byte,error){
    defer resultsIterator.Close()
    // buffer is a JSON array containing QueryRecords
    var buffer bytes.Buffer
    buffer.WriteString("[")
    bArrayMemberAlreadyWritten := false
    for resultsIterator.HasNext() {
        queryResponse, err := resultsIterator.Next()
        if err != nil {
            return nil, err
        }
        // Add a comma before array members, suppress it for the first array member
        if bArrayMemberAlreadyWritten == true {
            buffer.WriteString(",")
        }
        item,_:= json.Marshal( queryResponse)
        buffer.Write(item)
        bArrayMemberAlreadyWritten = true
    }
    buffer.WriteString("]")
    fmt.Printf("queryResult:\n%s\n", buffer.String())
    return buffer.Bytes(), nil
}


//范围查询
func (t *SimpleChaincode) RangeQuery(stub shim.ChaincodeStubInterface, args []string) pb.Response{
    resultsIterator,err:= stub.GetStateByRange(args[0],args[1])
    if err!=nil{
        return shim.Error("Query by Range failed")
    }
    res,err:=getListResult(resultsIterator)
    if err!=nil{
        return shim.Error("getListResult failed")
    }
    return shim.Success(res)
}

func getListResult(resultsIterator shim.StateQueryIteratorInterface) ([]byte,error){
    defer resultsIterator.Close()
    // buffer is a JSON array containing QueryRecords
    var buffer bytes.Buffer
    buffer.WriteString("[")
    bArrayMemberAlreadyWritten := false
    for resultsIterator.HasNext() {
        queryResponse, err := resultsIterator.Next()
        if err != nil {
            return nil, err
        }
        // Add a comma before array members, suppress it for the first array member
        if bArrayMemberAlreadyWritten == true {
            buffer.WriteString(",")
        }
        buffer.WriteString("{\"Key\":")
        buffer.WriteString("\"")
        buffer.WriteString(queryResponse.Key)
        buffer.WriteString("\"")
        buffer.WriteString(", \"Record\":")
        // Record is a JSON object, so we write as-is
        buffer.WriteString(string(queryResponse.Value))
        buffer.WriteString("}")
        bArrayMemberAlreadyWritten = true
    }
    buffer.WriteString("]")
    fmt.Printf("queryResult:\n%s\n", buffer.String())
    return buffer.Bytes(), nil
}

//上链
func (t *SimpleChaincode)Put(stub shim.ChaincodeStubInterface, args []string) pb.Response{
    if len(args) != 2 {
        return shim.Error("Incorrect arguments. Expecting a key and a value")
    }
    key:=args[0]
    err := stub.PutState(key, []byte(args[1]))
    if err != nil {
        return shim.Error("Failed to set asset: %s"+ args[0])
    }
    return shim.Success([]byte("put "+key+" success"))
}


//执行链路调用
func (t *SimpleChaincode)InvokeTrace(stub shim.ChaincodeStubInterface, args []string) pb.Response{

    // 分割链路中的每次调用    
	services := strings.Split(args[0], "##")
        for i:=0;i<len(services);i++ {
            // 对于一次调用，分割出调用者，服务名和调用路径
            arr := strings.Split(services[i],"@@")
            invoker := arr[0]
            serviceName := arr[1]
            uri:=arr[2]

            // 若缓存中有该服务的实例的ip和端口信息，则直接发送http请求进行调用
            if ipaddr, ok := GlobalCacheMap[serviceName]; ok {
				startTime := time.Now().UnixNano()
            	url := "http://"+ipaddr+uri
				res,_ :=http.Get(url)
				defer res.Body.Close()
				endTime := time.Now().UnixNano()
				body,_ := ioutil.ReadAll(res.Body)
                // 上链的键为 服务名@@调用开始时间
				key := serviceName + "@@" + strconv.Itoa(int(startTime))
				// 准备上链的值
                value :=make(map[string]interface{})
                value["invoker"] = invoker //调用者
                value["serviceName"] = serviceName //服务名
                value["url"] = url //路径
                value["startTime"] = strconv.Itoa(int(startTime)) // 开始时间
                value["endTime"] = strconv.Itoa(int(endTime)) //结束时间
                valuejson,err :=json.Marshal(value) //将map序列化为byte数组
                if err!=nil{
                    return shim.Error("Marshal failed"+err.Error())
                }
                 // 上链
                err = stub.PutState(key, valuejson)
    				if err != nil {
        				return shim.Error("Failed to set asset: %s"+ args[0])
    				}
				fmt.Println(body)
				// return shim.Success([]byte(key+"PutState Success"))
            } else {

                // 若缓存中没有该服务的信息，则上链进行查询，拼装出该服务的一个实例的ip+prot
            	bstatus, err := stub.GetState("com.alibaba.nacos.naming.iplist.public##DEFAULT_GROUP@@" + serviceName)
                if err != nil||bstatus==nil {
                	return shim.Error("Query form status fail, form number:" + serviceName)
                }
				var ext_de_json map[string]interface{}
				if err := json.Unmarshal(bstatus, &ext_de_json); err!=nil {
					return shim.Error("Json Parse Error")
				}
				ip := ext_de_json["value"].(map[string]interface{})["instanceList"].([]interface{})[0].(map[string]interface{})["ip"].(string)
				port := ext_de_json["value"].(map[string]interface{})["instanceList"].([]interface{})[0].(map[string]interface{})["port"].(float64)
				ipaddr:=ip+":"+strconv.Itoa(int(port))

                // 将查询结果设置进缓存中
				GlobalCacheMap[serviceName] = ipaddr
				
                // 同上、调用服务及上链
                startTime := time.Now().UnixNano()
            	url := "http://"+ipaddr+uri
				res, _ :=http.Get(url)
				defer res.Body.Close()
				endTime := time.Now().UnixNano()
				body,_ := ioutil.ReadAll(res.Body)
				key := serviceName + "@@" + strconv.Itoa(int(startTime))
				// value := invoker + "@@" + serviceName + "@@" + url + "@@" + strconv.Itoa(int(startTime)) + "@@" + strconv.Itoa(int(endTime))
               	value :=make(map[string]interface{})
                value["invoker"] = invoker
                value["serviceName"] = serviceName
                value["url"] = url
                value["startTime"] = strconv.Itoa(int(startTime))
                value["endTime"] = strconv.Itoa(int(endTime))
                valuejson,err :=json.Marshal(value)
                if err!=nil{
                    return shim.Error("Marshal failed"+err.Error())
                }
                err = stub.PutState(key, valuejson)
    				if err != nil {
        				return shim.Error("Failed to set asset: %s"+ args[0])
    				}
				fmt.Println(body)
				//return shim.Success([]byte(key+" PutState Success"))
            }
        }
        return shim.Success([]byte("success"))
	
}


//删除数据
func (t *SimpleChaincode)Delete(stub shim.ChaincodeStubInterface, args []string) pb.Response{
    key:=args[0]
    err:= stub.DelState(key)
    if err != nil {
    return shim.Error("Failed to delete Student from DB, key is: "+key)
    }
    return shim.Success([]byte("Delete Success,Key is: "+key))
}
//     Main
// ============
func main() {
    err := shim.Start(new(SimpleChaincode))
    if err != nil {
        fmt.Printf("Error starting Contract chaincode: %s", err)
    }
}




