package com.slmdev.jsonapi.simple.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Data<T> {
    private final String type;
    private final String id;
    @JsonIgnoreProperties({"id"})
    private final T attributes;
    private final Link links;

    @Getter
    @ToString
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Link {
        private final String self;
        private final RelatedLink related;
    }

    @Getter
    @ToString
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RelatedLink {
        private final String href;
    }
}
