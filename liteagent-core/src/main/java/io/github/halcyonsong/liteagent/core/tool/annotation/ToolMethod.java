package io.github.halcyonsong.liteagent.core.tool.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 工具方法标记。
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ToolMethod {

    /**
     * 工具名称。
     */
    String name();

    /**
     * 工具描述。
     */
    String description() default "";
}