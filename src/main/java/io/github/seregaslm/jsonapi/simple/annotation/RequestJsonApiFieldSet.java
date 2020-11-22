package io.github.seregaslm.jsonapi.simple.annotation;

import io.github.seregaslm.jsonapi.simple.request.FieldSet;

import java.lang.annotation.*;

/**
 * The annotation extract sparse fieldsets from request and
 * store them in the {@link FieldSet} object.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestJsonApiFieldSet {
    /**
     * The sparse fieldsets query param name, default is {@code fields}.
     *
     * @return sparse fieldsets param name
     */
    String name() default "fields";
}
