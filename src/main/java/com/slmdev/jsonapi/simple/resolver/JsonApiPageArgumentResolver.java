package com.slmdev.jsonapi.simple.resolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableArgumentResolver;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.data.web.SortDefault.SortDefaults;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.slmdev.jsonapi.simple.annotation.RequestJsonApiPage;

/**
 * Spring resolver using for extract page values from the request.
 *
 * <p>By default using {@code page} param name and key names in square brackets,
 * for example {@code page[number]=3&page[size]=15}.
 *
 * Optionally, if provided with a {@link org.springframework.data.web.PageableDefault}, it uses those
 * values as the defaults when parsing the request parameters.
 *
 * After the parsing, a new spring {@link org.springframework.data.domain.Pageable} object will be created.
 *
 * <p>This resolver must be registered in Spring application.
 */
public class JsonApiPageArgumentResolver implements PageableArgumentResolver {
    private static final String REQUEST_PAGE_KEY_BRACKET_START = "[";
    private static final String REQUEST_PAGE_KEY_BRACKET_END = "]";

    public boolean supportsParameter(MethodParameter parameter) {
        return Pageable.class.equals(parameter.getParameterType()) &&
               parameter.getParameterAnnotation(RequestJsonApiPage.class) != null;
    }

    @NonNull
    @Override
    public Pageable resolveArgument(final MethodParameter methodParameter,
                                    final ModelAndViewContainer modelAndViewContainer,
                                    final NativeWebRequest nativeWebRequest,
                                    final WebDataBinderFactory webDataBinderFactory) {

        final SortDefault sortDefault = methodParameter.getParameterAnnotation(SortDefault.class);
        final SortDefaults sortDefaults = methodParameter.getParameterAnnotation(SortDefaults.class);
        final PageableDefault pageableDefault = methodParameter.getParameterAnnotation(PageableDefault.class);

        if (sortDefault != null && sortDefaults != null) {
            throw new IllegalArgumentException(
                String.format(
                    "Cannot use both @%s and @%s on parameter %s; Move %s into %s to define sorting order",
                    SortDefaults.class.getSimpleName(),
                    SortDefault.class.getSimpleName(),
                    methodParameter.toString(),
                    SortDefault.class.getSimpleName(),
                    SortDefaults.class.getSimpleName()
                )
            );
        }

        final RequestJsonApiPage requestJsonApiPage = methodParameter.getParameterAnnotation(RequestJsonApiPage.class);

        final String pageKeyStart = requestJsonApiPage.name() + REQUEST_PAGE_KEY_BRACKET_START;

        final Sort sort = parseSortField(nativeWebRequest, sortDefault, sortDefaults, pageableDefault);
        int page = pageableDefault == null ?  0 : pageableDefault.page();
        int size = pageableDefault == null ? 10 : pageableDefault.size();

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

    @Nullable
    private Sort parseSortField(
        final NativeWebRequest nativeWebRequest,
        @Nullable final SortDefault sortDefault,
        @Nullable final SortDefaults sortDefaults,
        @Nullable final PageableDefault pageableDefault
    ) {

        if (CollectionUtils.isEmpty(nativeWebRequest.getParameterMap())
                || nativeWebRequest.getParameterMap().get("sort") == null) {

            SortDefault[] sortDefaultArray =
                sortDefaults != null ?
                    sortDefaults.value() :
                    sortDefault != null ?
                        new SortDefault[]{sortDefault} :
                        new SortDefault[0];

            // Takes the sort from sortDefalt(s)
            if (sortDefaultArray.length > 0) {
                return Arrays.stream(sortDefaultArray).map(
                    currentSortDefault -> Sort.by(
                        currentSortDefault.direction(),
                        currentSortDefault.sort()
                    )
                ).reduce(
                    Sort.unsorted(),
                    Sort::and
                );
            // Takes the sort from pageableDefault
            } else if (pageableDefault != null && pageableDefault.sort().length > 0) {
                return Sort.by(pageableDefault.direction(), pageableDefault.sort());
            }

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
