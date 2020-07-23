package self.licw.simpleframework.aop;

import self.licw.simpleframework.aop.annotation.Aspect;
import self.licw.simpleframework.aop.annotation.Order;
import self.licw.simpleframework.aop.aspect.AspectInfo;
import self.licw.simpleframework.aop.aspect.DefaultAspect;
import self.licw.simpleframework.core.BeanContainer;
import self.licw.simpleframework.util.ValidationUtil;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * 按业务逻辑对不同的类织入不同的aspect
 * 从bean容器里边获得被代理类 aspect 以及aspect织入被代理类生成代理类实例等过程
 */
public class AspectWeaver {
    private BeanContainer beanContainer;
    public AspectWeaver(){
        this.beanContainer = BeanContainer.getInstance();
    }

    public void doAop(){
        //1.获取所有切面类 ASPECT标签标记的类
        Set<Class<?>> aspectSet = beanContainer.getClassesByAnnotation(Aspect.class);
        //2.将切面类按照不同的织入目标进行切分（也就是按照@Aspect里边的value切分）
        /**
         * categorizedMap 存着某一个注解下（如@Controller） 所对应的aspectinfo对象集合
         * 也即是该注解对应着多少个切面类
         */
        Map<Class<? extends Annotation>, List<AspectInfo>> categorizedMap = new HashMap<>();
        if (ValidationUtil.isEmpty(aspectSet)){
            return;
        }
        for (Class<?> asspectClass:aspectSet){
            if (verifyAspect(asspectClass)){
                cateorizedAspect(categorizedMap,asspectClass);
            }else {
                throw new RuntimeException("verifyAspect failed");
            }
        }
        //3.按照不同的织入目标分别按序织入Aspect逻辑
        if (ValidationUtil.isEmpty(categorizedMap)){return;}
        for (Class<? extends Annotation> category:categorizedMap.keySet()){
            weaveByCategory(category,categorizedMap.get(category));
        }
    }


    //校验Aspect类
    //规则一：aspect类要添加@Aspect和@Order
    //规则二：继承DefaultAspect.class
    //规则三：@Aspect属性值不能是其本身
    private boolean verifyAspect(Class<?> aspectClass) {
        return aspectClass.isAnnotationPresent(Aspect.class) &&
                aspectClass.isAnnotationPresent(Order.class) &&
                //满足DefaultAspect的子类有aspectClass
                DefaultAspect.class.isAssignableFrom(aspectClass) &&
                aspectClass.getAnnotation(Aspect.class).value() != Aspect.class;
    }


    /**
     * 根据某一个Aspect类 得到其order和aspect属性，并将对应的信息放入到categorizedMap中
     * @param categorizedMap
     * @param asspectClass
     */
    private void cateorizedAspect(Map<Class<? extends Annotation>, List<AspectInfo>> categorizedMap, Class<?> asspectClass) {
        Order orderTag = asspectClass.getAnnotation(Order.class);
        Aspect aspectTag = asspectClass.getAnnotation(Aspect.class);
        DefaultAspect aspect = (DefaultAspect)beanContainer.getBean(asspectClass);
        AspectInfo aspectInfo = new AspectInfo(orderTag.value(),aspect);
        //如果map中还没存在该joinpoint（也就是@Controller @Service 之类的标注）
        if (!categorizedMap.containsKey(aspectTag.value())){
            List<AspectInfo> aspectInfos = new ArrayList<>();
            aspectInfos.add(aspectInfo);
            categorizedMap.put(aspectTag.value(),aspectInfos);
        }else{
            //如果该键位joinpoint不是第一次出现，则直接加入新的aspectinfo即可
            List<AspectInfo> aspectInfos = categorizedMap.get(aspectTag.value());
            aspectInfos.add(aspectInfo);
        }

    }


    /**
     * 为被标注了joinpot的被代理类生成代理类实例
     * @param category      要织入的joinpoint
     * @param aspectInfos   该joinpoint对应的aspectinfo信息对象列表
     */
    private void weaveByCategory(Class<? extends Annotation> category, List<AspectInfo> aspectInfos) {
        //1.获取被代理类的集合
        Set<Class<?>> classSet = beanContainer.getClassesByAnnotation(category);
        if (ValidationUtil.isEmpty(classSet)){return;}
        //2。遍历被代理类，分别为每个被代理类生成动态代理实例
        for (Class<?> targetClass:classSet){
            AspectListExecutor aspectListExecutor = new AspectListExecutor(targetClass,aspectInfos);
            Object proxybean = ProxyCreator.createProxy(targetClass,aspectListExecutor);
            //3.将动态代理对象实例添加到容器，取代未被代理前的类实例
            beanContainer.addBean(targetClass,proxybean);
        }
    }

}
