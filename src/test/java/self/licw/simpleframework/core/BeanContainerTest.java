package self.licw.simpleframework.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class BeanContainerTest {
    private static BeanContainer beanContainer;
    //该注解类的作用是，在执行所有util测试类时，最开始先执行一次这个
    @BeforeAll
    static void init(){
        beanContainer = BeanContainer.getInstance();
    }

    @Test
    public void loanBeansTest(){
        Assertions.assertEquals(false,beanContainer.isLoaded());
        beanContainer.loadBeans("self.licw.o2o");
        Assertions.assertEquals(6,beanContainer.size());
        Assertions.assertEquals(true,beanContainer.isLoaded());
    }
}
