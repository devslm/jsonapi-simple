package io.github.seregaslm.jsonapi.simple.response;

import lombok.NonNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@RunWith(SpringRunner.class)
public class ResponseTests {
	private static final UUID TEST_DTO_1_ID = UUID.randomUUID();
	private static final String TEST_DTO_1_NAME = "TEST";
	private static final LocalDateTime TEST_DTO_1_DATE_CREATE = LocalDateTime.now(ZoneOffset.UTC);
	private static final String ERROR_DESCRIPTION = "TEST";

	@Test
	public void shouldReturnResponseWithDataAs1ObjectWithoutErrors() {
		final Response<Data<TestDto>> response = Response.<Data<TestDto>, TestDto>builder()
			.data(getTestDto())
			.build();

		assertThat(response.getData().getAttributes().getId(), is(TEST_DTO_1_ID));
		assertThat(response.getData().getAttributes().getName(), is(TEST_DTO_1_NAME));
		assertThat(response.getData().getAttributes().getCreateDate(), is(TEST_DTO_1_DATE_CREATE));

		assertThat(response.getData().getId(), is(TEST_DTO_1_ID.toString()));
		assertThat(response.getData().getType(), is(TestDto.API_TYPE));

		assertThat(response.getErrors(), nullValue());

		assertThat(response.getMeta().getApi().getVersion(), is("1"));
		assertThat(response.getMeta().getPage().getMaxSize(), is(25));
		assertThat(response.getMeta().getPage().getTotal(), is(1L));
	}

	@Test
	public void shouldReturnResponseWithErrorWithoutDataIfDataObjectPassed() {
		final Response<Data<TestDto>> response = Response.<Data<TestDto>, TestDto>builder()
			.data(getTestDto())
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
			.data(getTestDto())
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

	private TestDto getTestDto() {
		return new TestDto()
			.setId(TEST_DTO_1_ID)
			.setName(TEST_DTO_1_NAME)
			.setCreateDate(TEST_DTO_1_DATE_CREATE);
	}

	private void assertErrorResponse(final @NonNull Response<?> response) {
		assertThat(response.getData(), nullValue());

		assertThat(response.getErrors().get(0).getStatus(), is(HttpStatus.BAD_REQUEST.value()));
		assertThat(response.getErrors().get(0).getDetail(), is(ERROR_DESCRIPTION));

		assertThat(response.getMeta().getApi().getVersion(), is("1"));
		assertThat(response.getMeta().getPage().getMaxSize(), is(25));
		assertThat(response.getMeta().getPage().getTotal(), is(0L));
	}
}
