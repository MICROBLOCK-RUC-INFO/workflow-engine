import argparse
import io
import os
import subprocess
import time
import requests
import json
import base64
import hashlib
from cryptography.hazmat.primitives.asymmetric import ec
from cryptography.hazmat.primitives import serialization, hashes
from cryptography.hazmat.backends import default_backend

def parseCmd() ->argparse.ArgumentParser:
    # 创建 ArgumentParser 对象
    parser = argparse.ArgumentParser(description="这是一个示例脚本，用于演示如何使用 argparse 解析命令行参数")

    fcnParsers = parser.add_subparsers(dest='command', help='功能')
    bpmnParser=fcnParsers.add_parser("bpmn",help="执行bpmn的部署,实例化与执行")

    serviceParser=fcnParsers.add_parser("service",help="服务绑定，注册，查询")

    orgParser=fcnParsers.add_parser('org',help='负责组织的注册等')

    sigParser=fcnParsers.add_parser("sign",help="数字签名工具")
    startParser=fcnParsers.add_parser("start",help="启动容器内的脚本")
    #queryParser=fcnParsers.add_parser("query",help="各种查询")
    generaterParser=fcnParsers.add_parser('generate',help='用于生成比如服务绑定数据等文件')

    subGeneratorParsers=generaterParser.add_subparsers(dest='generateCmd',help='生成服务注册元数据文件:smd,生成服务绑定元数据文件:sbd')
    smdGenaratorParser= subGeneratorParsers.add_parser('smd',help='服务注册元数据')
    smdGenaratorParser.add_argument('-ip','--ip',type=str,help="服务ip地址")
    smdGenaratorParser.add_argument('-p','--port',type=str,help='服务端口')
    smdGenaratorParser.add_argument('-n','--serviceName',type=str,help='注册服务名')
    smdGenaratorParser.add_argument('-g','--serviceGroup',type=str,help='服务分组，有默认值可以不输入')
    smdGenaratorParser.add_argument('-o','--out',action='store_true',help='会自动输出在serviceMetaDatas文件夹下')

    sbdGeneratorParser=subGeneratorParsers.add_parser('sbd',help='服务绑定元数据')
    sbdGeneratorParser.add_argument('-i','--oid',type=str,help='BPMN实例的唯一标识')
    sbdGeneratorParser.add_argument('-if','--oidFile',type=str,help='BPMN实例的唯一标识,从文件中输入')
    sbdGeneratorParser.add_argument('-tn','--taskName',type=str,help='要绑定的服务任务名')
    sbdGeneratorParser.add_argument('-sn','--serviceName',type=str,help='要绑定的服务名')
    sbdGeneratorParser.add_argument('-m','--httpMethod',type=str,help='服务调用方法get,post,delete,put')
    sbdGeneratorParser.add_argument('-r','--route',type=str,help='路径，url端口之后的那一部分,比如127.0.0.1:8080/test,这里就写/test')
    sbdGeneratorParser.add_argument('-input','--input',type=str,help='json格式,用于指定输入，不输入使用默认值')
    sbdGeneratorParser.add_argument('-output','--output',type=str,help='json格式,用于指定输出，不输入使用默认值')
    sbdGeneratorParser.add_argument('-he','--headers',type=str,help='json格式，服务调用额外的头部，不输入使用默认值')
    sbdGeneratorParser.add_argument('-g','--serviceGroup',type=str,help='要绑定的服务分组,不输入使用默认值')
    sbdGeneratorParser.add_argument('-o','--out',type=str,help='指定输出文件名，会写入到文件夹serviceBindDatas中')
    #bpmn解析器
    subBpmnParsers=bpmnParser.add_subparsers(dest='bpmnCmd',help="部署:deploy,实例化:instance,执行:complete")

    
    deployParser=subBpmnParsers.add_parser("deploy",help="部署BPMN文件")
    deployParser.add_argument('-ip','--ip',type=str,help="ip")
    deployParser.add_argument('-p','--port',type=str,help='端口')
    deployParser.add_argument('-f',"--file",type=str,help="要部署的BPMN文件的路径")
    deployParser.add_argument('-n','--deploymentName',type=str,help='部署名称')
    deployParser.add_argument('-s','--sigs',type=str,help='签名文件的路径')


    instanceParser=subBpmnParsers.add_parser("instance",help="实例化BPMN文件")
    instanceParser.add_argument('-ip','--ip',type=str,help="ip")
    instanceParser.add_argument('-p','--port',type=str,help='端口')
    instanceParser.add_argument('-n','--deploymentName',type=str,help='要实例化的BPMN文件的部署名称')
    instanceParser.add_argument('-pdf','--processDataFile',type=str,help='流程数据的文件路径,json格式,与-pd选一个输入')
    instanceParser.add_argument('-pd','--processData',type=str,help='流程数据,json格式,字符串直接输入,与-pdf选一个输入')
    instanceParser.add_argument('-bdf','--businessDataFile',type=str,help='业务数据的文件路径,json格式,与-bd选一个输入')
    instanceParser.add_argument('-bd','--businessData',type=str,help='业务数据,json格式,字符串直接输入，与-bdf选一个输入')
    instanceParser.add_argument('-t','--staticAllocationTable',type=str,help='用户任务的静态分配表,json格式,字符串直接输入,与-tf选一个输入')
    instanceParser.add_argument('-tf','--staticAllocationTableFile',type=str,help='用户任务的静态分配表文件路径,json格式,与-t选一个输入')
    instanceParser.add_argument('-o','--out',type=str,help='指定oid的输出文件名，会写入到oids文件夹下')


    completeParser=subBpmnParsers.add_parser("complete",help="执行BPMN文件")
    completeParser.add_argument('-ip','--ip',type=str,help="ip")
    completeParser.add_argument('-p','--port',type=str,help='端口')
    completeParser.add_argument('-i','--oid',type=str,help='BPMN实例的唯一标识')
    completeParser.add_argument('-if','--oidFile',type=str,help='BPMN实例的唯一标识,从文件中输入')
    completeParser.add_argument('-n','--taskName',type=str,help='要执行的')
    completeParser.add_argument('-pdf','--processDataFile',type=str,help='流程数据的文件路径,json格式,与-pd选一个输入')
    completeParser.add_argument('-pd','--processData',type=str,help='流程数据,json格式,字符串直接输入,与-pdf选一个输入')
    completeParser.add_argument('-bdf','--businessDataFile',type=str,help='业务数据的文件路径,json格式,与-bd选一个输入')
    completeParser.add_argument('-bd','--businessData',type=str,help='业务数据,json格式,字符串直接输入，与-bdf选一个输入')
    completeParser.add_argument('-u','--user',type=str,help='用户任务的执行者，如果没有分配就不需要这个选项')
    #service解析器
    subServiceParsers=serviceParser.add_subparsers(dest="serviceCmd",help="服务绑定,注册，删除")

    
    serviceRegisterParser=subServiceParsers.add_parser("register",help="服务注册")
    serviceRegisterParser.add_argument('-ip','--ip',type=str,help="ip")
    serviceRegisterParser.add_argument('-p','--port',type=str,help='端口')
    serviceRegisterParser.add_argument('-pv','--provider',type=str,help='服务提供方的名字，按照注册时的名字对应')
    serviceRegisterParser.add_argument('-smd','--serviceMetaData',type=str,help='服务注册元数据,字符串直接输入，与-smdf选一个输入')
    serviceRegisterParser.add_argument('-smdf','--serviceMetaDataFile',type=str,help='服务注册元数据文件路径，与-smd选一个输入')
    serviceRegisterParser.add_argument('-s','--sig',type=str,help='数字签名，直接字符串输入，与-sf选一个输入')
    serviceRegisterParser.add_argument('-sf','--sigFile',type=str,help='数字签名文件路径，与-s选一个输入')

    serviceBindParser=subServiceParsers.add_parser("bind",help="服务绑定")
    serviceBindParser.add_argument('-ip','--ip',type=str,help="ip")
    serviceBindParser.add_argument('-p','--port',type=str,help='端口')
    serviceBindParser.add_argument('-d','--data',type=str,help='服务绑定元数据,json格式,字符串直接输入,与-df选一个输入')
    serviceBindParser.add_argument('-df','--dataFile',type=str,help='服务绑定元数据文件路径,json格式,与-d选一个输入')
    serviceBindParser.add_argument('-s','--signatures',type=str,help='数字签名文件所在的目录，会读取其中所有的签名文件')
    #serviceBindParser.add_argument('-sf','--sigsFile',type=str,help='数字签名文件路径，与-s选一个输入')
    #deleteParser=subServiceParsers.add_parser("delete",help="删除服务")

    #组织解析器
    subOrgParsers=orgParser.add_subparsers(dest='orgCmd',help='组织的注册等...')

    orgRegisterParser=subOrgParsers.add_parser('register',help='组织注册')
    orgRegisterParser.add_argument('-ip','--ip',type=str,help='ip')
    orgRegisterParser.add_argument('-p','--port',type=str,help='端口')
    orgRegisterParser.add_argument('-n','--name',type=str,help='组织名')
    orgRegisterParser.add_argument('-o','--autoOut',action='store_true',help='会在privateKeys文件夹下自动保存返回的私钥')

    #脚本启动解析器
    subStartParser=startParser.add_subparsers(dest='startCmd',help='执行脚本选择，nacos或wfService')

    nacosStartParser=subStartParser.add_parser('nacos',help='启动nacos脚本')
    nacosStartParser.add_argument('-n','--dockerName',type=str,help='容器名称')

    wfServiceStartParser=subStartParser.add_parser('wfService',help='启动wfService脚本')
    wfServiceStartParser.add_argument('-n','--dockerName',type=str,help='容器名称')

    #签名工具
    subSigParsers= sigParser.add_subparsers(dest='signCmd',help='生成单个签名还是生成多个')
    singleSigParser=subSigParsers.add_parser('single',help='只需要一个私钥生成单个签名')
    mutilSigParser=subSigParsers.add_parser('mutil',help='使用多个私钥生成多个签名')

    singleSigParser.add_argument('-t','--type',type=str,help="file和str两个选项,分别代表要签名的数据是从文件输入还是字符串直接输入")
    singleSigParser.add_argument('-c','--content',type=str,help="要加密的数据,如果type是file则输入路径即可,如果是str输入带引号的字符串")
    singleSigParser.add_argument('-pk','--privateKey',type=str,help='私钥文件名(一般为注册时的name)，会在privateKeys文件中读取')
    #singleSigParser.add_argument('-j','--json',action='store_true',help='如果有这个选项输出的字符串会再被json转义一次')
    singleSigParser.add_argument('-o','--out',type=str,help="输出的文件名，会将签名写入该文件")

    mutilSigParser.add_argument('-t','--type',type=str,help="file和str两个选项,分别代表要签名的数据是从文件输入还是字符串直接输入")
    mutilSigParser.add_argument('-c','--content',type=str,help="要加密的数据,如果type是file则输入路径即可,如果是str输入带引号的字符串")
    mutilSigParser.add_argument('-pks','--privateKeys',nargs='+',help='多个私钥，会在privateKeys文件中读取')
    mutilSigParser.add_argument('-o','--out',type=str,help="指定文件夹名，会将生成的所有签名文件写入该文件夹")
    return parser

