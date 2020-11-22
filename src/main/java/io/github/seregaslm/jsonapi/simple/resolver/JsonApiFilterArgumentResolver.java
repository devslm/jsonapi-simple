package io.github.seregaslm.jsonapi.simple.resolver;

import io.github.seregaslm.jsonapi.simple.annotation.RequestJsonApiFilter;
import io.github.seregaslm.jsonapi.simple.request.Filter;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.*;
import java.util.stream.Stream;

/**
 * Spring filter using for extract filter values from the request.
 *
 * <p>By default using {@code filter} param name and key names in square brackets,
 * for example {@code filter[id]=123&filter[name]=test}. If filter operator omitted
 * then using by default operator {@code EQ}. We can use other operators for example:
 * {@code filter[id][in]=123,345&filter[name][not_contain]=test}.
 * See {@link io.github.seregaslm.jsonapi.simple.request.Filter.FilterItem.Operator} for
 * all supported operators.
 *
 * After parsing will be create new {@link Filter} object with the map of found filters.
 * Each value is {@link Filter.FilterItem} object.
 *
 * <p>This filter must be registered in Spring application.
 */
public class JsonApiFilterArgumentResolver implements HandlerMethodArgumentResolver {
    private static final String REQUEST_FILTER_KEY_BRACKET_START = "[";
    private static final String REQUEST_FILTER_KEY_BRACKET_END = "]";

    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(RequestJsonApiFilter.class) != null;
    }

    public Object resolveArgument(final MethodParameter methodParameter,
                                  final ModelAndViewContainer modelAndViewContainer,
                                  final NativeWebRequest nativeWebRequest,
                                  final WebDataBinderFactory webDataBinderFactory) {
        final RequestJsonApiFilter requestJsonApiFilter = methodParameter.getParameterAnnotation(RequestJsonApiFilter.class);
        final Map<String, Filter.FilterItem> filterParams = new HashMap<>();
        final String filterKeyStart = requestJsonApiFilter.name() + REQUEST_FILTER_KEY_BRACKET_START;

        nativeWebRequest.getParameterMap()
            .entrySet()
            .stream()
            .filter(entry -> entry.getKey().startsWith(filterKeyStart) && entry.getKey().contains(REQUEST_FILTER_KEY_BRACKET_END))
            .forEach(entry -> {
                final String fieldName;
                final Filter.FilterItem.Operator operator;
                final List<String> valueItems = valueToList(entry.getValue());
                final String[] items = entry.getKey().split("\\" + REQUEST_FILTER_KEY_BRACKET_START);

                if (items.length == 3) {
                    fieldName = items[1].replace(REQUEST_FILTER_KEY_BRACKET_END, "");
                    operator = Filter.FilterItem.Operator.of(items[2].replace(REQUEST_FILTER_KEY_BRACKET_END, ""));
                } else {
                    fieldName = entry.getKey().replace(filterKeyStart, "").replace(REQUEST_FILTER_KEY_BRACKET_END, "");
                    operator = Filter.FilterItem.Operator.EQ;
                }
                Filter.validateOperatorWithValue(operator, valueItems);

                filterParams.put(
                    fieldName,
                    Filter.FilterItem.builder()
                        .field(fieldName)
                        .value(valueItems)
                        .operator(operator)
                        .build()
                );
            });

        return new Filter(filterParams);
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
