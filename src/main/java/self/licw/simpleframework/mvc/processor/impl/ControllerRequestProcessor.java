package self.licw.simpleframework.mvc.processor.impl;

import lombok.extern.slf4j.Slf4j;
import self.licw.simpleframework.core.BeanContainer;
import self.licw.simpleframework.mvc.RequestProcessorChain;
import self.licw.simpleframework.mvc.annotation.RequestMapping;
import self.licw.simpleframework.mvc.annotation.RequestParam;
import self.licw.simpleframework.mvc.annotation.ResponseBody;
import self.licw.simpleframework.mvc.processor.RequestProcessor;
import self.licw.simpleframework.mvc.render.ResultRender;
import self.licw.simpleframework.mvc.render.impl.JsonRender;
import self.licw.simpleframework.mvc.render.impl.ResourceNotFoundResultRender;
import self.licw.simpleframework.mvc.render.impl.ViewResultRender;
import self.licw.simpleframework.mvc.type.ControllerMethod;
import self.licw.simpleframework.mvc.type.RequestPathInfo;
import self.licw.simpleframework.util.ConverterUtil;
import self.licw.simpleframework.util.ValidationUtil;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * controller请求处理器
 */
@Slf4j
public class ControllerRequestProcessor implements RequestProcessor {
    //IOC容器
    private BeanContainer beanContainer;
    //客户端请求和controller方法的映射集合
    private Map<RequestPathInfo, ControllerMethod> pathAndControllerMethodMap = new ConcurrentHashMap<>();

    /**
     * 依靠容器的能力，建立起请求路径，请求方法与controller方法实例的映射
     */
    public ControllerRequestProcessor() {
        this.beanContainer = BeanContainer.getInstance();
        //获取所有被@RequestMapping标识的类
        Set<Class<?>> requestMappingSet = beanContainer.getClassesByAnnotation(RequestMapping.class);
        initPathControllerMethodMap(requestMappingSet);
    }

    /**
     * 将requestmapping的url和请求方法 与 controller做绑定
     *
     * @param requestMappingSet
     */
    private void initPathControllerMethodMap(Set<Class<?>> requestMappingSet) {
        if (ValidationUtil.isEmpty(requestMappingSet)) {
            return;
        }
        //1.遍历所有被@RequestMapping标记的类
        for (Class<?> requestMappingClass : requestMappingSet) {
            //2.获取类上边该注解的属性值作为一级路径
            RequestMapping requestMapping = requestMappingClass.getAnnotation(RequestMapping.class);
            String basePath = requestMapping.value();
            //统一路径以/开头 方便后续处理
            if (!basePath.startsWith("/")) {
                basePath = "/" + basePath;
            }
            //3.遍历类里所有被@RequestMapping标记的方法，获取方法上面的注解属性值作为二级路径
            Method[] methods = requestMappingClass.getDeclaredMethods();
            if (ValidationUtil.isEmpty(methods)) {
                continue;
            }
            for (Method method : methods) {
                //判断该方法是否被@RequestMapping标注
                if (method.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping methodRequest = method.getAnnotation(RequestMapping.class);
                    String methodPath = methodRequest.value();
                    if (!methodPath.startsWith("/")) {
                        methodPath = "/" + methodPath;
                    }
                    String url = basePath + methodPath;
                    //4.解析该方法里被@RequestParam标记的参数
                    //获取该注解的属性值————参数名（key）   参数的数据类型作为value
                    Map<String, Class<?>> methodParams = new HashMap<>();
                    Parameter[] parameters = method.getParameters();
                    if (!ValidationUtil.isEmpty(parameters)) {

                        for (Parameter parameter : parameters) {
                            RequestParam param = parameter.getAnnotation(RequestParam.class);
                            //暂定controller方法里所有的参数都需要@RequestParam注解
                            if (param == null) {
                                throw new RuntimeException("parameter must have @RequestParam");
                            }
                            methodParams.put(param.value(), parameter.getType());
                        }
                    }
                    //5.将获取到的信息封装成RequestPathInfo实例和ControllerMethod实例，放到映射表里
                    String httpMethod = String.valueOf(methodRequest.method());
                    RequestPathInfo requestPathInfo = new RequestPathInfo(httpMethod, url);
                    //如果之前存在了该key（匹配路径和方法被写了两次或以上）  则直接覆盖
                    if (this.pathAndControllerMethodMap.containsKey(requestPathInfo)) {
                        log.warn("url:{}   /   current class:{}   /  method:{}   override another",
                                requestPathInfo.getHttpPath(), requestMappingClass.getName(), method.getName());
                    }
                    ControllerMethod controllerMethod = new ControllerMethod(requestMappingClass, method, methodParams);
                    this.pathAndControllerMethodMap.put(requestPathInfo, controllerMethod);
                }
            }
        }
    }

