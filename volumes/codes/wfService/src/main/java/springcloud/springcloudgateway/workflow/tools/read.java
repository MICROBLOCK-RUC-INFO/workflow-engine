package springcloud.springcloudgateway.workflow.tools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.stereotype.Component;


public class read {
    public static String readFile(String path) {
        try {
            //01.FileInputStream（字节流） 实现了InputStream接口，用来读取文件中的字节流，参数是文件或者文件路径+文件名称
            FileInputStream fileInputStream = new FileInputStream(path);
            // 02.将 fileInputStream（字节流） 流作为参数，转为InputStreamReader（字符流）
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            // 03.将 字符流（参数）转为字符串流，带缓冲的流读取，默认缓冲区8k
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String tempString;
            StringBuilder stringBuilder=new StringBuilder();
            while ((tempString = bufferedReader.readLine()) != null) {// 直接返回读取的字符串
                // 将字符串 添加到 stringBuilder中
                stringBuilder.append(tempString);
            }
            bufferedReader.close();
            inputStreamReader.close();
            fileInputStream.close();
            return stringBuilder.toString(); 
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
    }
}
