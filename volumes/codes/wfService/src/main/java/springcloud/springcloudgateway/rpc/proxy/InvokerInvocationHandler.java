package springcloud.springcloudgateway.rpc.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import springcloud.springcloudgateway.rpc.codec.InvokerObject;
import springcloud.springcloudgateway.rpc.codec.Request;
import springcloud.springcloudgateway.rpc.remoting.client.AbstractClient;
import springcloud.springcloudgateway.rpc.remoting.client.DefaultFuture;
import springcloud.springcloudgateway.rpc.remoting.client.NettyClientManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * InvokerHandler
 */
public class InvokerInvocationHandler implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(InvokerInvocationHandler.class);

    private String interfaceName;

    public InvokerInvocationHandler(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
//    if (method.getDeclaringClass() == Object.class) {
//      return method.invoke(invoker, args);
//    }
        if ("toString".equals(methodName) && parameterTypes.length == 0) {
            return proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(proxy)) + ", with InvocationHandler " + this;
        }
        if ("hashCode".equals(methodName) && parameterTypes.length == 0) {
            return System.identityHashCode(proxy);
        }
        if ("equals".equals(methodName) && parameterTypes.length == 1) {
            return proxy == args[0];
        }

        Request request = new Request();
        InvokerObject invoker = new InvokerObject();
        invoker.setClassName(interfaceName);
        invoker.setMethodName(methodName);
        invoker.setParameters(args);
        invoker.setParameterTypes(parameterTypes);
        request.setmData(invoker);
        //balance client
        AbstractClient client = NettyClientManager.getInstance().balanceClient();
        DefaultFuture f = client.send(request);
        Object result = f.get();
        return result;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }
}
