package io.github.seregaslm.jsonapi.simple.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
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
	private static final String ERROR_DESCRIPTION = "TEST";

	private final ObjectMapper objectMapper = new ObjectMapper();

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
		assertThat(response.getData().getType(), is(TestDto.API_TYPE));
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
		assertThat(response.getData().get(0).getType(), is(TestDto.API_TYPE));
		assertThat(response.getData().get(0).getLinks().getSelf(), is(buildSelfLink(testDto1)));
		assertThat(response.getData().get(0).getLinks().getRelated(), nullValue());

		assertThat(response.getData().get(1).getAttributes().getId(), is(TEST_DTO_2_ID));
		assertThat(response.getData().get(1).getAttributes().getName(), is(TEST_DTO_2_NAME));
		assertThat(response.getData().get(1).getAttributes().getCreateDate(), is(TEST_DTO_2_DATE_CREATE));
		assertThat(response.getData().get(1).getId(), is(TEST_DTO_2_ID.toString()));
		assertThat(response.getData().get(1).getType(), is(TestDto.API_TYPE));
		assertThat(response.getData().get(1).getLinks().getSelf(), is(buildSelfLink(testDto2)));
		assertThat(response.getData().get(1).getLinks().getRelated(), nullValue());

		assertThat(response.getErrors(), nullValue());

		assertThat(response.getMeta().getApi().getVersion(), is("1"));
		assertThat(response.getMeta().getPage().getMaxSize(), is(25));
		assertThat(response.getMeta().getPage().getTotal(), is(2L));

		assertThatAttributesIdFieldIsAbsentInCollection(response);
	}

	@Test
	public void shouldReturnResponseWithDataAndUriSpecifiedWithoutUriPlaceholdersAndErrors() {
		final TestDto testDto = buildTestDto1();
		final Response<Data<TestDto>> response = Response.<Data<TestDto>, TestDto>builder()
			.uri(TEST_RESPONSE_URI)
			.data(testDto)
			.build();

		assertThat(response.getData().getAttributes().getId(), is(TEST_DTO_1_ID));
		assertThat(response.getData().getAttributes().getName(), is(TEST_DTO_1_NAME));
		assertThat(response.getData().getAttributes().getCreateDate(), is(TEST_DTO_1_DATE_CREATE));
		assertThat(response.getData().getId(), is(TEST_DTO_1_ID.toString()));
		assertThat(response.getData().getType(), is(TestDto.API_TYPE));
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
		assertThat(response.getData().getType(), is(TestDto.API_TYPE));
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
	public void shouldReturnResponseWithErrorWithoutDataIfDataObjectPassed() {
		final Response<Data<TestDto>> response = Response.<Data<TestDto>, TestDto>builder()
			.data(buildTestDto1())
			.error(HttpStatus.BAD_REQUEST, ERROR_DESCRIPTION)
			.build();

		assertErrorResponse(response);
	}

	@Test
	public void shouldReturnResponseWithErrorWithoutDataIfDataObjectAbsent() {
		final Response<Data<TestDto>> response = Response.<Data<TestDto>, TestDto>builder()
			.error(HttpStatus.BAD_REQUEST, ERROR_DESCRIPTION)
			.build();

		assertErrorResponse(response);
	}

	@Test
	public void shouldReturnResponseWithValidationErrorWithoutDataIfDataObjectPassed() {
		final Response<Void> response = Response.<Void, Void>builder()
			.data(buildTestDto1())
			.validationError("someField", ERROR_DESCRIPTION)
			.build();

		assertErrorResponse(response);

		assertThat(response.getErrors().get(0).getCode(), is("VALIDATION_ERROR"));
		assertThat(response.getErrors().get(0).getSource().getParameter(), is("someField"));
	}

	@Test
	public void shouldReturnResponseWithValidationErrorWhenEmptyData() {
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
