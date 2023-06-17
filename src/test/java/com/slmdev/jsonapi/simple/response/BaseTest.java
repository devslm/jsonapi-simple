package com.slmdev.jsonapi.simple.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.NonNull;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

abstract class BaseTest {
    protected static final String TEST_RESPONSE_URI = "/api/v1";
    protected static final UUID TEST_DTO_1_ID = UUID.randomUUID();
    protected static final String TEST_DTO_1_NAME = "TEST-1";
    protected static final LocalDateTime TEST_DTO_1_DATE_CREATE = LocalDateTime.now(ZoneOffset.UTC);
    protected static final UUID TEST_DTO_2_ID = UUID.randomUUID();
    protected static final String TEST_DTO_2_NAME = "TEST-2";
    protected static final LocalDateTime TEST_DTO_2_DATE_CREATE = LocalDateTime.now(ZoneOffset.UTC);
    protected static final String ERROR_CODE = "TEST_ERROR_CODE";
    protected static final String ERROR_DESCRIPTION = "TEST";

    protected final ObjectMapper objectMapper = JsonMapper.builder()
        .addModule(new JavaTimeModule())
        .build();

    protected TestDto buildTestDto1() {
        return new TestDto()
            .setId(TEST_DTO_1_ID)
            .setName(TEST_DTO_1_NAME)
            .setCreateDate(TEST_DTO_1_DATE_CREATE);
    }

    protected TestDto buildTestDto2() {
        return new TestDto()
            .setId(TEST_DTO_2_ID)
            .setName(TEST_DTO_2_NAME)
            .setCreateDate(TEST_DTO_2_DATE_CREATE);
    }

    protected String buildSelfLink(final @NonNull TestDto testDto) {
        return buildSelfLink("", testDto);
    }

    protected String buildSelfLink(final @NonNull String uri, final @NonNull TestDto testDto) {
        return uri + "/" + TestDto.API_TYPE + "/" + testDto.getId();
    }

    protected void assertResponseErrorCode(final @NonNull Response<?> response) {
        assertThat(response.getErrors().get(0).getCode(), is(ERROR_CODE));
    }

    protected void assertErrorResponse(final @NonNull Response<?> response) {
        assertThat(response.getData(), nullValue());

        assertThat(response.getErrors().get(0).getStatus(), is(HttpStatus.BAD_REQUEST.value()));
        assertThat(response.getErrors().get(0).getDetail(), is(ERROR_DESCRIPTION));

        assertThat(response.getMeta().getApi().getVersion(), is("1"));
        assertThat(response.getMeta().getPage().getMaxSize(), is(25));
        assertThat(response.getMeta().getPage().getTotal(), is(0L));
    }

    @SuppressWarnings("unchecked")
    protected void assertThatAttributesIdFieldIsPresentInCollection(final @NonNull Object response) {
        final Map<String, Object> responseMap = objectMapper.convertValue(response, Map.class);

        ((List<Object>)responseMap.get("data")).forEach(dataItem -> {
            final Object idAttributeField = ((Map<String, Object>)((Map<String, Object>)dataItem)
                .get("attributes"))
                .get("id");

            assertThat(idAttributeField, notNullValue());
        });
    }

    @SuppressWarnings("unchecked")
    protected void assertThatAttributesIdFieldIsPresentInObject(final @NonNull Object response) {
        final Map<String, Object> responseMap = objectMapper.convertValue(response, Map.class);

        final Object idAttributeField = ((Map<String, Object>)((Map<String, Object>)responseMap
            .get("data"))
            .get("attributes"))
            .get("id");

        assertThat(idAttributeField, notNullValue());
    }
}