#######组件功能
def queryResult(ip:str,port:str,oid:str):
    url=f'http://{ip}:{port}/grafana/getResponseByOid/{oid}'
    return requests.get(url)
    
#创建文件夹，如果存在直接返回
def createIfNotExist(path:str):
    path=getAbsPath(path)
    if not os.path.exists(path):  
        os.makedirs(path)    
    else:  
        return

#路径转换
def getAbsPath(path:str)->str:
    if os.path.isabs(path):
        return path
    else:
        pwd = os.getcwd()
        if (pwd!=None and pwd!='' and pwd[len(pwd)-1]=='/'): 
            pwd=pwd[:-1]
        if (path[0]=='/'):
            path=path[1:]
        absPath=combin(pwd,path)
        return absPath
#读文件
def readFile(path:str)->str:
    filePath=getAbsPath(path)
    if(not os.path.exists(filePath)):
        raise RuntimeError(f'没有对应文件，请检查路径{path}')
    with open(filePath, 'rb') as file:
        return file.read().decode('utf-8').strip()

def readDir(path:str)->list:
    res=[]
    dirPath=getAbsPath(path)
    if os.path.exists(dirPath):
        for file_name in os.listdir(dirPath):
            filePath = os.path.join(dirPath, file_name)
            with open(filePath, 'rb') as file:
                res.append(file.read().decode('utf-8').strip())
        return res
    else:
        raise RuntimeError(f'没有对应的文件夹:{path}')
    
