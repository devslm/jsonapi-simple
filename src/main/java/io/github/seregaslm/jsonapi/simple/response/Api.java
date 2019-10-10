package io.github.seregaslm.jsonapi.simple.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@ToString
@AllArgsConstructor
@Accessors(chain = true)
public class Api {
    @ApiModelProperty(value = "Application specific api version", required = true)
    private String version;
}
