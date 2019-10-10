package io.github.seregaslm.jsonapi.simple.test.request;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class contains filter values from {@code GET} requests.
 */
@ToString
@AllArgsConstructor
@EqualsAndHashCode
public class Filter {
    private Map<String, List<String>> requestParams;

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
    public List<String> getParam(final @NonNull String name) {
        return requestParams.get(name);
    }
}
