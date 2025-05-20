package springcloud.springcloudgateway.workflow.tools;

/**
 * 2025/4/30
 * Key组合工具，比如oid+taskName或者其他的
 * 当时应该是因为怕后面忘记其中的Key是怎么组合的，所以写了这个
 */
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
