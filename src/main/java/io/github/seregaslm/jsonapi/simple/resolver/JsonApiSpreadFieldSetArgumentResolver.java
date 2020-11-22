package io.github.seregaslm.jsonapi.simple.resolver;

import io.github.seregaslm.jsonapi.simple.annotation.RequestJsonApiFieldSet;
import io.github.seregaslm.jsonapi.simple.request.FieldSet;
import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class JsonApiSpreadFieldSetArgumentResolver implements HandlerMethodArgumentResolver {
    private static final String REQUEST_FIELD_SET_KEY_BRACKET_START = "[";
    private static final String REQUEST_FIELD_SET_KEY_BRACKET_END = "]";

    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(RequestJsonApiFieldSet.class) != null;
    }

    public Object resolveArgument(final MethodParameter methodParameter,
                                  final ModelAndViewContainer modelAndViewContainer,
                                  final NativeWebRequest nativeWebRequest,
                                  final WebDataBinderFactory webDataBinderFactory) {
        final RequestJsonApiFieldSet requestJsonApiFieldSet = methodParameter.getParameterAnnotation(RequestJsonApiFieldSet.class);
        final Map<String, Set<String>> fieldSet = new HashMap<>();
        final String fieldSetKeyStart = requestJsonApiFieldSet.name() + REQUEST_FIELD_SET_KEY_BRACKET_START;

        nativeWebRequest.getParameterMap()
            .entrySet()
            .stream()
            .filter(entry -> entry.getKey().startsWith(fieldSetKeyStart)
                && entry.getKey().contains(REQUEST_FIELD_SET_KEY_BRACKET_END))
            .forEach(entry -> {
                final String[] items = entry.getKey().split("\\" + REQUEST_FIELD_SET_KEY_BRACKET_START);
                final String resourceType = items[1].replace(REQUEST_FIELD_SET_KEY_BRACKET_END, "");

                if (!StringUtils.hasText(resourceType)) {
                    throw new IllegalArgumentException(
                        "Could not prepare spread fields set! " +
                            "Argument format wrong! Valid format example: fields[resource-type]=field_1,field_2"
                    );
                } else if (entry.getValue().length == 1) {
                    fieldSet.put(resourceType, Set.of(entry.getValue()[0].split(",")));
                } else {
                    fieldSet.put(resourceType, Collections.emptySet());
                }
            });

        return new FieldSet(fieldSet);
    }
}
