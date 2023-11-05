package springcloud.springcloudgateway.rpc.codec;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcEncoder extends MessageToByteEncoder {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if (msg instanceof Request) {
            encodeRequest(ctx, (Request) msg, out);
        } else if (msg instanceof Response) {
            encodeResponse(ctx, (Response) msg, out);
        }
    }

    /**
     * header = msgType + serializer + requestId + dataLength = byte(1) + byte(1) + long(8) + int(4) = 14
     * body = data
     *
     * @param ctx
     * @param msg
     * @param out
     */
    private void encodeRequest(ChannelHandlerContext ctx, Request msg, ByteBuf out) {
        // header.
        byte[] header = new byte[14];
        // 1. request, 2.response
        Bytes.bytes((byte) 1, header);
        // set serializer
        Bytes.bytes(msg.getSerializer(), header, 1);
        // set requestId
        Bytes.long2bytes(msg.getmId(), header, 2);
        // set length
        byte[] data = JSON.toJSONBytes(msg.getmData());
        Bytes.int2bytes(data.length, header, 10);
        // header 14个字节
        out.writeBytes(header);
        // data
        out.writeBytes(data);
    }

    private void encodeResponse(ChannelHandlerContext ctx, Response msg, ByteBuf out) {
        // header.
        byte[] header = new byte[14];
        // 1. request, 2.response
        Bytes.bytes((byte) 2, header);
        // set serializer
        Bytes.bytes(msg.getSerializer(), header, 1);
        // set requestId
        Bytes.long2bytes(msg.getmId(), header, 2);
        // set length
        byte[] data = JSON.toJSONBytes(msg.getResult());
        Bytes.int2bytes(data.length, header, 10);
        // header 14个字节
        out.writeBytes(header);
        // data
        out.writeBytes(data);
    }
}
