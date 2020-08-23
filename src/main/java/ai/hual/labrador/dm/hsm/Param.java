package ai.hual.labrador.dm.hsm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static ai.hual.labrador.dm.hsm.ComponentRenderUtils.PLAIN_FORM;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Param {

    /**
     * key of the param
     * empty string for the field name
     */
    String key() default "";

    boolean required() default true;

    String defaultValue() default "";

    String[] range() default {};

    // tip message to explain this param
    String tip() default "";

    // component to use in front end
    String component() default PLAIN_FORM;
}
