package com.slmdev.jsonapi.simple.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@ToString
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
    @ApiModelProperty(value = "Parameter name only for validation errors", required = false)
    private Source source;

    @Getter
    @ToString
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Source {
        private String parameter;
    }
}
