package io.github.halcyonsong.liteagent.core.tool.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 工具参数标记。
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ToolParam {

    /** 不填时优先使用反射参数名。 */
    String name() default "";

    String description() default "";

    /** 默认必填；如果是可选参数，显式设置为 false。 */
    boolean required() default true;
}