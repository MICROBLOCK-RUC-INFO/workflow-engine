package springcloud.springcloudgateway.workflow.tools;

public class keyCombination {
    /**
     * @apiNote 简单的字符串拼接,将传入的String数组,以"-"拼接成字符串
     * @param keys
     * @return
     */
    public static String combine(String... keys) {
        StringBuilder sb=new StringBuilder();
        for (String key:keys) {
            sb.append(key).append("-");
        }
        return sb.toString();
    }
}
