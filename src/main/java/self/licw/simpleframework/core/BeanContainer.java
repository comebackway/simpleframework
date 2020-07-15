package self.licw.simpleframework.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import self.licw.simpleframework.core.annotation.Component;
import self.licw.simpleframework.core.annotation.Controller;
import self.licw.simpleframework.core.annotation.Repository;
import self.licw.simpleframework.core.annotation.Service;
import self.licw.simpleframework.util.ClassUtil;
import self.licw.simpleframework.util.ValidationUtil;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BeanContainer {
    /**
     * 设置该容器是否已经被加载过
     */
    private boolean loaded = false;

    public boolean isLoaded(){
        return loaded;
    }

    /**
     * 存放所有被配置标记的目标对象的Map
     * ConcurrentHashMap 能更好地支持并发
     * key:class对象    value:对象的实例
     */
    private final Map<Class<?>,Object> beanMap = new ConcurrentHashMap();

    /**
     * 加载bean的注解列表，标识了该注解的类（bean）都要被容器所管理
     */
    private static final List<Class<? extends Annotation>> BEAN_ANNOTATION = Arrays.asList(Component.class, Controller.class, Repository.class, Service.class);


    /**
     * 获取bean容器实例
     * @return
     */
    public static BeanContainer getInstance(){
        return ContainerHolder.HOLDER.instance;
    }

    private enum ContainerHolder{
        HOLDER;
        private BeanContainer instance;
        ContainerHolder(){
            instance = new BeanContainer();
        }
    }

    /**
     * 扫描加载指定包名下的所有bean
     * 方法中使用同步锁synchronized 这样就能保证该方法只有一个线程在执行，从而保证loadbean只会被执行一次
     * @param basepackage
     */
    public synchronized void loadBeans(String basepackage){
        if (isLoaded()){
            log.warn("beancontainer has been loaded.");
            return;
        }
        Set<Class<?>> classSet = ClassUtil.extractPackageClass(basepackage);
        if (ValidationUtil.isEmpty(classSet)){
            log.warn("nothing from package" + basepackage);
            return;
        }
        for (Class<?> clazz:classSet){
            //判断类是否标记了定义的注解
            for (Class<? extends Annotation> annotation:BEAN_ANNOTATION){
                //标记了则将目标类本身作为键，目标类的实例作为值，放入到beanMap中
                if (clazz.isAnnotationPresent(annotation)){
                    beanMap.put(clazz,ClassUtil.newInstance(clazz,true));
                }
            }
        }
        loaded = true;
    }

    public int size(){
        return beanMap.size();
    }
}