def readSigsAsList(path:str)->list:
    sigsString=readDir(path)
    sigsList=[]
    for sigsJson in sigsString:
        sigsList.append(json.loads(sigsJson))
    return sigsList

#写文件
def writeFile(path:str,data:str):
    filePath=getAbsPath(path)
    # 打开文件用于写入，如果文件不存在则创建
    with open(filePath, 'w') as file:
        file.write(data)  # 写入内容到文件

#拼一下
def combin(dir:str,path:str) ->str:
    return dir+'/'+path

#签名
def sign(message:str,private_key:str):
    # 将私钥字符串解码为字节串
    private_key_bytes = base64.b64decode(private_key)
    # 使用字节串创建 ECC 私钥对象
    private_key = serialization.load_der_private_key(
        private_key_bytes,
        password=None,
        backend=default_backend()
    )
    # 创建哈希对象，默认使用SHA-256算法
    hash_object = hashlib.sha256()
    # 更新哈希对象的内容
    hash_object.update(message.encode('UTF-8'))
    # 获取十六进制表示的哈希值
    messageHash = hash_object.digest()
    # 使用私钥对象对消息进行签名
    signature = private_key.sign(
        messageHash,
        ec.ECDSA(hashes.SHA256())
    )
    # 将签名转换为 Base64 字符串
    signature_base64 = base64.b64encode(signature).decode('UTF-8')
    
    return signature_base64

