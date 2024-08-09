package moe.nea.firmament.init;

import net.fabricmc.loader.api.MappingResolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects the intermediary name of the given field into this field by replacing its initializer with a call to
 * {@link MappingResolver#mapClassName(String, String)}
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface IntermediaryName {
    //    String method() default "";
//
//    String field() default "";
    Class<?> value();
}
