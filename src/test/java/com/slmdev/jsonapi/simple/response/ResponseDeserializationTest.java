package com.slmdev.jsonapi.simple.response;

import com.fasterxml.jackson.databind.JavaType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class ResponseDeserializationTest extends BaseTest {
    @Test
    public void shouldDeserializeResponseWithDataAs1ObjectWithoutErrorsAndUriSpecified() throws Exception {
        final TestDto testDto = buildTestDto1();
        final Response<Data<TestDto>> response = Response.<Data<TestDto>, TestDto>builder()
            .data(testDto)
            .build();

        final String json = objectMapper.writeValueAsString(response);
        final JavaType dataType = objectMapper.getTypeFactory().constructParametricType(Data.class, TestDto.class);
        final Response<Data<TestDto>> deserealizedResponse = objectMapper.readValue(json, objectMapper.getTypeFactory().constructParametricType(Response.class, dataType));

        assertThat(deserealizedResponse.getData().getAttributes().getId(), is(TEST_DTO_1_ID));
        assertThat(deserealizedResponse.getData().getAttributes().getName(), is(TEST_DTO_1_NAME));
        assertThat(deserealizedResponse.getData().getAttributes().getCreateDate(), is(TEST_DTO_1_DATE_CREATE));
        assertThat(deserealizedResponse.getData().getId(), is(TEST_DTO_1_ID.toString()));
        assertThat(deserealizedResponse.getData().getType(), Matchers.is(TestDto.API_TYPE));
        assertThat(deserealizedResponse.getData().getLinks().getSelf(), is(buildSelfLink(testDto)));
        assertThat(deserealizedResponse.getData().getLinks().getRelated(), nullValue());

        assertThat(deserealizedResponse.getErrors(), nullValue());

        assertThat(deserealizedResponse.getMeta().getApi().getVersion(), is("1"));
        assertThat(deserealizedResponse.getMeta().getPage().getMaxSize(), is(25));
        assertThat(deserealizedResponse.getMeta().getPage().getTotal(), is(1L));

        assertThatAttributesIdFieldIsPresentInObject(deserealizedResponse);
    }

    @Test
    public void shouldDeserializeResponseWithDataListWithoutErrorsAndUriSpecified() throws Exception {
        final TestDto testDto1 = buildTestDto1();
        final TestDto testDto2 = buildTestDto2();
        final Response<List<Data<TestDto>>> response = Response.<List<Data<TestDto>>, TestDto>builder()
            .data(
                Arrays.asList(testDto1, testDto2)
            ).build();

        final String json = objectMapper.writeValueAsString(response);
        final JavaType dataType = objectMapper.getTypeFactory().constructParametricType(Data.class, TestDto.class);
        final JavaType listType = objectMapper.getTypeFactory().constructParametricType(List.class, dataType);
        final Response<List<Data<TestDto>>> deserealizedResponse = objectMapper.readValue(json, objectMapper.getTypeFactory().constructParametricType(Response.class, listType));

        assertThat(deserealizedResponse.getData().get(0).getAttributes().getId(), is(TEST_DTO_1_ID));
        assertThat(deserealizedResponse.getData().get(0).getAttributes().getName(), is(TEST_DTO_1_NAME));
        assertThat(deserealizedResponse.getData().get(0).getAttributes().getCreateDate(), is(TEST_DTO_1_DATE_CREATE));
        assertThat(deserealizedResponse.getData().get(0).getId(), is(TEST_DTO_1_ID.toString()));
        assertThat(deserealizedResponse.getData().get(0).getType(), Matchers.is(TestDto.API_TYPE));
        assertThat(deserealizedResponse.getData().get(0).getLinks().getSelf(), is(buildSelfLink(testDto1)));
        assertThat(deserealizedResponse.getData().get(0).getLinks().getRelated(), nullValue());

        assertThat(deserealizedResponse.getData().get(1).getAttributes().getId(), is(TEST_DTO_2_ID));
        assertThat(deserealizedResponse.getData().get(1).getAttributes().getName(), is(TEST_DTO_2_NAME));
        assertThat(deserealizedResponse.getData().get(1).getAttributes().getCreateDate(), is(TEST_DTO_2_DATE_CREATE));
        assertThat(deserealizedResponse.getData().get(1).getId(), is(TEST_DTO_2_ID.toString()));
        assertThat(deserealizedResponse.getData().get(1).getType(), Matchers.is(TestDto.API_TYPE));
        assertThat(deserealizedResponse.getData().get(1).getLinks().getSelf(), is(buildSelfLink(testDto2)));
        assertThat(deserealizedResponse.getData().get(1).getLinks().getRelated(), nullValue());

        assertThat(deserealizedResponse.getErrors(), nullValue());

        assertThat(deserealizedResponse.getMeta().getApi().getVersion(), is("1"));
        assertThat(deserealizedResponse.getMeta().getPage().getMaxSize(), is(25));
        assertThat(deserealizedResponse.getMeta().getPage().getTotal(), is(2L));

        assertThatAttributesIdFieldIsPresentInCollection(deserealizedResponse);
    }

    @Test
    public void shouldDeserializeResponseWithErrorWithData() throws Exception {
        final Response<Data<TestDto>> response = Response.<Data<TestDto>, TestDto>builder()
            .data(buildTestDto1())
            .error(HttpStatus.BAD_REQUEST, ERROR_DESCRIPTION)
            .build();

        final String json = objectMapper.writeValueAsString(response);
        final JavaType dataType = objectMapper.getTypeFactory().constructParametricType(Data.class, TestDto.class);
        final Response<Data<TestDto>> deserealizedResponse = objectMapper.readValue(json, objectMapper.getTypeFactory().constructParametricType(Response.class, dataType));

        assertErrorResponse(deserealizedResponse);

        assertThat(deserealizedResponse.getErrors().get(0).getMeta(), nullValue());
    }

    @Test
    public void shouldDeserializeResponseWithErrorWithMetaAndWithData() throws Exception {
        final ErrorMetaDto errorMeta = buildTestErrorMeta();
        final Response<Data<TestDto>> response = Response.<Data<TestDto>, TestDto>builder()
            .data(buildTestDto1())
            .error(HttpStatus.BAD_REQUEST, ERROR_CODE, ERROR_DESCRIPTION, new Error.ErrorMeta(errorMeta))
            .build();

        final String json = objectMapper.writeValueAsString(response);
        final JavaType dataType = objectMapper.getTypeFactory().constructParametricType(Data.class, TestDto.class);
        final Response<Data<TestDto>> deserealizedResponse = objectMapper.readValue(json, objectMapper.getTypeFactory().constructParametricType(Response.class, dataType));

        assertErrorResponse(deserealizedResponse);
        assertResponseErrorMeta(deserealizedResponse, errorMeta);
    }
}
