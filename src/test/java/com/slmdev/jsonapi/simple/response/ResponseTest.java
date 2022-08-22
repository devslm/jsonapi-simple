package com.slmdev.jsonapi.simple.response;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class ResponseTest {
	private static final String TEST_RESPONSE_URI = "/api/v1";
	private static final UUID TEST_DTO_1_ID = UUID.randomUUID();
	private static final String TEST_DTO_1_NAME = "TEST-1";
	private static final LocalDateTime TEST_DTO_1_DATE_CREATE = LocalDateTime.now(ZoneOffset.UTC);
	private static final UUID TEST_DTO_2_ID = UUID.randomUUID();
	private static final String TEST_DTO_2_NAME = "TEST-2";
	private static final LocalDateTime TEST_DTO_2_DATE_CREATE = LocalDateTime.now(ZoneOffset.UTC);
	private static final String ERROR_CODE = "TEST_ERROR_CODE";
	private static final String ERROR_DESCRIPTION = "TEST";

	private final ObjectMapper objectMapper = JsonMapper.builder()
		.addModule(new JavaTimeModule())
		.build();

	@Test
	public void shouldReturnResponseWithDataAs1ObjectWithoutErrorsAndUriSpecified() {
		final TestDto testDto = buildTestDto1();
		final Response<Data<TestDto>> response = Response.<Data<TestDto>, TestDto>builder()
			.data(testDto)
			.build();

		assertThat(response.getData().getAttributes().getId(), is(TEST_DTO_1_ID));
		assertThat(response.getData().getAttributes().getName(), is(TEST_DTO_1_NAME));
		assertThat(response.getData().getAttributes().getCreateDate(), is(TEST_DTO_1_DATE_CREATE));
		assertThat(response.getData().getId(), is(TEST_DTO_1_ID.toString()));
		assertThat(response.getData().getType(), Matchers.is(TestDto.API_TYPE));
		assertThat(response.getData().getLinks().getSelf(), is(buildSelfLink(testDto)));
		assertThat(response.getData().getLinks().getRelated(), nullValue());

		assertThat(response.getErrors(), nullValue());

		assertThat(response.getMeta().getApi().getVersion(), is("1"));
		assertThat(response.getMeta().getPage().getMaxSize(), is(25));
		assertThat(response.getMeta().getPage().getTotal(), is(1L));

		assertThatAttributesIdFieldIsAbsentInObject(response);
	}

	@Test
	public void shouldReturnResponseWithDataListWithoutErrorsAndUriSpecified() {
		final TestDto testDto1 = buildTestDto1();
		final TestDto testDto2 = buildTestDto2();
		final Response<List<Data<TestDto>>> response = Response.<List<Data<TestDto>>, TestDto>builder()
			.data(
				Arrays.asList(testDto1, testDto2)
			).build();

		assertThat(response.getData().get(0).getAttributes().getId(), is(TEST_DTO_1_ID));
		assertThat(response.getData().get(0).getAttributes().getName(), is(TEST_DTO_1_NAME));
		assertThat(response.getData().get(0).getAttributes().getCreateDate(), is(TEST_DTO_1_DATE_CREATE));
		assertThat(response.getData().get(0).getId(), is(TEST_DTO_1_ID.toString()));
		assertThat(response.getData().get(0).getType(), Matchers.is(TestDto.API_TYPE));
		assertThat(response.getData().get(0).getLinks().getSelf(), is(buildSelfLink(testDto1)));
		assertThat(response.getData().get(0).getLinks().getRelated(), nullValue());

		assertThat(response.getData().get(1).getAttributes().getId(), is(TEST_DTO_2_ID));
		assertThat(response.getData().get(1).getAttributes().getName(), is(TEST_DTO_2_NAME));
		assertThat(response.getData().get(1).getAttributes().getCreateDate(), is(TEST_DTO_2_DATE_CREATE));
		assertThat(response.getData().get(1).getId(), is(TEST_DTO_2_ID.toString()));
		assertThat(response.getData().get(1).getType(), Matchers.is(TestDto.API_TYPE));
		assertThat(response.getData().get(1).getLinks().getSelf(), is(buildSelfLink(testDto2)));
		assertThat(response.getData().get(1).getLinks().getRelated(), nullValue());

		assertThat(response.getErrors(), nullValue());

		assertThat(response.getMeta().getApi().getVersion(), is("1"));
		assertThat(response.getMeta().getPage().getMaxSize(), is(25));
		assertThat(response.getMeta().getPage().getTotal(), is(2L));

		assertThatAttributesIdFieldIsAbsentInCollection(response);
	}

	@Test
	public void shouldReturnResponseWithDataAndUriSpecifiedWithoutUriPlaceholdersAndErrors() throws Exception {
		final TestDto testDto = buildTestDto1();
		final Response<Data<TestDto>> response = Response.<Data<TestDto>, TestDto>builder()
			.uri(TEST_RESPONSE_URI)
			.data(testDto)
			.build();

		assertThat(response.getData().getAttributes().getId(), is(TEST_DTO_1_ID));
		assertThat(response.getData().getAttributes().getName(), is(TEST_DTO_1_NAME));
		assertThat(response.getData().getAttributes().getCreateDate(), is(TEST_DTO_1_DATE_CREATE));
		assertThat(response.getData().getId(), is(TEST_DTO_1_ID.toString()));
		assertThat(response.getData().getType(), Matchers.is(TestDto.API_TYPE));
		assertThat(response.getData().getLinks().getSelf(), is(buildSelfLink(TEST_RESPONSE_URI, testDto)));

		assertThat(response.getErrors(), nullValue());

		assertThat(response.getMeta().getApi().getVersion(), is("1"));
		assertThat(response.getMeta().getPage().getMaxSize(), is(25));
		assertThat(response.getMeta().getPage().getTotal(), is(1L));

		assertThatAttributesIdFieldIsAbsentInObject(response);
	}

	@Test
	public void shouldReturnResponseWithDataAndUriSpecifiedAndUriPlaceholdersWithoutErrors() {
		final TestDto testDto = buildTestDto1();
		final String uri = TEST_RESPONSE_URI + "/pages/{page_id}/authors/{authorId}/some/${someId}";
		final Response<Data<TestDto>> response = Response.<Data<TestDto>, TestDto>builder()
			.uri(uri, "123", "567", "uuu")
			.data(testDto)
			.build();

		assertThat(response.getData().getAttributes().getId(), is(TEST_DTO_1_ID));
		assertThat(response.getData().getAttributes().getName(), is(TEST_DTO_1_NAME));
		assertThat(response.getData().getAttributes().getCreateDate(), is(TEST_DTO_1_DATE_CREATE));
		assertThat(response.getData().getId(), is(TEST_DTO_1_ID.toString()));
		assertThat(response.getData().getType(), Matchers.is(TestDto.API_TYPE));
		assertThat(
			response.getData().getLinks().getSelf(),
			is(buildSelfLink(TEST_RESPONSE_URI + "/pages/123/authors/567/some/uuu", testDto))
		);
		assertThat(response.getErrors(), nullValue());

		assertThat(response.getMeta().getApi().getVersion(), is("1"));
		assertThat(response.getMeta().getPage().getMaxSize(), is(25));
		assertThat(response.getMeta().getPage().getTotal(), is(1L));

		assertThatAttributesIdFieldIsAbsentInObject(response);
	}


	@Test
	public void shouldThrowExceptionWhenNumberArgsInUriDifferentWithString() {
		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> {
				Response.<Data<TestDto>, TestDto>builder()
					.uri(TEST_RESPONSE_URI + "/pages/{page_id}/authors/{authorId}", "123")
					.build();
			}
		);
	}

	@Test
	public void shouldReturnResponseWithErrorWithData() {
		final Response<Data<TestDto>> response = Response.<Data<TestDto>, TestDto>builder()
			.data(buildTestDto1())
			.error(HttpStatus.BAD_REQUEST, ERROR_DESCRIPTION)
			.build();

		assertErrorResponse(response);
	}

	@Test
	public void shouldReturnResponseWithErrorWithEmptyData() {
		final Response<Data<TestDto>> response = Response.<Data<TestDto>, TestDto>builder()
			.error(HttpStatus.BAD_REQUEST, ERROR_DESCRIPTION)
			.build();

		assertErrorResponse(response);
	}

	@Test
	public void shouldReturnResponseWithErrorCodeWithEmptyData() {
		final Response<Data<TestDto>> response = Response.<Data<TestDto>, TestDto>builder()
			.error(HttpStatus.BAD_REQUEST, ERROR_CODE, ERROR_DESCRIPTION)
			.build();

		assertErrorResponse(response);
		assertResponseErrorCode(response);
	}

	@Test
	public void shouldReturnResponseWithErrorCodeWithData() {
		final Response<Data<TestDto>> response = Response.<Data<TestDto>, TestDto>builder()
			.data(buildTestDto1())
			.error(HttpStatus.BAD_REQUEST, ERROR_CODE, ERROR_DESCRIPTION)
			.build();

		assertErrorResponse(response);
		assertResponseErrorCode(response);
	}

	@Test
	public void shouldReturnResponseWithValidationErrorWithData() {
		final Response<Void> response = Response.<Void, Void>builder()
			.data(buildTestDto1())
			.validationError("someField", ERROR_DESCRIPTION)
			.build();

		assertErrorResponse(response);

		assertThat(response.getErrors().get(0).getCode(), is("VALIDATION_ERROR"));
		assertThat(response.getErrors().get(0).getSource().getParameter(), is("someField"));
	}

	@Test
	public void shouldReturnResponseWithValidationErrorWithEmptyData() {
		final Response<Void> response = Response.<Void, Void>builder()
			.validationError("someField", ERROR_DESCRIPTION)
			.build();

		assertErrorResponse(response);

		assertThat(response.getErrors().get(0).getCode(), is("VALIDATION_ERROR"));
		assertThat(response.getErrors().get(0).getSource().getParameter(), is("someField"));
	}

	@Test
	public void shouldReturnResponseWithEmptyDataAndChangedMetaVersion() {
		final Response<Void> response = Response.<Void, Void>builder()
			.apiVersion("2")
			.build();

		assertThat(response.getData(), nullValue());
		assertThat(response.getErrors(), nullValue());
		assertThat(response.getMeta().getApi().getVersion(), is("2"));
		assertThat(response.getMeta().getPage().getMaxSize(), is(25));
		assertThat(response.getMeta().getPage().getTotal(), is(0L));
	}

	@Test
	public void shouldReturnResponseWithManuallyDataTypeAndValidInvokesOrderForMapType() {
		final Response<Data<Map<String, String>>> response = Response.<Data<Map<String, String>>, Map<String, String>>builder()
			.jsonApiType("custom-data-type")
			.data(Map.of("key", "value"))
			.build();

		assertThat(response.getData().getType(), is("custom-data-type"));
		assertThat(response.getData().getAttributes().get("key"), is("value"));
	}

	@Test
	public void shouldReturnResponseWithManuallyDataTypeAndValidInvokesOrderForListType() {
		final Response<Data<Map<String, List<String>>>> response = Response.<Data<Map<String, List<String>>>, Map<String, List<String>>>builder()
			.jsonApiType("custom-data-type")
			.data(Map.of("key", List.of("value")))
			.build();

		assertThat(response.getData().getType(), is("custom-data-type"));
		assertThat(response.getData().getAttributes().get("key"), is(List.of("value")));
	}

	@Test
	public void shouldReturnResponseWithManuallyDataTypeAndIdAndValidInvokesOrderForListType() {
		final String id = UUID.randomUUID().toString();
		final Response<Data<Map<String, List<String>>>> response = Response.<Data<Map<String, List<String>>>, Map<String, List<String>>>builder()
			.jsonApiId(id)
			.jsonApiType("custom-data-type")
			.data(Map.of("key", List.of("value")))
			.build();

		assertThat(response.getData().getId(), is(id));
		assertThat(response.getData().getType(), is("custom-data-type"));
		assertThat(response.getData().getAttributes().get("key"), is(List.of("value")));
	}

	@Test
	public void shouldThrowExceptionWithManuallyDataTypeAndInValidInvokesOrder() {
		Assertions.assertThrows(
			RuntimeException.class,
			() -> {
				Response.<Data<Map<String, String>>, Map<String, String>>builder()
					.data(Map.of("key", "value"))
					.jsonApiType("custom-data-type")
					.build();
			}
		);
	}

	@Test
	@SneakyThrows
	public void shouldParseJsonResponseWithListData() {
		final String data = "{\n" +
			"    \"data\": [\n" +
			"        {\n" +
			"            \"type\": \"cars\",\n" +
			"            \"id\": \"M5\",\n" +
			"            \"attributes\": {\n" +
			"                \"id\": \"M5\",\n" +
			"                \"brand\": \"BMW\"\n" +
			"            }\n" +
			"        },{\n" +
			"            \"type\": \"cars\",\n" +
			"            \"id\": \"911\",\n" +
			"            \"attributes\": {\n" +
			"                \"id\": \"911\",\n" +
			"                \"brand\": \"Porshe\"\n" +
			"            }\n" +
			"        }\n" +
			"    ],\n" +
			"    \"meta\": {\n" +
			"        \"api\": {\n" +
			"            \"version\": \"1\"\n" +
			"        }\n" +
			"    }\n" +
			"}";
		final TypeReference<Response<List<Data<TestCarDto>>>> type = new TypeReference<>() {};
		final Response<List<Data<TestCarDto>>> response = new ObjectMapper().readValue(data, type);

		assertThat(response.getErrors(), nullValue());
		assertThat(response.getData().size(), is(2));

		assertThat(response.getData().get(0).getType(), is("cars"));
		assertThat(response.getData().get(0).getId(), is("M5"));
		assertThat(response.getData().get(0).getAttributes().getId(), nullValue());
		assertThat(response.getData().get(0).getAttributes().getBrand(), is("BMW"));

		assertThat(response.getData().get(1).getType(), is("cars"));
		assertThat(response.getData().get(1).getId(), is("911"));
		assertThat(response.getData().get(1).getAttributes().getId(), nullValue());
		assertThat(response.getData().get(1).getAttributes().getBrand(), is("Porshe"));

		assertThat(response.getMeta().getApi().getVersion(), is("1"));
	}

	@Test
	@SneakyThrows
	public void shouldParseJsonResponseWithSingleObjectData() {
		final String data = "{\n" +
			"    \"data\": \n" +
			"    {\n" +
			"        \"type\": \"cars\",\n" +
			"        \"id\": \"M5\",\n" +
			"        \"attributes\": {\n" +
			"            \"id\": \"M5\",\n" +
			"            \"brand\": \"BMW\"\n" +
			"        }\n" +
			"    },\n" +
			"    \"meta\": {\n" +
			"        \"api\": {\n" +
			"            \"version\": \"1\"\n" +
			"        }\n" +
			"    }\n" +
			"}";
		final TypeReference<Response<Data<TestCarDto>>> type = new TypeReference<>() {};
		final Response<Data<TestCarDto>> response = new ObjectMapper().readValue(data, type);

		assertThat(response.getErrors(), nullValue());

		assertThat(response.getData().getType(), is("cars"));
		assertThat(response.getData().getId(), is("M5"));
		assertThat(response.getData().getAttributes().getId(), nullValue());
		assertThat(response.getData().getAttributes().getBrand(), is("BMW"));

		assertThat(response.getMeta().getApi().getVersion(), is("1"));
	}

	@lombok.Data
	private static class TestCarDto {
		private String id;
		private String brand;
	}

	@Test
	@SneakyThrows
	public void shouldParseJsonResponseWithErrors() {
		final String data = "{\n" +
			"    \"errors\": [\n" +
			"        {\n" +
			"            \"code\": \"400\",\n" +
			"            \"source\": {\n" +
			"                \"parameter\": \"name\"\n" +
			"            }\n" +
			"        }\n" +
			"    ],\n" +
			"    \"meta\": {\n" +
			"        \"api\": {\n" +
			"            \"version\": \"1\"\n" +
			"        }\n" +
			"    }\n" +
			"}";
		final TypeReference<Response<List<Data<Map<String, Object>>>>> type = new TypeReference<>() {};
		final Response<List<Data<Map<String, Object>>>> response = new ObjectMapper().readValue(data, type);

		assertThat(response.getErrors().size(), is(1));
		assertThat(response.getErrors().get(0).getCode(), is("400"));
		assertThat(response.getErrors().get(0).getSource().getParameter(), is("name"));
	}

	private TestDto buildTestDto1() {
		return new TestDto()
			.setId(TEST_DTO_1_ID)
			.setName(TEST_DTO_1_NAME)
			.setCreateDate(TEST_DTO_1_DATE_CREATE);
	}

	private TestDto buildTestDto2() {
		return new TestDto()
			.setId(TEST_DTO_2_ID)
			.setName(TEST_DTO_2_NAME)
			.setCreateDate(TEST_DTO_2_DATE_CREATE);
	}

	private String buildSelfLink(final @NonNull TestDto testDto) {
		return buildSelfLink("", testDto);
	}

	private String buildSelfLink(final @NonNull String uri, final @NonNull TestDto testDto) {
		return uri + "/" + TestDto.API_TYPE + "/" + testDto.getId();
	}

	private void assertResponseErrorCode(final @NonNull Response<?> response) {
		assertThat(response.getErrors().get(0).getCode(), is(ERROR_CODE));
	}

	private void assertErrorResponse(final @NonNull Response<?> response) {
		assertThat(response.getData(), nullValue());

		assertThat(response.getErrors().get(0).getStatus(), is(HttpStatus.BAD_REQUEST.value()));
		assertThat(response.getErrors().get(0).getDetail(), is(ERROR_DESCRIPTION));

		assertThat(response.getMeta().getApi().getVersion(), is("1"));
		assertThat(response.getMeta().getPage().getMaxSize(), is(25));
		assertThat(response.getMeta().getPage().getTotal(), is(0L));
	}

	@SuppressWarnings("unchecked")
	private void assertThatAttributesIdFieldIsAbsentInCollection(final @NonNull Response<List<Data<TestDto>>> response) {
		final Map<String, Object> responseMap = objectMapper.convertValue(response, Map.class);

		((List<Object>)responseMap.get("data")).forEach(dataItem -> {
			final Object idAttributeField = ((Map<String, Object>)((Map<String, Object>)dataItem)
				.get("attributes"))
				.get("id");

			assertThat(idAttributeField, nullValue());
		});
	}

	@SuppressWarnings("unchecked")
	private void assertThatAttributesIdFieldIsAbsentInObject(final @NonNull Response<Data<TestDto>> response) {
		final Map<String, Object> responseMap = objectMapper.convertValue(response, Map.class);

		final Object idAttributeField = ((Map<String, Object>)((Map<String, Object>)responseMap
			.get("data"))
			.get("attributes"))
			.get("id");

		assertThat(idAttributeField, nullValue());
	}
}
