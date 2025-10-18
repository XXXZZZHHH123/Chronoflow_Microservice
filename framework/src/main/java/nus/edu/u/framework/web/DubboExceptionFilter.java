package nus.edu.u.framework.web;

import static org.apache.dubbo.common.constants.LoggerCodeConstants.CONFIG_FILTER_VALIDATION_EXCEPTION;

import java.lang.reflect.Method;
import nus.edu.u.common.exception.ServiceException;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.common.logger.ErrorTypeAwareLogger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.common.utils.ReflectUtils;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.service.GenericService;
import org.apache.dubbo.rpc.support.RpcUtils;

/**
 * @author Lu Shuwen
 * @date 2025-10-17
 */
@Activate(group = CommonConstants.PROVIDER)
public class DubboExceptionFilter implements Filter, Filter.Listener {
    private ErrorTypeAwareLogger logger =
            LoggerFactory.getErrorTypeAwareLogger(DubboExceptionFilter.class);

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        return invoker.invoke(invocation);
    }

    @Override
    public void onResponse(Result appResponse, Invoker<?> invoker, Invocation invocation) {
        if (appResponse.hasException() && GenericService.class != invoker.getInterface()) {
            try {
                Throwable exception = appResponse.getException();

                // 如果检查异常则直接抛出
                if (!(exception instanceof RuntimeException) && (exception instanceof Exception)) {
                    return;
                }

                // 如果签名中出现异常，则直接抛出
                try {
                    Method method =
                            invoker.getInterface()
                                    .getMethod(
                                            RpcUtils.getMethodName(invocation),
                                            invocation.getParameterTypes());
                    Class<?>[] exceptionClasses = method.getExceptionTypes();
                    for (Class<?> exceptionClass : exceptionClasses) {
                        if (exception.getClass().equals(exceptionClass)) {
                            return;
                        }
                    }
                } catch (NoSuchMethodException e) {
                    return;
                }

                // 对于在方法的签名中找不到的异常，请在服务器的日志中打印ERROR消息。
                logger.error(
                        CONFIG_FILTER_VALIDATION_EXCEPTION,
                        "",
                        "",
                        "Got unchecked and undeclared exception which called by "
                                + RpcContext.getServiceContext().getRemoteHost()
                                + ". service: "
                                + invoker.getInterface().getName()
                                + ", method: "
                                + RpcUtils.getMethodName(invocation)
                                + ", exception: "
                                + exception.getClass().getName()
                                + ": "
                                + exception.getMessage(),
                        exception);

                // 如果异常类和接口类在同一个jar文件中，则直接抛出。
                String serviceFile = ReflectUtils.getCodeBase(invoker.getInterface());
                String exceptionFile = ReflectUtils.getCodeBase(exception.getClass());
                if (serviceFile == null
                        || exceptionFile == null
                        || serviceFile.equals(exceptionFile)) {
                    return;
                }
                // 如果是JDK异常，则直接抛出
                String className = exception.getClass().getName();
                if (className.startsWith("java.")
                        || className.startsWith("javax.")
                        || className.startsWith("jakarta.")) {
                    return;
                }
                // 如果是dubbo异常，则直接抛出
                if (exception instanceof RpcException) {
                    return;
                }

                // 自定义异常直接返回
                if (exception instanceof ServiceException) {
                    return;
                }

                // 否则，请使用RuntimeException进行包装并返回到客户端
                appResponse.setException(new RuntimeException(StringUtils.toString(exception)));
            } catch (Throwable e) {
                logger.warn(
                        CONFIG_FILTER_VALIDATION_EXCEPTION,
                        "",
                        "",
                        "Fail to ExceptionFilter when called by "
                                + RpcContext.getServiceContext().getRemoteHost()
                                + ". service: "
                                + invoker.getInterface().getName()
                                + ", method: "
                                + RpcUtils.getMethodName(invocation)
                                + ", exception: "
                                + e.getClass().getName()
                                + ": "
                                + e.getMessage(),
                        e);
            }
        }
    }

    @Override
    public void onError(Throwable e, Invoker<?> invoker, Invocation invocation) {
        logger.error(
                CONFIG_FILTER_VALIDATION_EXCEPTION,
                "",
                "",
                "Got unchecked and undeclared exception which called by "
                        + RpcContext.getServiceContext().getRemoteHost()
                        + ". service: "
                        + invoker.getInterface().getName()
                        + ", method: "
                        + RpcUtils.getMethodName(invocation)
                        + ", exception: "
                        + e.getClass().getName()
                        + ": "
                        + e.getMessage(),
                e);
    }

    // For test purpose
    public void setLogger(ErrorTypeAwareLogger logger) {
        this.logger = logger;
    }
}