#获取私钥
def getPrivateKeyString(priKeys:list)->list:
    pkFiles=[]
    for privateKey in priKeys:
        pkFiles.append(json.loads(readFile(os.path.join('privateKeys',privateKey))))
    return pkFiles

########简单的参数检查
def sbdGeneratorArgsCheck(args:argparse.Namespace):
    if not ((args.oid==None) ^ (args.oidFile==None)):
        raise RuntimeError('检查对应实例的oid的输入,-i或-if只能选一个输入')
    if not args.taskName:
        raise RuntimeError('未执行要完成的用户任务名称,使用-tn或--taskName输入')
    if not args.serviceName:
        raise RuntimeError('缺少服务名，使用-sn或--serviceName输入')
    if not args.httpMethod:
        raise RuntimeError('缺少服务调用方法,使用-m或--httpMethod输入')
    if not (args.httpMethod.upper() in ["POST", "GET", "DELETE", "PUT"]):
        raise RuntimeError(f'{args.httpMethod},这是你输入的，是不是输出错了，只检测了常用的4个')
    if not args.route:
        raise RuntimeError('缺少rest接口路径,使用-r或--route输入')
    if not args.input:
        print('缺少input输入,使用默认值')
    if not args.output:
        print('缺少output输入，使用默认值')
    if not args.headers:
        print('缺少headers输入，使用默认值')        
    if not args.serviceGroup:
        print('缺少serviceGroup输入，使用默认值')
    if not args.out:
        print('********未指定输出文件名，仅打印**********')
def smdGeneratorArgsCheck(args:argparse.Namespace):
    if not args.ip:
        raise RuntimeError('缺少服务ip地址,使用-ip或--ip输入')
    if not args.port:
        raise RuntimeError('缺少服务端口号,使用-p或--port输入')
    if not args.serviceName:
        raise RuntimeError('缺少服务名，使用-n或--serviceName输入')
    if not args.serviceGroup:
        print('缺少服务分组，采用默认分组，如果需要服务分组使用-g或--serviceGroup输入')
    if not args.out:
        print('*****未选择输出为文件*******')
def nacosStartArgsCheck(args:argparse.Namespace):
    if not args.dockerName:
        raise RuntimeError('缺少容器名称输入,使用-n或者--dockerName输入')

def wfServiceStartArgsCheck(args:argparse.Namespace):
    if not args.dockerName:
        raise RuntimeError('缺少容器名称输入,使用-n或者--dockerName输入')

def deployArgsCheck(args:argparse.Namespace):
    if not args.ip:
        raise RuntimeError('缺少ip,使用-ip或--ip输入')
    if not args.port:
        raise RuntimeError('缺少端口,使用-p或--port输入')
    if not args.file:
        raise RuntimeError('缺少BPMN文件输入,使用-f或--file输入文件路径')
    if not args.deploymentName:
        raise RuntimeError('未指定BPMN文件的部署名,使用-n或--deploymentName输入')
    if not args.sigs:
        raise RuntimeError('缺少对应的数字签名文件路径,使用-s或--sigs输入签名文件所在的目录即可')

def instanceArgsCheck(args:argparse.Namespace):
    if not args.out:
        print('未指定oid输出，请注意')
    if not args.ip:
        raise RuntimeError('缺少ip,使用-ip或--ip输入')
    if not args.port:
        raise RuntimeError('缺少端口,使用-p或--port输入')
    if not args.deploymentName:
        raise RuntimeError('未指定要实例化的BPMN文件的部署名,使用-n或--deploymentName输入')
    if not ((args.processData==None) ^ (args.processDataFile==None)):
        raise RuntimeError('检查processData的输入,-pd与-pdf只能选一个输入')
    if not ((args.businessData==None) ^ (args.businessDataFile==None)):
        raise RuntimeError('检查businessData的输入,-bd与-bdf只能选一个输入')
    if not ((args.staticAllocationTable==None) ^ (args.staticAllocationTableFile==None)):
        raise RuntimeError('检查staticAllocationTable的输入,-t与-tf只能选一个输入')
    
