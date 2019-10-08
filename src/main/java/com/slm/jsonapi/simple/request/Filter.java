package com.slm.jsonapi.simple.request;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ToString
@AllArgsConstructor
@EqualsAndHashCode
public class Filter {
    private Map<String, List<String>> requestParams;

    public Filter() {
        this.requestParams = new HashMap<>();
    }

    public boolean hasParam(final @NonNull String name) {
        return requestParams.containsKey(name);
    }

    public List<String> getParam(final @NonNull String name) {
        return requestParams.get(name);
    }
}
