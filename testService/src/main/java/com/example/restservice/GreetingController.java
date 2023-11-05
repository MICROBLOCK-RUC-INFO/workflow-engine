package com.example.restservice;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.helper.jsonTransfer;

@RestController
public class GreetingController {

    @RequestMapping(value = "/test", method = {RequestMethod.POST})
    @ResponseBody
    public String test(@RequestBody String req) throws IOException, InterruptedException, ExecutionException {
        Map<String,Object> requestMap=jsonTransfer.jsonToMap(req);
        for (String key:requestMap.keySet()) {
            System.out.println(key+":"+String.valueOf(requestMap.get(key)));
        }
        return req;
    }
}
