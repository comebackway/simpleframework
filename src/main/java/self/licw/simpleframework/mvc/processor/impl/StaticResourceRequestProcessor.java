package self.licw.simpleframework.mvc.processor.impl;

import lombok.extern.slf4j.Slf4j;
import self.licw.simpleframework.mvc.RequestProcessorChain;
import self.licw.simpleframework.mvc.processor.RequestProcessor;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;

/**
 * 静态资源请求处理，包括但不限于图片，css，js文件等
 */
@Slf4j
public class StaticResourceRequestProcessor implements RequestProcessor {
    //静态资源请求的RequestDispatcher名称
    public static final String DEFAULT_TOMCAT_SERVLET = "default";
    //静态资源请求资源路径前缀
    public static final String STATIC_RESOURCE_PREFIX = "/static/";
    //tomcat 默认请求派发器 RequestDispatcher的名称
    RequestDispatcher defaultDispatcher;

    public StaticResourceRequestProcessor (ServletContext servletContext){
        //servletContext.getNamedDispatcher()  这里的参数必须是一个servlet的名字在web.xml定义
        this.defaultDispatcher = servletContext.getNamedDispatcher(DEFAULT_TOMCAT_SERVLET);
        if (this.defaultDispatcher == null){
            throw new RuntimeException("There is no default tomcat servlet");
        }
        log.info("The default servlet for static resource is {}", DEFAULT_TOMCAT_SERVLET);
    }

    @Override
    public boolean process(RequestProcessorChain requestProcessorChain) throws Exception {
        //1.通过请求路径判断是否时请求的静态资源 /webapp/static
        if (isStaticResource(requestProcessorChain.getRequestPath())){
            //2.如果是静态资源，则将请求转发给defalut servlet处理
            defaultDispatcher.forward(requestProcessorChain.getRequest(),requestProcessorChain.getResponse());
            return false;
        }
        //进入下一个处理器
        return true;
    }

    //通过请求路径前缀（目录） 是否为静态资源 /static/
    private boolean isStaticResource(String requestPath) {
        return requestPath.startsWith(STATIC_RESOURCE_PREFIX);
    }


}
