package self.licw.simpleframework.mvc.processor.impl;

import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import lombok.extern.slf4j.Slf4j;
import self.licw.simpleframework.mvc.RequestProcessorChain;
import self.licw.simpleframework.mvc.processor.RequestProcessor;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;

/**
 * jsp资源请求处理
 */
@Slf4j
public class JspRequestProcessor implements RequestProcessor {
    //jsp请求的RequestDispatcher的名称
    private static final String JSP_SERVLET="jsp";
    //jsp请求资源路径前缀
    private static final String JSP_RESOURCE_PREFIX = "/templates/";
    //jsp的RequestDispatcher，处理jsp资源
    private RequestDispatcher jspServlet;

    public JspRequestProcessor(ServletContext servletContext) {
        this.jspServlet = servletContext.getNamedDispatcher(JSP_SERVLET);
        if (null == jspServlet){
            throw new RuntimeException("there is no jsp servlet");
        }
        log.info("The default servlet for static resource is {}", JSP_SERVLET);
    }

    @Override
    public boolean process(RequestProcessorChain requestProcessorChain) throws Exception {
        if (isJspResource(requestProcessorChain.getRequestPath())) {
            jspServlet.forward(requestProcessorChain.getRequest(), requestProcessorChain.getResponse());
            return false;
        }
        return true;
    }

    /**
     * 是否请求的是jsp资源
     */
    private boolean isJspResource(String url) {
        return url.startsWith(JSP_RESOURCE_PREFIX);
    }
}
