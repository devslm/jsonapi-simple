package com.slmdev.jsonapi.simple.response;

import com.slmdev.jsonapi.simple.annotation.RequestJsonApiPage;
import com.slmdev.jsonapi.simple.resolver.JsonApiPageArgumentResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;

public class PageSortTest extends BaseTest {
    private static final String REQUEST_PAGE_ARGUMENT_NAME = "page";
    private static final String REQUEST_PAGE_NUMBER_KEY = "number";
    private static final String REQUEST_PAGE_SIZE_KEY = "size";
    private static final String REQUEST_PAGE_ARGUMENT_KEY = "sort";
    private static final Optional<Integer> TEST_PAGE_NUMBER_2 = Optional.of(2);
    private static final Optional<Integer> TEST_PAGE_SIZE_10 = Optional.of(10);
    private static final String TEST_SORT_FIELD_NAME = "name";
    private static final String TEST_SORT_FIELD_AGE = "age";

    private JsonApiPageArgumentResolver jsonApiPageArgumentResolver;

    @Mock
    private MethodParameter methodParameter;
    @Mock
    private RequestJsonApiPage requestJsonApiPage;
    @Mock
    private NativeWebRequest nativeWebRequest;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        Mockito.when(methodParameter.getParameterAnnotation(any()))
            .thenReturn(requestJsonApiPage);
        Mockito.when(requestJsonApiPage.name())
            .thenReturn(REQUEST_PAGE_ARGUMENT_NAME);

        jsonApiPageArgumentResolver = new JsonApiPageArgumentResolver();
    }

    @Test
    public void shouldParsePageWithSortDescMode() {
        shouldParsePageWithSizeAndPageAndSort(
            TEST_PAGE_NUMBER_2,
            TEST_PAGE_SIZE_10,
            List.of(),
            List.of(TEST_SORT_FIELD_AGE)
        );
    }

    @Test
    public void shouldParsePageWithSortAscAndDescModes() {
        shouldParsePageWithSizeAndPageAndSort(
            TEST_PAGE_NUMBER_2,
            TEST_PAGE_SIZE_10,
            List.of(TEST_SORT_FIELD_NAME),
            List.of(TEST_SORT_FIELD_AGE)
        );
    }

    @Test
    public void shouldParsePageWithEmptySort() {
        final var page = shouldParsePageWithSizeAndPageAndSort(
            TEST_PAGE_NUMBER_2,
            TEST_PAGE_SIZE_10,
            List.of(),
            List.of()
        );
        assertThat(page.getSort().isUnsorted(), is(true));
    }

    @Test
    public void shouldParseOnlySortWithAscAndDescModes() {
        final var page = shouldParsePageWithSizeAndPageAndSort(
            Optional.empty(),
            Optional.empty(),
            List.of(TEST_SORT_FIELD_NAME),
            List.of(TEST_SORT_FIELD_AGE)
        );
    }

    @Test
    public void shouldParseOnlySortWithDescMode() {
        final var page = shouldParsePageWithSizeAndPageAndSort(
            Optional.empty(),
            Optional.empty(),
            List.of(),
            List.of(TEST_SORT_FIELD_AGE)
        );
    }

    @Test
    public void shouldParseOnlyEmptySort() {
        final var page = shouldParsePageWithSizeAndPageAndSort(
            Optional.empty(),
            Optional.empty(),
            List.of(),
            List.of()
        );
    }

    private Pageable shouldParsePageWithSizeAndPageAndSort(
        Optional<Integer> pageNumber,
        Optional<Integer> pageSize,
        List<String> ascSortFields,
        List<String> descSortFields
    ) {
        final var sortFields = Stream.concat(ascSortFields.stream(), descSortFields.stream().map(field -> "-" + field))
            .toArray(String[]::new);

        final var pageParams = new HashMap<String, String[]>();
        pageParams.put(REQUEST_PAGE_ARGUMENT_KEY, sortFields);

        pageNumber.ifPresent(number ->
            pageParams.put(REQUEST_PAGE_ARGUMENT_NAME + "[" + REQUEST_PAGE_NUMBER_KEY + "]", new String[]{String.valueOf(number)})
        );
        pageSize.ifPresent(size ->
            pageParams.put(REQUEST_PAGE_ARGUMENT_NAME + "[" + REQUEST_PAGE_SIZE_KEY + "]", new String[]{String.valueOf(size)})
        );
        Mockito.when(nativeWebRequest.getParameterMap())
            .thenReturn(pageParams);

        final var page = jsonApiPageArgumentResolver.resolveArgument(
            methodParameter,
            null,
            nativeWebRequest,
            null
        );
        pageNumber.ifPresent(number -> {
            final var requiredPageNumber = (number > 0 ? number - 1 : 0);

            assertThat(page.getPageNumber(), is(requiredPageNumber));
        });
        pageSize.ifPresent(size -> assertThat(page.getPageSize(), is(size)));

        ascSortFields.forEach(ascSortField ->
            assertThat(page.getSort().getOrderFor(ascSortField).getDirection().isAscending(), is(true))
        );
        descSortFields.forEach(descSortField ->
            assertThat(page.getSort().getOrderFor(descSortField).getDirection().isDescending(), is(true))
        );
        return page;
    }
}
