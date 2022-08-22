package com.slmdev.jsonapi.simple.request;

import lombok.*;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

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

    private final Map<String, FilterItem> requestParams;

    public Filter() {
        this.requestParams = new HashMap<>();
    }

    public Set<String> getAllKeys() {
        return requestParams.keySet();
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
     * Get all filter parameters.
     *
     * <p>Value always is a list of strings.
     *
     * @return list of all param values
     */
    public List<FilterItem> getAllParams() {
        return new ArrayList<>(requestParams.values());
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

    @SuppressWarnings("unchecked")
    public Optional<List<String>> listOfStringValues(final @NonNull String name) {
        if (hasParam(name)
                && requestParams.get(name).getValue() instanceof Collection) {
            return Optional.of(
                (List<String>)requestParams.get(name).getValue()
            );
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public Optional<List<Integer>> listOfIntegerValues(final @NonNull String name) {
        if (hasParam(name)
                && requestParams.get(name).getValue() instanceof Collection) {
            return Optional.of(
                ((List<String>)requestParams.get(name).getValue()).stream()
                    .map(Integer::parseInt)
                    .collect(Collectors.toList())
            );
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public Optional<List<UUID>> listOfUuidValues(final @NonNull String name) {
        if (hasParam(name)
                && requestParams.get(name).getValue() instanceof Collection) {
            return Optional.of(
                ((List<String>)requestParams.get(name).getValue()).stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toList())
            );
        }
        return Optional.empty();
    }

    public Optional<String> stringValue(final @NonNull String name) {
        final String param = getFilterValue(name);

        if (param != null) {
            return Optional.of(param);
        }
        return Optional.empty();
    }

    public Optional<Integer> intValue(final @NonNull String name) {
        final String param = getFilterValue(name);

        if (param != null) {
            return Optional.of(Integer.parseInt(param));
        }
        return Optional.empty();
    }

    public Optional<Long> longValue(final @NonNull String name) {
        final String param = getFilterValue(name);

        if (param != null) {
            return Optional.of(Long.parseLong(param));
        }
        return Optional.empty();
    }

    public Optional<Boolean> boolValue(final @NonNull String name){
        final String param = getFilterValue(name);

        if (param != null) {
            return Optional.of(Boolean.parseBoolean(param));
        }
        return Optional.empty();
    }

    public Optional<UUID> uuidValue(final @NonNull String name){
        final String param = getFilterValue(name);

        if (param != null) {
            return Optional.of(UUID.fromString(param));
        }
        return Optional.empty();
    }

    public Optional<Object> getAsObject(final @NonNull String name) {
        if (hasParam(name)) {
            return Optional.of(
                requestParams.get(name).getValue()
            );
        }
        return Optional.empty();
    }

    private String getFilterValue(final @NonNull String name) {
        final StringBuilder param = new StringBuilder();

        getAsObject(name).ifPresent(value -> {
            if (value instanceof Collection) {
                final List values = (List)value;

                if (!values.isEmpty()) {
                    param.append(values.get(0));
                }
            } else {
                param.append(value);
            }
        });
        return (param.length() > 0 ? param.toString() : null);
    }

    public static Filter.FilterItem.FilterItemBuilder in(final @NonNull List<?> values) {
        return createFilterItemBuilder(FilterItem.Operator.IN, values);
    }

    public static Filter.FilterItem.FilterItemBuilder in(final @NonNull String value) {
        return createFilterItemBuilder(FilterItem.Operator.IN, Collections.singletonList(value));
    }

    public static Filter.FilterItem.FilterItemBuilder notIn(final @NonNull List<?> values) {
        return createFilterItemBuilder(FilterItem.Operator.NOT_IN, values);
    }

    public static Filter.FilterItem.FilterItemBuilder notIn(final @NonNull String value) {
        return createFilterItemBuilder(FilterItem.Operator.NOT_IN, Collections.singletonList(value));
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
                && ((Collection<?>) value).size() > 1) {
            throw new IllegalArgumentException("Could not prepare filter! For operator: " + operator + " array values not permitted!");
        }
    }

    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    public static class FilterItem {
        private final String field;
        private final Object value;
        private final Operator operator;

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
