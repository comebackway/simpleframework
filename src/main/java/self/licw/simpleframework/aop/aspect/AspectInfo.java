package self.licw.simpleframework.aop.aspect;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 将@Order和@Aspect 封装在一起
 * orderIndex：@Order的值
 * aspect    ：要织入的aspect
 */

@AllArgsConstructor
@Getter
public class AspectInfo {
    private int orderIndex;
    private DefaultAspect aspect;
}