def completeArgsCheck(args:argparse.Namespace):
    if not args.ip:
        raise RuntimeError('缺少ip,使用-ip或--ip输入')
    if not args.port:
        raise RuntimeError('缺少端口,使用-p或--port输入')
    if not ((args.oid==None) ^ (args.oidFile==None)):
        raise RuntimeError('检查对应实例的oid的输入,-i或-if只能选一个输入')
    if not args.taskName:
        raise RuntimeError('未执行要完成的用户任务名称,使用-n或--taskName输入')
    if not ((args.processData==None) ^ (args.processDataFile==None)):
        raise RuntimeError('检查processData的输入,-pd与-pdf只能选一个输入')
    if not ((args.businessData==None) ^ (args.businessDataFile==None)):
        raise RuntimeError('检查businessData的输入,-bd与-bdf只能选一个输入')
    
def bindArgsCheck(args:argparse.Namespace):
    if not args.ip:
        raise RuntimeError('缺少ip,使用-ip或--ip输入')
    if not args.port:
        raise RuntimeError('缺少端口,使用-p或--port输入')
    if not ((args.data==None) ^ (args.dataFile==None)):
        raise RuntimeError('检查服务绑定信息的输入，-d与-df只能选一个输入')
    if not args.signatures:
        raise RuntimeError('未指定签名文件所在的目录,使用-s或者--signatures指定')

def serviceRegisterArgsCheck(args:argparse.Namespace):
    if not args.ip:
        raise RuntimeError('缺少ip,使用-ip或--ip输入')
    if not args.port:
        raise RuntimeError('缺少端口,使用-p或--port输入')
    if not args.provider:
        raise RuntimeError('未输入组织名称，使用-pv或--provider输入')
    if not ((args.serviceMetaData==None) ^ (args.serviceMetaDataFile==None)):
        raise RuntimeError('检查服务注册元信息的输入，-smd与-smdf只能选一个输入')
    if not ((args.sig==None) ^ (args.sigFile==None)):
        raise RuntimeError('检查签名文件的输入，-s与-sf只能选一个输入')

def orgRegisterArgsCheck(args:argparse.Namespace):
    if not args.ip:
        raise RuntimeError('缺少ip,使用-ip或--ip输入')
    if not args.port:
        raise RuntimeError('缺少端口,使用-p或--port输入')
    if not args.name:
        raise RuntimeError('缺少组织名输入,使用-n或--name输入')
    if not args.autoOut:
        print('未设置自动输出，将所有私钥文件都放入privateKeys文件夹中管理,并以组织名命名')    

def singleSignArgsCheck(args:argparse.Namespace):
    if (not args.type) or (args.type!='file' and args.type!='str'):
        raise RuntimeError('请检查type输入,type输入只能是file或str,file表示要签名的数据从文件输入,str表示直接从命令行的字符串读取')
    if not args.content:
        raise RuntimeError('未输入要签名的内容，使用-c或-content输入，如果type是file则输入路径，反之直接输入数据')
    if not args.privateKey:
        raise RuntimeError('缺少私钥文件输入，直接用-pk或--privateKey输入组织名，会自动在privateKeys文件夹下查找')

def mutilSignArgsCheck(args:argparse.Namespace):
    if (not args.type) or (args.type!='file' and args.type!='str'):
        raise RuntimeError('请检查type输入,type输入只能是file或str,file表示要签名的数据从文件输入,str表示直接从命令行的字符串读取')
    if not args.content:
        raise RuntimeError('未输入要签名的内容，使用-c或-content输入，如果type是file则输入路径，反之直接输入数据')
    if not args.privateKeys:
        raise RuntimeError('缺少私钥文件输入，直接用-pks或--privateKeys输入组织名，会自动在privateKeys文件夹下查找')

########子功能实现
def sbdGenerate(args:argparse.Namespace):
    sbdGeneratorArgsCheck(args)
    serviceBindData={
        'oid':args.oid if args.oid else readFile(args.oidFile),
        'taskName':args.taskName,
        'serviceName':args.serviceName,
        'httpMethod':args.httpMethod.upper(),
        'route':args.route
    }
    if args.input:
        serviceBindData['input']=args.input
    if args.output:
        serviceBindData['output']=args.output
    if args.headers:
        serviceBindData['headers']=args.headers
    if args.serviceGroup:
        serviceBindData['serviceGroup']=args.serviceGroup
    print(json.dumps(serviceBindData,ensure_ascii=False))
    if args.out:
        createIfNotExist('serviceBindDatas')
        fileName=os.path.join('serviceBindDatas',args.out)
        writeFile(data=json.dumps(serviceBindData,ensure_ascii=False),path=fileName)
        print(f'已写入{fileName}')

