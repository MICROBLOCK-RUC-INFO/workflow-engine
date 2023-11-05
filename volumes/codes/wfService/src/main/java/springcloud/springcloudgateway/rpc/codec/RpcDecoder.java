package springcloud.springcloudgateway.rpc.codec;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class RpcDecoder extends ByteToMessageDecoder {

    /**
     * header = msgType + serializer + requestId + dataLength = byte(1) + byte(1) + long(8) + int(4) = 14
     * body = data
     *
     * @param ctx
     * @param in
     * @param out
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 14) {
            return;
        }
        //标记当前的readindex位置
        in.markReaderIndex();

        //get header
        byte[] header = new byte[14];
        in.readBytes(header);

        // dataLength
        int dataLength = Bytes.bytes2int(header, 10);
        if (in.readableBytes() < dataLength) {
            //读到的消息体长度如果小于我们传送过来的消息长度，则resetReaderIndex. 这个配合markReaderIndex使用的。把readIndex重置到mark的地方
            in.resetReaderIndex();
            return;
        }
        byte msgType = header[0];
        // requestId
        long requestId = Bytes.bytes2long(header, 2);
        byte[] dataOrResult = new byte[dataLength];
        //先默认用fastjson
        in.readBytes(dataOrResult);
        Object data = JSON.parse(dataOrResult);
        if (msgType == 1) {
            //request
            Request request = new Request(requestId);
            request.setSerializer(header[1]);
            request.setmData(data);
            out.add(request);
        } else {
            //response
            Response response = new Response(requestId);
            response.setSerializer(header[1]);
            response.setResult(data);
            out.add(response);
        }
    }
}
