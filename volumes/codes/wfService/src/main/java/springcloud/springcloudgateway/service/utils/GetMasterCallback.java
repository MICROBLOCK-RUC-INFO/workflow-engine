package springcloud.springcloudgateway.service.utils;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: 李浩然
 * @Date: 2021/10/20 10:26 下午
 */
public class GetMasterCallback implements Callback {
    private String server;
    private String data;
    Logger logger = LoggerFactory.getLogger(GetMasterCallback.class);

    public GetMasterCallback(String server, String data) {
        this.server = server;
        this.data = data;
    }

    @Override
    public void onFailure(Call call, IOException e) {

    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        Map<String, String> params = new HashMap<>();
        if (Boolean.parseBoolean(response.body().string())) {
            logger.info("master node ip is:" + server);
            String masterIP = "http://" + server + "/grafana/monitor/collectData";
            try {
                HttpClientUtil.asyncHttpRequest(masterIP, params, data, "POST");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