def smdGenerate(args:argparse.Namespace):
    smdGeneratorArgsCheck(args)
    serviceMetaData={
        'ip':args.ip,
        'port':args.port,
        'serviceName':args.serviceName
    }
    if args.serviceGroup:
        serviceMetaData['serviceGroup']=args.serviceGroup
    print(json.dumps(serviceMetaData,ensure_ascii=False))
    if args.out:
        createIfNotExist('serviceMetaDatas')
        fileName=f'{args.serviceName}_{args.serviceGroup}' if args.serviceGroup else f'{args.serviceName}_default'
        fileName=os.path.join('serviceMetaDatas',fileName)
        writeFile(data=json.dumps(serviceMetaData,ensure_ascii=False),path=fileName)
        print(f'已写入{fileName}')

def nacosStart(args:argparse.Namespace):
    nacosStartArgsCheck(args)
    nacos=subprocess.run(f'docker exec {args.dockerName} sh /usr/local/scripts/nacosScripts/nacosStart.sh',shell=True,capture_output=True, text=True)
    print(nacos.stdout)

def wfServiceStart(args:argparse.Namespace):
    wfServiceStartArgsCheck(args)
    wfService=subprocess.run(f'docker exec {args.dockerName} sh /usr/local/scripts/wfServiceScripts/wfServiceStart.sh',shell=True,capture_output=True, text=True)
    print(wfService.stdout)
          
def deploy(args:argparse.Namespace):
    deployArgsCheck(args)
    url=f'http://{args.ip}:{args.port}/grafana/wfRequest/deploy'
    files = {
        'file': open(args.file, 'rb'),
        'signatures': ('sigs',io.BytesIO(json.dumps(readSigsAsList(args.sigs),ensure_ascii=False).encode('utf-8')))
    }
    data = {
        'deploymentName': args.deploymentName
    }
    response=requests.post(url,files=files,data=data)
    #这里要有个结果处理的逻辑
    resultMap={}
    if (response.status_code!=200):
        resultMap['code']=response.status_code
        resultMap['body']=response.text
        resultMap['state']='Failed'
    else:
        responseMap=json.loads(response.text)
        if responseMap['code']!=200:
            resultMap['code']=responseMap['code']
            resultMap['body']=responseMap['body']
            resultMap['state']='Failed'
        else:
            for i in range(50):
                time.sleep(0.2)
                response=queryResult(args.ip,args.port,args.deploymentName)
                if response.status_code!=200:
                    print(f'查询执行结果出错,code:{response.status_code},body:{response.text}')
                    break
                elif response.text!='none':
                    #temp=json.loads(response.text)
                    resultMap['code']=200
                    resultMap['body']=args.deploymentName+"部署成功"
                    resultMap['state']='Success'
                    break
            if not ('code' in resultMap):
                resultMap['code']=500
                resultMap['body']='超时，可能是上链失败或者其他原因导致'
                resultMap['state']='Failed'
    print(json.dumps(resultMap,ensure_ascii=False))

def instance(args:argparse.Namespace):
    instanceArgsCheck(args)
    url=f'http://{args.ip}:{args.port}/grafana/wfRequest/instance'
    # 定义请求头
    headers = {
        'Content-Type': 'application/json'
    }
    data = {
        "deploymentName": args.deploymentName,
        "processData": args.processData if args.processData else readFile(path=args.processDataFile),
        "businessData": args.businessData if args.businessData else readFile(path=args.businessDataFile),
        "staticAllocationTable": args.staticAllocationTable if args.staticAllocationTable else readFile(path=args.staticAllocationTableFile)
    }
    response=requests.post(url,headers=headers,data=json.dumps(data))
    #处理执行结果
    resultMap={}
    print(response.text)
    if (response.status_code!=200):
        resultMap['code']=response.status_code
        resultMap['body']=response.text
        resultMap['state']='Failed'
    else:
        responseMap=json.loads(response.text)
        oid=responseMap['Oid']
        if responseMap['code']!=200:
            resultMap['code']=responseMap['code']
            resultMap['body']=responseMap['body']
            resultMap['state']='Failed'
        else:
            for i in range(50):
                time.sleep(0.2)
                response=queryResult(args.ip,args.port,oid)
                if response.status_code!=200:
                    print(f'查询执行结果出错,code:{response.status_code},body:{response.text}')
                    break
                elif response.text!='none':
                    temp=json.loads(response.text)
                    resultMap['code']=200
                    resultMap['body']=temp['businessData'] if temp['businessData']!='...' else '{}'
                    resultMap['oid']=oid
                    resultMap['nextTaskName']=temp["toTaskName"][1:len(temp["toTaskName"])-1]
                    resultMap['isEnd']=temp['isEnd']
                    resultMap['state']='Success'
                    break
            if not ('code' in resultMap):
                resultMap['code']=500
                resultMap['body']='超时，可能是上链失败或者其他原因导致'
                resultMap['state']='Failed'

    if args.out:
        createIfNotExist('oids')
        path=os.path.join('oids',args.out)
        writeFile(data=oid,path=path)
        print(f'已输出至{path}')
    print(json.dumps(resultMap,ensure_ascii=False))

