package com.slmdev.jsonapi.simple.annotation;

import org.springframework.data.domain.Pageable;

import java.lang.annotation.*;

/**
 * The annotation extract page fields from request and
 * store them in the {@link Pageable} object.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestJsonApiPage {
    /**
     * The page query param name, default is {@code page}.
     *
     * @return page param name
     */
    String name() default "page";
}
