package com.slmdev.jsonapi.simple.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Page {
        @Schema(description = "Page size", requiredMode = Schema.RequiredMode.REQUIRED)
        private int maxSize;
        @Schema(description = "Total number data objects", requiredMode = Schema.RequiredMode.REQUIRED)
        private long total;
        @Schema(description = "Link to the previous page if exist")
        private String prev;
        @Schema(description = "Link to the next page if exist")
        private String next;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WebSocket {
        @Schema(description = "Websocket session id")
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

        @Schema(description = "Trace id (any string identifier, ie request id)")
        private String id;
    }
}
