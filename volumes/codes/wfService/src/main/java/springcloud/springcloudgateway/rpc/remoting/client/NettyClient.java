package springcloud.springcloudgateway.rpc.remoting.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import springcloud.springcloudgateway.rpc.codec.Request;
import springcloud.springcloudgateway.rpc.codec.RpcDecoder;
import springcloud.springcloudgateway.rpc.codec.RpcEncoder;
import springcloud.springcloudgateway.rpc.remoting.RemotingException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static springcloud.springcloudgateway.rpc.utils.GloabalConstants.DEFAULT_IO_THREADS;

/**
 * NettyClient.
 */
public class NettyClient extends AbstractClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private NioEventLoopGroup nioEventLoopGroup;

    private Bootstrap bootstrap;

    private Channel channel;

    public NettyClient(String host, Integer port, int connectTimeout) throws RemotingException {
        super(host, port, connectTimeout);
    }

    @Override
    protected void doOpen() throws Throwable {
        NettyClientHandler nettyClientHandler = new NettyClientHandler(this);
        bootstrap = new Bootstrap();
        nioEventLoopGroup = new NioEventLoopGroup(DEFAULT_IO_THREADS, new DefaultThreadFactory("NettyClientWorker", true));
        bootstrap.group(nioEventLoopGroup)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                //.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, getTimeout())
                .channel(NioSocketChannel.class);

        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline()
                        .addLast(new RpcDecoder())
                        .addLast(new RpcEncoder())
                        .addLast(nettyClientHandler);
            }
        });
    }

    @Override
    protected void doConnect() throws Throwable {
        ChannelFuture future = bootstrap.connect(getConnectAddress());
        try {
            boolean ret = future.awaitUninterruptibly(3000L, MILLISECONDS);
            if (ret && future.isSuccess()) {
                Channel newChannel = future.channel();
                try {
                    // Close old channel
                    // copy reference
                    Channel oldChannel = NettyClient.this.channel;
                    if (oldChannel != null) {
                        try {
                            if (logger.isInfoEnabled()) {
                                logger.info(
                                        "Close old netty channel " + oldChannel + " on create new netty channel "
                                                + newChannel);
                            }
                            oldChannel.close();
                        } finally {
//              NettyChannel.removeChannelIfDisconnected(oldChannel);
                        }
                    }
                } finally {
                    if (NettyClient.this.isClosed()) {
                        try {
                            if (logger.isInfoEnabled()) {
                                logger
                                        .info("Close new netty channel " + newChannel + ", because the client closed.");
                            }
                            newChannel.close();
                        } finally {
                            NettyClient.this.channel = null;
                        }
                    } else {
                        NettyClient.this.channel = newChannel;
                    }
                }
            }
        } finally {
            // just add new valid channel to NettyChannel's cache
            if (!isConnected()) {
                //future.cancel(true);
            }
        }

    }

    @Override
    protected void doClose() throws Throwable {
        nioEventLoopGroup.shutdownGracefully();
    }

    @Override
    protected void doDisConnect() throws Throwable {
    }

    @Override
    protected Channel getChannel() {
        return channel;
    }

    @Override
    public DefaultFuture send(Object msg) throws Throwable {
        Request request = (Request) msg;
        if (!isConnected()) {
            connect();
        }

        DefaultFuture f = DefaultFuture.newFuture(getChannel(), request, 300000);
        try {
            getChannel().writeAndFlush(msg);
        } catch (Exception e) {
            f.cancel();
        }
        return f;
    }
}
