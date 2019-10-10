package io.github.seregaslm.jsonapi.simple.test.response;

import io.github.seregaslm.jsonapi.simple.test.annotation.JsonApiId;
import io.github.seregaslm.jsonapi.simple.test.annotation.JsonApiType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
@JsonApiType(TestDto.API_TYPE)
public class TestDto {
    public static final String API_TYPE = "test-object";

    @JsonApiId
    private UUID id;
    private String name;
    private LocalDateTime createDate;
}
