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
import org.springframework.data.domain.Sort;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNotNull;

public class PageTest extends BaseTest {
    private static final String REQUEST_PAGE_ARGUMENT_NAME = "page";
    private static final String REQUEST_PAGE_NUMBER_KEY = "number";
    private static final String REQUEST_PAGE_SIZE_KEY = "size";
    private static final int DEFAULT_PAGE_NUMBER = 0;
    private static final int DEFAULT_PAGE_SIZE = 25;
    private static final int TEST_PAGE_NUMBER_1 = 1;
    private static final int TEST_PAGE_NUMBER_2 = 2;
    private static final int TEST_PAGE_SIZE_10 = 10;
    private static final int TEST_PAGE_SIZE_25 = DEFAULT_PAGE_SIZE;

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
    public void shouldParsePageWithPageAndSize10Params() {
        shouldParsePageWithSizeAndPage(TEST_PAGE_NUMBER_2, TEST_PAGE_SIZE_10);
    }

    @Test
    public void shouldParsePageWithPageAndSize25Params() {
        shouldParsePageWithSizeAndPage(TEST_PAGE_NUMBER_2, TEST_PAGE_SIZE_25);
    }

    @Test
    public void shouldParsePageWithSize10OnlyParam() {
        shouldParsePageWithSizeOnly(TEST_PAGE_SIZE_10);
    }

    @Test
    public void shouldParsePageWithPageOnlyParam() {
        shouldParsePageWithNumberOnly(TEST_PAGE_NUMBER_2);
    }

    @Test
    public void shouldParsePageWithPageOnlyParamWithNumber1() {
        shouldParsePageWithNumberOnly(TEST_PAGE_NUMBER_1);
    }

    @Test
    public void shouldParsePageWithDefaultValuesWithoutPageAndSizeParams() {
        shouldParsePageWithoutPageAndSizeParams();
    }

    private void shouldParsePageWithoutPageAndSizeParams() {
        shouldParsePageWithSizeAndPage(Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    private void shouldParsePageWithSizeOnly(int size) {
        shouldParsePageWithSizeAndPage(Integer.MIN_VALUE, size);
    }

    private void shouldParsePageWithNumberOnly(int pageNumber) {
        shouldParsePageWithSizeAndPage(pageNumber, Integer.MIN_VALUE);
    }

    private void shouldParsePageWithSizeAndPage(int pageNumber, int size) {
        shouldParsePageWithSizeAndPageAndSort(pageNumber, size, Optional.empty());
    }

    private void shouldParsePageWithSizeAndPageAndSort(int pageNumber, int size, Optional<Sort> sort) {
        final Map<String, String[]> pageParams = new HashMap<>();
        final int requiredPageNumber = (pageNumber > 0 ? pageNumber - 1 : 0);

        if (pageNumber != Integer.MIN_VALUE) {
            pageParams.put(REQUEST_PAGE_ARGUMENT_NAME + "[" + REQUEST_PAGE_NUMBER_KEY + "]", new String[]{pageNumber + ""});
        }

        if (size != Integer.MIN_VALUE) {
            pageParams.put(REQUEST_PAGE_ARGUMENT_NAME + "[" + REQUEST_PAGE_SIZE_KEY + "]", new String[]{size + ""});
        }
        Mockito.when(nativeWebRequest.getParameterMap())
            .thenReturn(pageParams);

        final Pageable page = jsonApiPageArgumentResolver.resolveArgument(methodParameter, null, nativeWebRequest, null);

        if (pageNumber != Integer.MIN_VALUE) {
            assertThat(page.getPageNumber(), is(requiredPageNumber));
        } else {
            assertThat(page.getPageNumber(), is(DEFAULT_PAGE_NUMBER));
        }

        if (size != Integer.MIN_VALUE) {
            assertThat(page.getPageSize(), is(size));
        } else {
            assertThat(page.getPageSize(), is(DEFAULT_PAGE_SIZE));
        }
        sort.ifPresentOrElse(
            (value) -> assertThat(value, isNotNull()),
            () -> assertThat(page.getSort(), is(Sort.unsorted()))
        );
    }
}
