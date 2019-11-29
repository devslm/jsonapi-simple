package io.github.seregaslm.jsonapi.simple.request;

import lombok.*;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Class contains filter values from {@code GET} requests.
 */
@ToString
@AllArgsConstructor
@EqualsAndHashCode
public class Filter {
    private static final List<FilterItem.Operator> OPERATORS_REQUIRED_ARRAY_VALUES = Arrays.asList(
        FilterItem.Operator.IN,
        FilterItem.Operator.NOT_IN
    );

    private Map<String, FilterItem> requestParams;

    public Filter() {
        this.requestParams = new HashMap<>();
    }

    /**
     * Check if filter parameter exists.
     *
     * @param name filter param name
     * @return true if exists and false otherwise
     */
    public boolean hasParam(final @NonNull String name) {
        return requestParams.containsKey(name);
    }

    /**
     * Get filter parameter by name.
     *
     * <p>Value always is a list of strings.
     *
     * @param name filter param name
     * @return param value
     */
    public FilterItem getParam(final @NonNull String name) {
        return requestParams.get(name);
    }

    public Filter addParam(final @NonNull String key, final @NonNull Object value) {
        final FilterItem filterItem = createFilterItemBuilder(FilterItem.Operator.EQ, value).field(key).build();

        addParam(key, filterItem);

        return this;
    }

    public Filter addParam(final @NonNull String key,
                           final @NonNull Filter.FilterItem filterItem) {
        requestParams.put(key, filterItem);

        return this;
    }

    public static Filter.FilterItem.FilterItemBuilder in(final @NonNull List<?> values) {
        return createFilterItemBuilder(FilterItem.Operator.IN, values);
    }

    public static Filter.FilterItem.FilterItemBuilder in(final @NonNull String value) {
        return createFilterItemBuilder(FilterItem.Operator.IN, Arrays.asList(value));
    }

    public static Filter.FilterItem.FilterItemBuilder notIn(final @NonNull List<?> values) {
        return createFilterItemBuilder(FilterItem.Operator.NOT_IN, values);
    }

    public static Filter.FilterItem.FilterItemBuilder notIn(final @NonNull String value) {
        return createFilterItemBuilder(FilterItem.Operator.NOT_IN, Arrays.asList(value));
    }

    public static Filter.FilterItem.FilterItemBuilder eq(final @NonNull Object value) {
        return createFilterItemBuilder(FilterItem.Operator.EQ, value);
    }

    public static Filter.FilterItem.FilterItemBuilder notEq(final @NonNull Object value) {
        return createFilterItemBuilder(FilterItem.Operator.NE, value);
    }

    public static Filter.FilterItem.FilterItemBuilder gte(final @NonNull Object value) {
        return createFilterItemBuilder(FilterItem.Operator.GTE, value);
    }

    public static Filter.FilterItem.FilterItemBuilder gt(final @NonNull Object value) {
        return createFilterItemBuilder(FilterItem.Operator.GT, value);
    }

    public static Filter.FilterItem.FilterItemBuilder lte(final @NonNull Object value) {
        return createFilterItemBuilder(FilterItem.Operator.LTE, value);
    }

    public static Filter.FilterItem.FilterItemBuilder lt(final @NonNull Object value) {
        return createFilterItemBuilder(FilterItem.Operator.LT, value);
    }

    public static Filter.FilterItem.FilterItemBuilder contain(final @NonNull Object value) {
        return createFilterItemBuilder(FilterItem.Operator.CONTAIN, value);
    }

    public static Filter.FilterItem.FilterItemBuilder notContain(final @NonNull Object value) {
        return createFilterItemBuilder(FilterItem.Operator.NOT_CONTAIN, value);
    }

    private static Filter.FilterItem.FilterItemBuilder createFilterItemBuilder(final @NonNull FilterItem.Operator operator,
                                                                               final @NonNull Object value) {
        validateOperatorWithValue(operator, value);

        return Filter.FilterItem.builder()
            .operator(operator)
            .value(value);
    }

    public static void validateOperatorWithValue(final @NonNull FilterItem.Operator operator,
                                                 final @NonNull Object value) {
        if (!OPERATORS_REQUIRED_ARRAY_VALUES.contains(operator)
                && value instanceof Collection
                && ((Collection) value).size() > 1) {
            throw new IllegalArgumentException("Could not prepare filter! For operator: " + operator + " array values not permitted!");
        }
    }

    @Getter
    @Builder
    @ToString
    public static class FilterItem {
        private String field;
        private Object value;
        private Operator operator;

        public enum Operator {
            IN,
            NOT_IN,
            EQ,
            NE,
            GT,
            GTE,
            LT,
            LTE,
            CONTAIN,
            NOT_CONTAIN;

            public static Operator of(final @NonNull String operatorName) {
                if (!StringUtils.hasText(operatorName)) {
                    return null;
                }
                return Arrays.stream(values())
                    .filter(operator -> operator.name().equalsIgnoreCase(operatorName))
                    .findFirst()
                    .orElse(null);
            }
        }
    }
}
