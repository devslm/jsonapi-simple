package com.slmdev.jsonapi.simple.response;

import com.slmdev.jsonapi.simple.annotation.RequestJsonApiFilter;
import com.slmdev.jsonapi.simple.request.Filter;
import com.slmdev.jsonapi.simple.resolver.JsonApiFilterArgumentResolver;
import lombok.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;

public class FilterTest {
    private static final String REQUEST_FILTER_ARGUMENT_NAME = "filter";

    private static final String TEST_FILTER_KEY = "name";
    private static final String TEST_FILTER_KEY_2 = "key-2";

    private static final String TEST_FILTER_VALUE_STRING = "abc";
    private static final int TEST_FILTER_VALUE_INT = 123;
    private static final boolean TEST_FILTER_VALUE_BOOL = true;
    private static final UUID TEST_FILTER_VALUE_UUID = UUID.randomUUID();
    private static final String[] TEST_FILTER_ONE_VALUE = new String[] {TEST_FILTER_VALUE_STRING};
    private static final String[] TEST_FILTER_LIST_VALUE = new String[] {TEST_FILTER_VALUE_STRING, String.valueOf(TEST_FILTER_VALUE_INT)};
    private static final String[] TEST_FILTER_ONE_INT_VALUE = new String[] {String.valueOf(TEST_FILTER_VALUE_INT)};
    private static final String[] TEST_FILTER_BOOL_VALUE = new String[] {String.valueOf(TEST_FILTER_VALUE_BOOL)};
    private static final String[] TEST_FILTER_UUID_VALUE = new String[] {TEST_FILTER_VALUE_UUID.toString()};

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
    public void shouldReturnAllFilterItemsAndKeys() {
        final String key = TEST_FILTER_KEY;
        final Filter.FilterItem.Operator operator = Filter.FilterItem.Operator.IN;
        final String[] value = TEST_FILTER_LIST_VALUE;
        final Map<String, String[]> filterParams = new HashMap<>();
        filterParams.put(REQUEST_FILTER_ARGUMENT_NAME + "[" + key + "][" + operator.name().toLowerCase() + "]", value);

        Mockito.when(nativeWebRequest.getParameterMap())
            .thenReturn(filterParams);

        final Filter filter = (Filter)jsonApiFilterArgumentResolver.resolveArgument(methodParameter, null, nativeWebRequest, null);
        final Filter.FilterItem requiedFilterItem = Filter.FilterItem.builder()
            .field(key)
            .operator(operator)
            .value(List.of(value))
            .build();

        assertThat(filter.getParam(key).getField(), is(key));
        assertThat(filter.getParam(key).getOperator(), is(operator));
        assertThat(filter.getAllKeys(), is(Set.of(key)));
        assertThat(filter.getAllParams(), is(List.of(requiedFilterItem)));
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
    public void shouldParseFilterAndGetStringsListValue() {
        final String value1 = "str1";
        final String value2 = "str2";
        final Filter filter = shouldParseFilterWithOperator(TEST_FILTER_KEY, Filter.FilterItem.Operator.IN, new String[] {value1, value2});
        final List<String> values = filter.listOfStringValues(TEST_FILTER_KEY).get();

        assertThat(values.get(0), is(value1));
        assertThat(values.get(1), is(value2));
    }

    @Test
    public void shouldParseFilterAndGetIntsListValue() {
        final int value1 = 123;
        final int value2 = 678;
        final Filter filter = shouldParseFilterWithOperator(TEST_FILTER_KEY, Filter.FilterItem.Operator.IN, new String[] {value1 + "", value2 + ""});
        final List<Integer> values = filter.listOfIntegerValues(TEST_FILTER_KEY).get();

        assertThat(values.get(0), is(value1));
        assertThat(values.get(1), is(value2));
    }

    @Test
    public void shouldParseFilterAndGetUuidsListValue() {
        final UUID value1 = UUID.randomUUID();
        final UUID value2 = UUID.randomUUID();
        final Filter filter = shouldParseFilterWithOperator(TEST_FILTER_KEY, Filter.FilterItem.Operator.IN, new String[] {value1.toString(), value2.toString()});
        final List<UUID> values = filter.listOfUuidValues(TEST_FILTER_KEY).get();

        assertThat(values.get(0), is(value1));
        assertThat(values.get(1), is(value2));
    }

    @Test
    public void shouldParseFilterAndGetStringValue() {
        final Filter filter = shouldParseFilterWithOperator(TEST_FILTER_KEY, Filter.FilterItem.Operator.EQ, TEST_FILTER_ONE_VALUE);

        assertThat(filter.stringValue(TEST_FILTER_KEY).get(), is(TEST_FILTER_VALUE_STRING));
    }

    @Test
    public void shouldParseFilterAndGetIntValue() {
        final Filter filter = shouldParseFilterWithOperator(TEST_FILTER_KEY, Filter.FilterItem.Operator.EQ, TEST_FILTER_ONE_INT_VALUE);

        assertThat(filter.intValue(TEST_FILTER_KEY).get(), is(TEST_FILTER_VALUE_INT));
    }

    @Test
    public void shouldParseFilterAndGetLongValue() {
        final Filter filter = shouldParseFilterWithOperator(TEST_FILTER_KEY, Filter.FilterItem.Operator.EQ, TEST_FILTER_ONE_INT_VALUE);

        assertThat(filter.longValue(TEST_FILTER_KEY).get(), is((long)TEST_FILTER_VALUE_INT));
    }

    @Test
    public void shouldParseFilterAndGetBoolValue() {
        final Filter filter = shouldParseFilterWithOperator(TEST_FILTER_KEY, Filter.FilterItem.Operator.EQ, TEST_FILTER_BOOL_VALUE);

        assertThat(filter.boolValue(TEST_FILTER_KEY).get(), is(TEST_FILTER_VALUE_BOOL));
    }

    @Test
    public void shouldParseFilterAndGetUuidValue() {
        final Filter filter = shouldParseFilterWithOperator(TEST_FILTER_KEY, Filter.FilterItem.Operator.EQ, TEST_FILTER_UUID_VALUE);

        assertThat(filter.uuidValue(TEST_FILTER_KEY).get(), is(TEST_FILTER_VALUE_UUID));
    }

    @Test
    public Filter shouldParseFilterWith2Keys() {
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

        return filter;
    }

    private Filter shouldParseFilterWithOperator(final @NonNull String key,
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

        return filter;
    }
}
