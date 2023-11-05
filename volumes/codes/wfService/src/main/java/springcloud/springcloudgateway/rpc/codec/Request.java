package springcloud.springcloudgateway.rpc.codec;

import java.util.concurrent.atomic.AtomicLong;

public class Request {

    private static final AtomicLong INVOKE_ID = new AtomicLong(0);

    private final long mId;

    /**
     * 1. json
     * 2. protobuff
     * 3. bson
     * 4. kryo
     */
    private byte serializer = 1;

    private Object mData;

    public Request() {
        mId = newId();
    }

    public Request(long id) {
        mId = id;
    }

    private static long newId() {
        // getAndIncrement() When it grows to MAX_VALUE, it will grow to MIN_VALUE, and the negative can be used as ID
        return INVOKE_ID.getAndIncrement();
    }

    public long getmId() {
        return mId;
    }

    public byte getSerializer() {
        return serializer;
    }

    public void setSerializer(byte serializer) {
        this.serializer = serializer;
    }

    public Object getmData() {
        return mData;
    }

    public void setmData(Object mData) {
        this.mData = mData;
    }
}
