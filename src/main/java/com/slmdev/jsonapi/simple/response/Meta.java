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
    private Trace trace;

    @Data
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Page {
        @ApiModelProperty(value = "Page size", required = true)
        private int maxSize;
        @ApiModelProperty(value = "Total number data objects", required = true)
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
        @ApiModelProperty("Websocket session id")
        private UUID sessionId;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Trace {
        public Trace() {

        }

        public Trace(final String id) {
            this.id = id;
        }

        public Trace(final UUID id) {
            this.id = id.toString();
        }

        @ApiModelProperty("Trace id (any string identifier, ie request id)")
        private String id;
    }
}
