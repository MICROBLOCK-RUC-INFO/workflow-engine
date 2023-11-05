package springcloud.springcloudgateway.rpc.remoting.server;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import springcloud.springcloudgateway.rpc.remoting.MyChannelHandler;
import springcloud.springcloudgateway.rpc.remoting.server.ChannelEventRunnable.ChannelState;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import static springcloud.springcloudgateway.rpc.utils.NamedThreadFactory.SHARED_EXECUTOR;

/**
 *
 */
@Sharable
public class NettyServerHandler extends ChannelDuplexHandler {

    private final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    private MyChannelHandler handler;

    public NettyServerHandler(MyChannelHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("handler == null");
        }
        this.handler = handler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ExecutorService executor = getExecutorService();
        try {
            executor.execute(new ChannelEventRunnable(ctx.channel(), handler, ChannelState.RECEIVED, msg));
        } catch (Throwable t) {
            throw new ExecutionException("received event " + getClass() + " error when process received event .", t);
        }

    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
        ExecutorService executor = getExecutorService();
        try {
            executor.execute(new ChannelEventRunnable(ctx.channel(), handler, ChannelState.SENT, msg));
        } catch (Throwable t) {
            throw new ExecutionException("sent event " + getClass() + " error when process sent event .",
                    t);
        }
    }

    /**
     * client连接时触发调用
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ExecutorService executor = getExecutorService();
        try {
            executor.execute(new ChannelEventRunnable(ctx.channel(), handler, ChannelState.DISCONNECTED));
        } catch (Throwable t) {
            throw new ExecutionException("connect event " + getClass() + " error when process disconnected event .", t);
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        handler.disconnected(ctx.channel());


        ExecutorService executor = getExecutorService();
        try {
            executor.execute(new ChannelEventRunnable(ctx.channel(), handler, ChannelState.DISCONNECTED));
        } catch (Throwable t) {
            throw new ExecutionException("disconnect event " + getClass() + " error when process disconnected event .", t);
        }
    }

    /**
     *
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ExecutorService executor = getExecutorService();
        try {
            executor
                    .execute(new ChannelEventRunnable(ctx.channel(), handler, ChannelState.CAUGHT, cause));
        } catch (Throwable t) {
            throw new ExecutionException("caught event" + getClass() + " error when process caught event .", t);
        }
    }

    private ExecutorService getExecutorService() {
        return SHARED_EXECUTOR;
    }
}
