package com.slmdev.jsonapi.simple.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Error {
    @Schema(description = "Response HTTP code", requiredMode = Schema.RequiredMode.REQUIRED)
    private int status;
    @Schema(description = "Application specific error code")
    private String code;
    @Schema(description = "Error detail message", requiredMode = Schema.RequiredMode.REQUIRED)
    private String detail;
    @Schema(description = "Parameter name only for validation errors")
    private Source source;
    @Schema(description = "Links object can be used to represent links")
    private ErrorLink links;
    @Schema(description = "Meta object containing non-standard meta-information about the error")
    private Object meta;

    @Getter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Source {
        @Schema(description = "Object containing references to the primary source of the error")
        private String parameter;
    }

    @Getter
    @ToString
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorLink {
        @Schema(description = "A link that leads to further details about this particular occurrence of the problem")
        private String about;
        @Schema(description = "A link that identifies the type of error that this particular error is an instance of")
        private String type;
    }
    @Getter
    @ToString
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorMeta {
        private Object meta;
    }
}
