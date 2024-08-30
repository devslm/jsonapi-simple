package com.slmdev.jsonapi.simple.resolver;

import com.slmdev.jsonapi.simple.annotation.RequestJsonApiPage;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;
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

    public Pageable resolveArgument(final MethodParameter methodParameter,
                                    final ModelAndViewContainer modelAndViewContainer,
                                    final NativeWebRequest nativeWebRequest,
                                    final WebDataBinderFactory webDataBinderFactory) {
        final RequestJsonApiPage RequestJsonApiPage = methodParameter.getParameterAnnotation(RequestJsonApiPage.class);
        final String pageKeyStart = RequestJsonApiPage.name() + REQUEST_PAGE_KEY_BRACKET_START;
        final Sort sort = parseSortField(nativeWebRequest);
        int page = 0;
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
                    page = value - 1;
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
            page = 0;
        }

        if (size < 1) {
            size = 25;
        }

        if (sort != null) {
            return PageRequest.of(page, size, sort);
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

    private Sort parseSortField(final NativeWebRequest nativeWebRequest) {
        if (CollectionUtils.isEmpty(nativeWebRequest.getParameterMap())
                || nativeWebRequest.getParameterMap().get("sort") == null) {
            return null;
        }
        final var sortOrders = Arrays.stream(nativeWebRequest.getParameterMap()
            .get("sort"))
            .map(field -> {
                if (!field.startsWith("-")) {
                    return Sort.Order.asc(field);
                }
                return Sort.Order.desc(field.substring(1));
            }).collect(Collectors.toList());

        return Sort.by(sortOrders);
    }
}
