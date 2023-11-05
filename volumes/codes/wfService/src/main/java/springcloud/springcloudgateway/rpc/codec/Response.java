package springcloud.springcloudgateway.rpc.codec;

public class Response {

    private long mId;

    /**
     * 1. json
     * 2. protobuff
     * 3. bson
     * 4. kryo
     */
    private byte serializer = 1;

    private Object result;

    public Response(long mId) {
        this.mId = mId;
    }

    public long getmId() {
        return mId;
    }

    public void setmId(long mId) {
        this.mId = mId;
    }

    public byte getSerializer() {
        return serializer;
    }

    public void setSerializer(byte serializer) {
        this.serializer = serializer;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
