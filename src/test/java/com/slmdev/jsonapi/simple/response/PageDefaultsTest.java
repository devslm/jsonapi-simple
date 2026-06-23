package com.slmdev.jsonapi.simple.response;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.context.request.NativeWebRequest;

import com.slmdev.jsonapi.simple.annotation.RequestJsonApiPage;
import com.slmdev.jsonapi.simple.resolver.JsonApiPageArgumentResolver;

public class PageDefaultsTest extends BaseTest {
    private static final String REQUEST_PAGE_ARGUMENT_NAME = "page";

    private JsonApiPageArgumentResolver jsonApiPageArgumentResolver;

    @Mock
    private MethodParameter methodParameter;
    @Mock
    private RequestJsonApiPage requestJsonApiPage;
    @Mock
    private PageableDefault pageableDefault;
    @Mock
    private NativeWebRequest nativeWebRequest;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        Mockito.when(methodParameter.getParameterAnnotation(RequestJsonApiPage.class))
            .thenReturn(requestJsonApiPage);
        Mockito.when(methodParameter.getParameterAnnotation(PageableDefault.class))
            .thenReturn(pageableDefault);
        Mockito.when(requestJsonApiPage.name())
            .thenReturn(REQUEST_PAGE_ARGUMENT_NAME);

        jsonApiPageArgumentResolver = new JsonApiPageArgumentResolver();
    }

    @Test
    public void shouldParsePageWithPage2AndSize10AndNoSortParams() {
        Mockito.when(pageableDefault.size())
            .thenReturn(2);
        Mockito.when(pageableDefault.page())
            .thenReturn(10);
        Mockito.when(pageableDefault.sort())
            .thenReturn(new String[0]);
        shouldParsePageWithSizeAndPageAndSort();
    }

    @Test
    public void shouldParsePageWithPage2AndSize10AndAscSortParams() {
        Mockito.when(pageableDefault.size())
            .thenReturn(2);
        Mockito.when(pageableDefault.page())
            .thenReturn(10);
        Mockito.when(pageableDefault.direction())
            .thenReturn(Direction.ASC);
        Mockito.when(pageableDefault.sort())
            .thenReturn(new String[]{"foo", "bar"});
        shouldParsePageWithSizeAndPageAndSort();
    }

    @Test
    public void shouldParsePageWithPage2AndSize10AndDescSortParams() {
        Mockito.when(pageableDefault.size())
            .thenReturn(2);
        Mockito.when(pageableDefault.page())
            .thenReturn(10);
        Mockito.when(pageableDefault.direction())
            .thenReturn(Direction.DESC);
        Mockito.when(pageableDefault.sort())
            .thenReturn(new String[]{"foo", "bar"});
        shouldParsePageWithSizeAndPageAndSort();
    }

    private void shouldParsePageWithSizeAndPageAndSort() {

        // No page parameters should be passed in order for pageableDefault to take effect
        final var pageParams = new HashMap<String, String[]>();

        Mockito.when(nativeWebRequest.getParameterMap())
            .thenReturn(pageParams);

        final Pageable page = jsonApiPageArgumentResolver.resolveArgument(methodParameter, null, nativeWebRequest, null);

        assertEquals(page.getPageSize(), pageableDefault.size());
        assertEquals(page.getPageNumber(), pageableDefault.page());

        List<Order> pageOrders = page.getSort().toList();
        assertEquals(pageOrders.size(), pageableDefault.sort().length);
        assertAll("all orders match", IntStream.range(0, page.getSort().toList().size()).mapToObj(i ->
            () -> {
                assertEquals(pageOrders.get(i).getDirection(), pageableDefault.direction());
                assertEquals(pageOrders.get(i).getProperty(), pageableDefault.sort()[i]);
            }
        ));

    }

}
