package springcloud.springcloudgateway.rpc.utils;

public class GloabalConstants {

    public static final short MAGIC_NUMBER = (short) 0xbabb;

    public static final int HEADER_LENGTH = 5;

    public static final int DEFAULT_IO_THREADS = Math.min(Runtime.getRuntime().availableProcessors() + 1, 32);

    public static final int DEFAULT_TIMEOUT = 1000;

    public static final String ANYHOST_VALUE = "0.0.0.0";

    public static final String LOCALHOST_KEY = "localhost";

    public static final String LOCALHOST_VALUE = "127.0.0.1";
}