def complete(args:argparse.Namespace):
    completeArgsCheck(args)
    url=f'http://{args.ip}:{args.port}/grafana/wfRequest/complete'
     # 定义请求头
    headers = {
        'Content-Type': 'application/json'
    }
    oid=args.oid if args.oid else readFile(args.oidFile)
    data = {
        "Oid":oid,
        "taskName":args.taskName,
        "processData": args.processData if args.processData else readFile(path=args.processDataFile),
        "businessData": args.businessData if args.businessData else readFile(path=args.businessDataFile)
    }
    if args.user:
        data['user']=args.user
    response=requests.post(url,headers=headers,data=json.dumps(data))
    #处理执行结果
    resultMap={}
    if (response.status_code!=200):
        resultMap['code']=response.status_code
        resultMap['body']=response.text
        resultMap['state']='Failed'
    else:
        responseMap=json.loads(response.text)
        if responseMap['code']!=200:
            resultMap['code']=responseMap['code']
            resultMap['body']=responseMap['body']
            resultMap['state']='Failed'
        else:
            for i in range(50):
                time.sleep(0.2)
                response=queryResult(args.ip,args.port,oid)
                if response.status_code!=200:
                    print(f'查询执行结果出错,code:{response.status_code},body:{response.text}')
                    break
                elif response.text!='none':
                    temp=json.loads(response.text)
                    resultMap['code']=200
                    resultMap['body']=temp['businessData'] if temp['businessData']!='...' else '{}'
                    resultMap['oid']=oid
                    resultMap['nextTaskName']=temp["toTaskName"][1:len(temp["toTaskName"])-1]
                    resultMap['isEnd']=temp['isEnd']
                    resultMap['state']='Success'
                    break
            if not ('code' in resultMap):
                resultMap['code']=500
                resultMap['body']='超时，可能是上链失败或者其他原因导致'
                resultMap['state']='Failed'
    print(json.dumps(resultMap,ensure_ascii=False))

def bind(args:argparse.Namespace):
    bindArgsCheck(args)
    url=f'http://{args.ip}:{args.port}/grafana/serviceDynamicBind'
    # 定义请求头
    headers = {
        'Content-Type': 'application/json'
    }
    sigs=readSigsAsList(args.signatures)
    data = {
        'data':args.data if args.data else readFile(args.dataFile),
        'sigs':json.dumps(sigs)
    }
    response=requests.post(url,headers=headers,data=json.dumps(data))
    resultMap={}
    resultMap['code']=response.status_code
    resultMap['body']=response.text
    print(json.dumps(resultMap,ensure_ascii=False))

def serviceRegister(args:argparse.Namespace):
    serviceRegisterArgsCheck(args)
    url=f'http://{args.ip}:{args.port}/grafana/serviceRegister'
    headers = {
        'Content-Type': 'application/json'
    }
    data = {
        'provider':args.provider,
        'serviceMetaData':args.serviceMetaData if args.serviceMetaData else readFile(args.serviceMetaDataFile),
        'signature':args.sig if args.sig else json.loads(readFile(args.sigFile))['signature']
    }
    response=requests.post(url,headers=headers,data=json.dumps(data))
    resultMap={}
    resultMap['code']=response.status_code
    resultMap['body']=response.text
    print(json.dumps(resultMap,ensure_ascii=False))

