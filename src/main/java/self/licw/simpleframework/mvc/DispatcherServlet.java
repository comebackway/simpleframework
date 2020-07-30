package self.licw.simpleframework.mvc;

import self.licw.simpleframework.aop.AspectWeaver;
import self.licw.simpleframework.core.BeanContainer;
import self.licw.simpleframework.inject.DependencyInjector;
import self.licw.simpleframework.mvc.processor.RequestProcessor;
import self.licw.simpleframework.mvc.processor.impl.ControllerRequestProcessor;
import self.licw.simpleframework.mvc.processor.impl.JspRequestProcessor;
import self.licw.simpleframework.mvc.processor.impl.PreRequestProcessor;
import self.licw.simpleframework.mvc.processor.impl.StaticResourceRequestProcessor;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/*")
public class DispatcherServlet extends HttpServlet {

    List<RequestProcessor> PROCESSOR = new ArrayList<>();

    @Override
    public void init() throws ServletException {
        //1.初始化容器
        BeanContainer beanContainer = BeanContainer.getInstance();
        beanContainer.loadBeans("self.licw.o2o");
        new AspectWeaver().doAop();
        new DependencyInjector().doIoc();

        //2.初始化请求处理器责任链
        /*
        其中PreRequestProcessor要放前边，因为其责任链要做预处理
        ControllerRequestProcessor要放后边，因为其处理逻辑复杂，消耗时间较长
         */
        PROCESSOR.add(new PreRequestProcessor());
        PROCESSOR.add(new StaticResourceRequestProcessor(getServletContext()));
        PROCESSOR.add(new JspRequestProcessor(getServletContext()));
        PROCESSOR.add(new ControllerRequestProcessor());

    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //1.创建责任链对象实例
        RequestProcessorChain requestProcessorChain = new RequestProcessorChain(PROCESSOR.iterator(),req,resp);
        //2.通过责任链模式依次调用请求处理器对请求进行处理
        requestProcessorChain.doRequestProcessorChain();
        //3.通过第二步得到的render后，对处理结果进行渲染
        requestProcessorChain.doRender();
    }
}
