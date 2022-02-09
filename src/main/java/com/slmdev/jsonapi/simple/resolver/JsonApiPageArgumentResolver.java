package com.slmdev.jsonapi.simple.resolver;

import com.slmdev.jsonapi.simple.annotation.RequestJsonApiPage;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Spring resolver using for extract page values from the request.
 *
 * <p>By default using {@code page} param name and key names in square brackets,
 * for example {@code page[number]=3&page[size]=15}.
 *
 * After parsing will be created new spring {@link org.springframework.data.domain.Pageable} object.
 *
 * <p>This resolver must be registered in Spring application.
 */
public class JsonApiPageArgumentResolver implements HandlerMethodArgumentResolver {
    private static final String REQUEST_PAGE_KEY_BRACKET_START = "[";
    private static final String REQUEST_PAGE_KEY_BRACKET_END = "]";

    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(RequestJsonApiPage.class) != null;
    }

    public Object resolveArgument(final MethodParameter methodParameter,
                                  final ModelAndViewContainer modelAndViewContainer,
                                  final NativeWebRequest nativeWebRequest,
                                  final WebDataBinderFactory webDataBinderFactory) {
        final RequestJsonApiPage RequestJsonApiPage = methodParameter.getParameterAnnotation(RequestJsonApiPage.class);
        final String pageKeyStart = RequestJsonApiPage.name() + REQUEST_PAGE_KEY_BRACKET_START;
        int page = 1;
        int size = 25;

        final List<Map.Entry<String, String[]>> entries = nativeWebRequest.getParameterMap()
            .entrySet()
            .stream()
            .filter(entry -> entry.getKey().startsWith(pageKeyStart) && entry.getKey().contains(REQUEST_PAGE_KEY_BRACKET_END))
            .collect(Collectors.toList());

        for (final Map.Entry<String, String[]> entry : entries) {
            final String fieldName = entry.getKey().replace(pageKeyStart, "").replace(REQUEST_PAGE_KEY_BRACKET_END, "");
            final List<String> valueItems = valueToList(entry.getValue());

            if (valueItems.isEmpty()) {
                continue;
            }
            final int value = Integer.parseInt(valueItems.get(0));

            switch (fieldName) {
                case "page":
                case "number":
                    page = value;
                    break;
                case "size":
                case "limit":
                    size = value;
                    break;
                default:
                    // Skip unknown fields
            }
        }

        if (page < 1) {
            page = 1;
        }

        if (size < 1) {
            size = 25;
        }
        return PageRequest.of(page, size);
    }

    private List<String> valueToList(final String[] values) {
        final List<String> valueItems = new ArrayList<>();

        Stream.of(values)
            .map(value -> {
                if (!value.startsWith("{")
                        && !value.startsWith("[{")) {
                    return (Arrays.asList(value.split(",")));
                }
                return Collections.singletonList(value);
            }).forEach(valueItems::addAll);

        return valueItems;
    }
}
