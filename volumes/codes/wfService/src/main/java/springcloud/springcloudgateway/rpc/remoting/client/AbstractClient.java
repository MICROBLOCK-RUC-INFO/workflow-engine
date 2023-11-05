package springcloud.springcloudgateway.rpc.remoting.client;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import springcloud.springcloudgateway.rpc.codec.Response;
import springcloud.springcloudgateway.rpc.remoting.MyChannelHandler;
import springcloud.springcloudgateway.rpc.remoting.RemotingException;
import springcloud.springcloudgateway.rpc.utils.NetUtils;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractClient implements MyChannelHandler {

    private static final Logger logger = LoggerFactory.getLogger(AbstractClient.class);

    protected final String remotHost;

    protected final Integer port;

    private final Lock connectLock = new ReentrantLock();

    protected int connectTimeout;

    protected boolean closed;

    public AbstractClient(String remotHost, Integer port, int connectTimeout) throws RemotingException {
        this.remotHost = remotHost;
        this.port = port;
        this.connectTimeout = connectTimeout;
        try {
            doOpen();
        } catch (Throwable t) {
            close();
            throw new RemotingException(null, null,
                    "Failed to start " + getClass().getSimpleName() + " " + NetUtils.getLocalAddress()
                            + " connect to the server " + getRemoteAddress() + ", cause: " + t.getMessage(), t);
        }

        try {
            connect();
            if (logger.isInfoEnabled()) {
                logger.info("Start " + getClass().getSimpleName() + " " + NetUtils.getLocalAddress() + " connect to the server " + getRemoteAddress());
            }
        } catch (Throwable t) {
            close();
            throw new RemotingException(null, null,
                    "Failed to start " + getClass().getSimpleName() + " " + NetUtils.getLocalAddress()
                            + " connect to the server " + getRemoteAddress() + ", cause: " + t.getMessage(), t);
        }
    }

    @Override
    public void connected(Channel channel) throws ExecutionException {
        if (closed) {
            return;
        }
        logger.info("succed connected to " + channel.remoteAddress());
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
        Response response = (Response) message;
        DefaultFuture.received(channel, response);
    }

    @Override
    public void close() {
        try {
            disconnect();
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
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

    protected void connect() throws RemotingException {
        connectLock.lock();
        try {

            if (isConnected()) {
                return;
            }
            doConnect();
            if (!isConnected()) {
                throw new RemotingException(getChannel(), "Failed connect to server " + getRemoteAddress() + " from " + getClass().getSimpleName() + " "
                        + NetUtils.getLocalHost() + ", cause: Connect wait timeout: " + getConnectTimeout() + "ms.");

            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("Succeed connect to server " + getRemoteAddress() + " from " + getClass().getSimpleName() + " "
                            + NetUtils.getLocalHost() + ", channel is " + this.getChannel());
                }
            }

        } catch (RemotingException e) {
            throw e;
        } catch (Throwable e) {
            throw new RemotingException(getChannel(), "Failed connect to server " + getRemoteAddress() + " from " + getClass().getSimpleName() + " "
                    + NetUtils.getLocalHost() + " using dubbo version " + ", cause: " + e.getMessage(), e);

        } finally {
            connectLock.unlock();
        }
    }

    public void disconnect() {
        connectLock.lock();
        try {
            try {
                Channel channel = getChannel();
                if (channel != null) {
                    channel.close();
                }
            } catch (Throwable e) {
                logger.warn(e.getMessage(), e);
            }
            try {
                doDisConnect();
            } catch (Throwable e) {
                logger.warn(e.getMessage(), e);
            }
        } finally {
            connectLock.unlock();
        }
    }

    public InetSocketAddress getConnectAddress() {
        return new InetSocketAddress(remotHost, port);
    }

    public InetSocketAddress getRemoteAddress() {
        Channel channel = getChannel();
        return (InetSocketAddress) channel.remoteAddress();
    }

    public InetSocketAddress getLocalAddress() {
        Channel channel = getChannel();
        return (InetSocketAddress) channel.localAddress();
    }

    public boolean isConnected() {
        Channel channel = getChannel();
        if (channel == null) {
            return false;
        }
        return channel.isActive();
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public boolean isClosed() {
        return closed;
    }

    /**
     * Open client.
     *
     * @throws Throwable
     */
    protected abstract void doOpen() throws Throwable;

    /**
     * Close client.
     *
     * @throws Throwable
     */
    protected abstract void doClose() throws Throwable;

    /**
     * Connect to server.
     *
     * @throws Throwable
     */
    protected abstract void doConnect() throws Throwable;

    /**
     * disConnect to server.
     *
     * @throws Throwable
     */
    protected abstract void doDisConnect() throws Throwable;

    /**
     * Get the connected channel.
     *
     * @return channel
     */
    protected abstract Channel getChannel();

    public abstract DefaultFuture send(Object msg) throws Throwable;

}
