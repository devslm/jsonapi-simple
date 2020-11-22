package io.github.seregaslm.jsonapi.simple.response;

import io.github.seregaslm.jsonapi.simple.annotation.RequestJsonApiFilter;
import io.github.seregaslm.jsonapi.simple.request.Filter;
import io.github.seregaslm.jsonapi.simple.resolver.JsonApiFilterArgumentResolver;
import lombok.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;

public class FilterTest {
    private static final String REQUEST_FILTER_ARGUMENT_NAME = "filter";

    private static final String TEST_FILTER_KEY = "name";
    private static final String TEST_FILTER_KEY_2 = "key-2";
    private static final String[] TEST_FILTER_ONE_VALUE = new String[] {"abc"};
    private static final String[] TEST_FILTER_LIST_VALUE = new String[] {"abc", "123"};

    private JsonApiFilterArgumentResolver jsonApiFilterArgumentResolver;

    @Mock
    private MethodParameter methodParameter;
    @Mock
    private RequestJsonApiFilter requestJsonApiFilter;
    @Mock
    private NativeWebRequest nativeWebRequest;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        Mockito.when(methodParameter.getParameterAnnotation(any()))
            .thenReturn(requestJsonApiFilter);
        Mockito.when(requestJsonApiFilter.name())
            .thenReturn(REQUEST_FILTER_ARGUMENT_NAME);

        jsonApiFilterArgumentResolver = new JsonApiFilterArgumentResolver();
    }

    @Test
    public void shouldParseFilterWithOperatorEq() {
        shouldParseFilterWithOperator(TEST_FILTER_KEY, Filter.FilterItem.Operator.EQ, TEST_FILTER_ONE_VALUE);
    }

    @Test
    public void shouldParseFilterWithOperatorContain() {
        shouldParseFilterWithOperator(TEST_FILTER_KEY, Filter.FilterItem.Operator.CONTAIN, TEST_FILTER_ONE_VALUE);
    }

    @Test
    public void shouldParseFilterWithOperatorNotContain() {
        shouldParseFilterWithOperator(TEST_FILTER_KEY, Filter.FilterItem.Operator.NOT_CONTAIN, TEST_FILTER_ONE_VALUE);
    }

    @Test
    public void shouldParseFilterWithOperatorNe() {
        shouldParseFilterWithOperator(TEST_FILTER_KEY, Filter.FilterItem.Operator.NE, TEST_FILTER_ONE_VALUE);
    }

    @Test
    public void shouldParseFilterWithOperatorGt() {
        shouldParseFilterWithOperator(TEST_FILTER_KEY, Filter.FilterItem.Operator.GT, TEST_FILTER_ONE_VALUE);
    }

    @Test
    public void shouldParseFilterWithOperatorGte() {
        shouldParseFilterWithOperator(TEST_FILTER_KEY, Filter.FilterItem.Operator.GTE, TEST_FILTER_ONE_VALUE);
    }

    @Test
    public void shouldParseFilterWithOperatorLt() {
        shouldParseFilterWithOperator(TEST_FILTER_KEY, Filter.FilterItem.Operator.LT, TEST_FILTER_ONE_VALUE);
    }

    @Test
    public void shouldParseFilterWithOperatorLte() {
        shouldParseFilterWithOperator(TEST_FILTER_KEY, Filter.FilterItem.Operator.LTE, TEST_FILTER_ONE_VALUE);
    }

    @Test
    public void shouldParseFilterWithOperatorIn() {
        shouldParseFilterWithOperator(TEST_FILTER_KEY, Filter.FilterItem.Operator.IN, TEST_FILTER_LIST_VALUE);
    }

    @Test
    public void shouldParseFilterWithOperatorNotIn() {
        shouldParseFilterWithOperator(TEST_FILTER_KEY, Filter.FilterItem.Operator.NOT_IN, TEST_FILTER_LIST_VALUE);
    }

    @Test
    public void shouldThrowExceptionWhenOperatorNotSupportArrays() {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> shouldParseFilterWithOperator(TEST_FILTER_KEY, Filter.FilterItem.Operator.EQ, TEST_FILTER_LIST_VALUE)
        );
    }

    @Test
    public void shouldParseFilterWith2Keys() {
        final Map<String, String[]> filterParams = new HashMap<>();
        filterParams.put(
            REQUEST_FILTER_ARGUMENT_NAME + "[" + TEST_FILTER_KEY + "]" +
                "[" + Filter.FilterItem.Operator.IN.name().toLowerCase() + "]",
            TEST_FILTER_LIST_VALUE
        );
        filterParams.put(
            REQUEST_FILTER_ARGUMENT_NAME + "[" + TEST_FILTER_KEY_2 + "]" +
                "[" + Filter.FilterItem.Operator.EQ.name().toLowerCase() + "]",
            TEST_FILTER_ONE_VALUE
        );

        Mockito.when(nativeWebRequest.getParameterMap())
            .thenReturn(filterParams);

        final Filter filter = (Filter)jsonApiFilterArgumentResolver.resolveArgument(methodParameter, null, nativeWebRequest, null);

        assertThat(filter.getParam(TEST_FILTER_KEY).getField(), is(TEST_FILTER_KEY));
        assertThat(filter.getParam(TEST_FILTER_KEY).getOperator(), is(Filter.FilterItem.Operator.IN));
        assertThat(filter.getParam(TEST_FILTER_KEY).getValue(), is(Arrays.asList(TEST_FILTER_LIST_VALUE)));

        assertThat(filter.getParam(TEST_FILTER_KEY_2).getField(), is(TEST_FILTER_KEY_2));
        assertThat(filter.getParam(TEST_FILTER_KEY_2).getOperator(), is(Filter.FilterItem.Operator.EQ));
        assertThat(filter.getParam(TEST_FILTER_KEY_2).getValue(), is(Arrays.asList(TEST_FILTER_ONE_VALUE)));
    }

    private void shouldParseFilterWithOperator(final @NonNull String key,
                                               final @NonNull Filter.FilterItem.Operator operator,
                                               final @NonNull String[] value) {
        final Map<String, String[]> filterParams = new HashMap<>();
        filterParams.put(REQUEST_FILTER_ARGUMENT_NAME + "[" + key + "][" + operator.name().toLowerCase() + "]", value);

        Mockito.when(nativeWebRequest.getParameterMap())
            .thenReturn(filterParams);

        final Filter filter = (Filter)jsonApiFilterArgumentResolver.resolveArgument(methodParameter, null, nativeWebRequest, null);

        assertThat(filter.getParam(key).getField(), is(key));
        assertThat(filter.getParam(key).getOperator(), is(operator));
        assertThat(filter.getParam(key).getValue(), is(Arrays.asList(value)));
    }
}
