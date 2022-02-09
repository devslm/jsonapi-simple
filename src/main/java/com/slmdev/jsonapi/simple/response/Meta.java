package com.slmdev.jsonapi.simple.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Meta {
    private Api api;
    private Page page;
    private WebSocket webSocket;

    @Data
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Page {
        @ApiModelProperty(value = "Page size", required = true)
        private int maxSize;
        @ApiModelProperty(value = "Totatl number data objects", required = true)
        private long total;
        @ApiModelProperty("Link to the previous page if exist")
        private String prev;
        @ApiModelProperty("Link to the next page if exist")
        private String next;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WebSocket {
        private UUID sessionId;
    }
}
