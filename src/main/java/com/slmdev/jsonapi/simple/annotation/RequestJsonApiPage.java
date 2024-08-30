package com.slmdev.jsonapi.simple.annotation;

import java.lang.annotation.*;

/**
 * The annotation extract page fields from request and
 * store them in the {@link org.springframework.data.domain.Pageable} object.
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