def orgRegister(args:argparse.Namespace):
    orgRegisterArgsCheck(args)
    url=f'http://{args.ip}:{args.port}/grafana/register'
    data={
        'name':args.name
    }
    response=requests.post(url,data=data)
    if (response.status_code!=200):
        raise RuntimeError(f'请求出错：{response.text}')
    if args.autoOut:
        createIfNotExist('privateKeys')
        writeFile(data=response.text,path=os.path.join('privateKeys',args.name))
        print(f'已写入privateKeys/{args.name}')
    else:
        print(response.text)

def singleSign(args:argparse.Namespace):
    singleSignArgsCheck(args)
    content=args.content if args.type=='str' else readFile(args.content)
    nameAndPriKey=json.loads(readFile(os.path.join('privateKeys',args.privateKey)))
    signature=sign(message=content,private_key=nameAndPriKey['privateKey'])
    data={
        'name':nameAndPriKey['name'],
        'signature':signature
    }
    print(f'生成的数字签名如下:\n{json.dumps(data,ensure_ascii=False)}')
    if args.out:
        createIfNotExist('signatures')
        fileName=os.path.join('signatures',args.out)
        writeFile(data=json.dumps(data,ensure_ascii=False),path=fileName)
        print(f'已写入{fileName}')

def mutilSign(args:argparse.Namespace):
    mutilSignArgsCheck(args)
    content=args.content if args.type=='str' else readFile(args.content)
    nameAndPriKeys=getPrivateKeyString(args.privateKeys)
    listData = []
    for nameAndPriKey in nameAndPriKeys:
        temp = {"name": nameAndPriKey['name'], "signature": sign(content, nameAndPriKey['privateKey'])}
        listData.append(temp)
    print(f'生成的数字签名如下:\n{json.dumps(listData,ensure_ascii=False)}')
    if args.out:
        createIfNotExist('signatures')
        dirName=os.path.join('signatures',args.out)
        createIfNotExist(dirName)
        for data in listData:
            writeFile(data=json.dumps(data,ensure_ascii=False),path=os.path.join(dirName,data['name']))
        print(f'已输出至{dirName}')

def sig(args:argparse.Namespace):
    listData = []
    nameAndPriKeys=getPrivateKeyString(args.priKeyFiles,args.priKeyDirs)
    if args.type=='str':
        for nameAndPriKey in nameAndPriKeys:
            temp = {"name": nameAndPriKey['name'], "signature": sign(args.content, nameAndPriKey['privateKey'])}
            listData.append(temp)
    if args.type=='file':
        for nameAndPriKey in nameAndPriKeys:
            temp={'name':nameAndPriKey['name'],'signature':sign(readFile[args.content],nameAndPriKey['privateKey'])}
            listData.append(temp)
    if args.out:
        createIfNotExist(args.out)
        for data in listData:
            fileName='signature_'+data['name']
            writeFile(data=json.dumps(data),path=os.path.join(args.out,fileName))
    else:
        print(f'生成数字签名如下\n{json.dumps(listData)}')    
    
    
def main():

    # 解析参数
    args = parseCmd().parse_args()
    # 使用解析后的参数
    if args.command=='bpmn':
        if args.bpmnCmd=='deploy':
            deploy(args)
        elif args.bpmnCmd=='instance':
            instance(args)
        elif args.bpmnCmd=='complete':
            complete(args)
        else:
            raise RuntimeError(f'bpmn子功能下不支持{args.bpmnCmd}功能')
    elif args.command=='service':
        if args.serviceCmd=='register':
            serviceRegister(args)
        elif args.serviceCmd=='bind':
            bind(args)
        else:
            raise RuntimeError(f'service子功能不支持{args.serviceCmd}功能')
    elif args.command=='org':
        if args.orgCmd=='register':
            orgRegister(args)
        else:
            raise RuntimeError(f'org子功能不支持{args.orgCmd}功能')
    elif args.command=='sign':
        if args.signCmd=='single':
            singleSign(args)
        elif args.signCmd=='mutil':
            mutilSign(args)
        else:
            raise RuntimeError(f'sign子功能不支持{args.signCmd}功能')
    elif args.command=='start':
        if args.startCmd=='nacos':
            nacosStart(args)
        elif args.startCmd=='wfService':
            wfServiceStart(args)
        else:
            raise RuntimeError(f'start子功能不支持{args.startCmd}功能')
    elif args.command=='generate':
        if args.generateCmd=='smd':
            smdGenerate(args)
        elif args.generateCmd=='sbd':
            sbdGenerate(args)
        else:
            raise RuntimeError(f'generate子功能不支持{args.generateCmd}功能')
    else:
        raise RuntimeError(f'主功能不支持{args.command}功能')

if __name__ == "__main__":
    main()
