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
 * for example {@code filter[id]=123,345&filter[name]=test}. After parsing will be
 * create new {@link Filter} object with the map of finded filters. Each value is
 * {@link List} of stings object.
 *
 * <p>This filter must be registered in Spring application.
 */
public class JsonApiFilterArgumentResolver implements HandlerMethodArgumentResolver {
    private static final String REQUEST_FILTER_KEY_BRACKET_START = "[";
    private static final String REQUEST_FILTER_KEY_BRACKET_END = "]";

    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(RequestJsonApiFilter.class) != null;
    }

    public Object resolveArgument(final MethodParameter parameter,
                                  final ModelAndViewContainer mavContainer,
                                  final NativeWebRequest webRequest,
                                  final WebDataBinderFactory binderFactory) {
        final RequestJsonApiFilter requestJsonApiFilter = parameter.getParameterAnnotation(RequestJsonApiFilter.class);
        final Map<String, List<String>> filterParams = new HashMap<>();
        final String filterKeyStart = requestJsonApiFilter.name() + REQUEST_FILTER_KEY_BRACKET_START;

        webRequest.getParameterMap()
            .entrySet()
            .stream()
            .filter(entry -> entry.getKey().startsWith(filterKeyStart) && entry.getKey().contains(REQUEST_FILTER_KEY_BRACKET_END))
            .forEach(entry -> {
                final List<String> valueItems = new ArrayList<>();

                Stream.of(entry.getValue())
                    .map(value -> (Arrays.asList(value.split(","))))
                    .forEach(valueItems::addAll);

                filterParams.put(
                    entry.getKey().replace(filterKeyStart, "").replace(REQUEST_FILTER_KEY_BRACKET_END, ""),
                    valueItems
                );
            });
        return new Filter(filterParams);
    }
}
