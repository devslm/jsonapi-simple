package com.slmdev.jsonapi.simple.annotation;

import com.slmdev.jsonapi.simple.request.Filter;

import java.lang.annotation.*;

/**
 * The annotation extract filter fields from request and
 * store them in the {@link Filter} object.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestJsonApiFilter {
    /**
     * The filter query param name, default is {@code filter}.
     *
     * @return filter param name
     */
    String name() default "filter";
}
