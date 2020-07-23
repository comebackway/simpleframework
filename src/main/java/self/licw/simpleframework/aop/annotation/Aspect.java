package self.licw.simpleframework.aop.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Aspect {
    //以注解作为值  eg:@Controller  会将该Aspect注入到所有有@Controller标注的类里
    Class<? extends Annotation> value();
}
