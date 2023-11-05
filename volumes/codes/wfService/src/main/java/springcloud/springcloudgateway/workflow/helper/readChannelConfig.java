package springcloud.springcloudgateway.workflow.helper;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import org.springframework.stereotype.Component;

import springcloud.springcloudgateway.workflow.tools.read;


@Component
public class readChannelConfig {
    public final String peerName="peerName";
    public final String peerAddress="peerAddress";
    public final String peerTlsCaPath="peerTlsCaPath";
    public final String ordererName="ordererName";
    public final String ordererAddress="ordererAddress";
    public final String ordererTlsCaPath="ordererTlsCaPath";



    public Map<String,List<String>> getPeerConfig(wfConfig wfConfig) {
        String peerConfig=read.readFile(wfConfig.getPeersConfigPath());
        if (peerConfig.equals("error")) {
            return null;
        }
        peerConfig=peerConfig.replaceAll(" ","");
        peerConfig=peerConfig.replaceAll("\\[","");
        String[] peerProperties=peerConfig.split("]");
        List<String> peerNameList=Arrays.asList(peerProperties[0].split(","));
        List<String> peerAddressList=Arrays.asList(peerProperties[1].split(","));
        List<String> peerTlsCaPathList=Arrays.asList(peerProperties[2].split(","));
        Map<String,List<String>> res=new HashMap<>();
        res.put(peerName,peerNameList);
        res.put(peerAddress,peerAddressList);
        res.put(peerTlsCaPath,peerTlsCaPathList);
        return res;
    }

    public Map<String,List<String>> getOrdererConfig(wfConfig wfConfig) {
        String ordererConfig=read.readFile(wfConfig.getOrderersConfigPath());
        if (ordererConfig.equals("error")) {
            return null;
        }
        ordererConfig=ordererConfig.replaceAll(" ","");
        ordererConfig=ordererConfig.replaceAll("\\[","");
        String[] ordererProperties=ordererConfig.split("]");
        List<String> ordererNameList=Arrays.asList(ordererProperties[0].split(","));
        List<String> ordererAddressList=Arrays.asList(ordererProperties[1].split(","));
        List<String> ordererTlsCaPathList=Arrays.asList(ordererProperties[2].split(","));
        Map<String,List<String>> res=new HashMap<>();
        res.put(ordererName,ordererNameList);
        res.put(ordererAddress,ordererAddressList);
        res.put(ordererTlsCaPath,ordererTlsCaPathList);
        return res;
    }
}
