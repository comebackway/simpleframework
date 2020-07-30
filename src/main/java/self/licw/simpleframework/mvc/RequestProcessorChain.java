package self.licw.simpleframework.mvc;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import self.licw.simpleframework.mvc.processor.RequestProcessor;
import self.licw.simpleframework.mvc.render.ResultRender;
import self.licw.simpleframework.mvc.render.impl.DefaultRequestRender;
import self.licw.simpleframework.mvc.render.impl.InternalErrorResultRender;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;

@Data
@Slf4j
public class RequestProcessorChain {
    //请求处理器迭代器
    private Iterator<RequestProcessor> requestProcessorIterator;
    //请求request
    private HttpServletRequest request;
    //响应response
    private HttpServletResponse response;


    //http请求方法
    private String requestMethod;
    //http请求路径
    private String requestPath;
    //http响应状态码
    private int responseCode;
    //对应本次请求的结果渲染器实例
    private ResultRender resultRender;

    public RequestProcessorChain(Iterator<RequestProcessor> requestProcessorIterator, HttpServletRequest request, HttpServletResponse response) {
        this.requestProcessorIterator = requestProcessorIterator;
        this.request = request;
        this.response = response;

        this.requestMethod = request.getMethod();
        this.requestPath = request.getPathInfo();
        this.responseCode = HttpServletResponse.SC_OK;
    }

    /**
     * 以责任链的模式执行请求链
     */
    public void doRequestProcessorChain() {
        try {
            //1.通过迭代器遍历注册的请求处理器实现类列表
            while (requestProcessorIterator.hasNext()) {
                //2.直到某个请求处理器执行完后返回false为止
                if (!requestProcessorIterator.next().process(this)){
                    break;
                }
            }
        }catch (Exception e){
            //3.期间如果内部出现异常，则交由内部异常渲染器处理
            this.resultRender = new InternalErrorResultRender(e.getMessage()) ;
            log.error("doRequestProcessorChain error:",e);
        }
    }

    /**
     * 执行渲染器
     */
    public void doRender() {
        //1.如果请求处理器实现类未选择合适的渲染器，则使用默认的
        if (this.resultRender == null){
            this.resultRender = new DefaultRequestRender();
        }
        //2.调用渲染器的render方法对结果进行渲染
        try {
            this.resultRender.render(this);
        } catch (Exception e) {
            log.error("doRender error:",e);
            throw new RuntimeException(e);
        }
    }
}