    @Override
    public boolean process(RequestProcessorChain requestProcessorChain) throws Exception {
        //1.解析HttpServletRequest的请求方法(GET/POST)，请求路径，获取对应的ControllerMethod实例
        String method = requestProcessorChain.getRequestMethod();
        String path = requestProcessorChain.getRequestPath();
        ControllerMethod controllerMethod = this.pathAndControllerMethodMap.get(new RequestPathInfo(method, path));
        if (controllerMethod == null) {
            requestProcessorChain.setResultRender(new ResourceNotFoundResultRender(path,method));
            return false;
        }
        //2.解析请求参数，并传递参数给获取到的ControllerMethod实例去执行
        Object result = invokeControllerMethod(controllerMethod, requestProcessorChain.getRequest());
        //3.根据处理的结果，选择对应的render进行渲染
        setResultRender(result, controllerMethod, requestProcessorChain);
        return true;
    }

    /**
     * 根据不同的情况 如返回值是否为空 是否又@ResponseBody注解标识等情况，返回不同的渲染器render
     *
     * @param result
     * @param controllerMethod
     * @param requestProcessorChain
     */
    private void setResultRender(Object result, ControllerMethod controllerMethod, RequestProcessorChain requestProcessorChain) {
        if (result == null) {
            //使用默认的渲染器
            return;
        }
        ResultRender resultRender;
        boolean isJson = controllerMethod.getInvokeMethod().isAnnotationPresent(ResponseBody.class);
        if (isJson) {
            resultRender = new JsonRender(result);
        } else {
            resultRender = new ViewResultRender(result);
        }
        requestProcessorChain.setResultRender(resultRender);
    }

    /**
     * @param controllerMethod
     * @param request
     * @return
     */
    private Object invokeControllerMethod(ControllerMethod controllerMethod, HttpServletRequest request) {
        //1.从请求里获取GET或者POST的参数名及其对应的值（因为request.getParameterMap()只能获取GET/POST请求过来的所有参数）
        Map<String, String> requestParamMap = new HashMap<>();
        //获取GET/POST请求的所有参数
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (Map.Entry<String, String[]> parameter : parameterMap.entrySet()) {
            if (!ValidationUtil.isEmpty(parameter.getValue())) {
                //这里设置只支持一个参数对应一个值的形式
                requestParamMap.put(parameter.getKey(), parameter.getValue()[0]);
            }
        }
        //2.根据上边获取到的请求参数名和对应的值，以及之前的controllermethod里边的参数名和类型的映射关系 实例化出方法对应的参数
        List<Object> methodParams = new ArrayList<>();
        Map<String, Class<?>> methodParamMap = controllerMethod.getMethodParameters();
        //循环该方法里对应的每一个参数名
        for (String paramName : methodParamMap.keySet()) {
            Class<?> type = methodParamMap.get(paramName);
            String requestvalue = requestParamMap.get(paramName);
            Object value;
            if (requestvalue == null) {
                //将请求里的参数转成是培育参数类型的空值
                value = ConverterUtil.primitveNull(type);
            } else {
                value = ConverterUtil.convert(type, requestvalue);
            }
            methodParams.add(value);
        }
        //3.使用反射执行controller里对应的方法并返回结果
        Object controller = beanContainer.getBean(controllerMethod.getControllerClass());
        Method invokeMethod = controllerMethod.getInvokeMethod();
        invokeMethod.setAccessible(true);
        Object result;
        try {
            if (methodParams.size() == 0) {
                result = invokeMethod.invoke(controller);
            } else {
                result = invokeMethod.invoke(controller, methodParams.toArray());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
