package springcloud.springcloudgateway.rpc.remoting.server;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import springcloud.springcloudgateway.rpc.codec.InvokerObject;
import springcloud.springcloudgateway.rpc.codec.Request;
import springcloud.springcloudgateway.rpc.codec.Response;
import springcloud.springcloudgateway.rpc.remoting.MyChannelHandler;
import springcloud.springcloudgateway.rpc.remoting.RemotingException;
import springcloud.springcloudgateway.rpc.utils.Wrapper;

import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;

public abstract class AbstractServer implements MyChannelHandler {

    private static final Logger logger = LoggerFactory.getLogger(AbstractServer.class);

    protected volatile boolean closed;

    protected InetSocketAddress localAddress;

    protected InetSocketAddress bindAddress;

    public AbstractServer(String bindIp, Integer bindPort) throws RemotingException {
        localAddress = new InetSocketAddress(bindIp, bindPort);
        bindAddress = new InetSocketAddress("0.0.0.0", bindPort);
        try {
            doOpen();
            if (logger.isInfoEnabled()) {
                logger.info("Start " + getClass().getSimpleName() + " bind " + getBindAddress());
            }
        } catch (Throwable t) {
            throw new RemotingException(getLocalAddress(), null, "Failed to bind " + getClass().getSimpleName() + " on " + getLocalAddress() + ", cause: " + t.getMessage(), t);
        }
    }

    protected abstract void doOpen() throws Throwable;

    protected abstract void doClose() throws Throwable;

    @Override
    public void connected(Channel channel) throws ExecutionException {
        if (closed) {
            return;
        }
        logger.info("connected from " + channel.remoteAddress());
    }

    @Override
    public void disconnected(Channel channel) throws ExecutionException {
        logger.info("disconnected from " + channel.remoteAddress());
    }

    @Override
    public void sent(Channel channel, Object message) throws ExecutionException {
    }

    @Override
    public void received(Channel channel, Object message) throws ExecutionException {
        Request request = (Request) message;
        Response response = new Response(request.getmId());
        Object data = request.getmData();
        if (data == null) {
            response.setResult("null");
        } else {
            InvokerObject invokerObject = JSON.parseObject(data.toString(), InvokerObject.class);
            ServiceConfig service = NettyServerManager.getInstance().getWrapper(invokerObject.getClassName());
            System.out.println("invoke::::" + invokerObject.getMethodName());
            Wrapper wrapper = service.getWrapper();
            Object result = null;
            try {
                result = wrapper.invokeMethod(service.getRef(), invokerObject.getMethodName(), invokerObject.getParameterTypes(), invokerObject.getParameters());
            } catch (NoSuchMethodException e) {

            } catch (InvocationTargetException e) {

            }
            response.setResult(result);
        }
        channel.writeAndFlush(response);
    }

    @Override
    public void close() {
        if (logger.isInfoEnabled()) {
            logger.info("Close " + getClass().getSimpleName() + " bind " + getBindAddress() + ", export " + getLocalAddress());
        }
        try {
            doClose();
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
    }

    @Override
    public void caught(Channel channel, Throwable exception) throws ExecutionException {

    }

    public boolean isClosed() {
        return closed;
    }

    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    public InetSocketAddress getBindAddress() {
        return bindAddress;
    }
}
