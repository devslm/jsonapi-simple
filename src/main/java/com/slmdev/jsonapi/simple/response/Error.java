package com.slmdev.jsonapi.simple.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Error {
    @ApiModelProperty(value = "Response HTTP code", required = true)
    private int status;
    @ApiModelProperty("Application specific error code")
    private String code;
    @ApiModelProperty(value = "Error detail message", required = true)
    private String detail;
    @ApiModelProperty(value = "Parameter name only for validation errors")
    private Source source;
    private ErrorLink links;

    @Getter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Source {
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
        @ApiModelProperty(value = "A link that leads to further details about this particular occurrence of the problem")
        private String about;
        @ApiModelProperty(value = "A link that identifies the type of error that this particular error is an instance of")
        private String type;
    }
}
