package com.slmdev.jsonapi.simple.response;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
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
import org.springframework.data.web.SortDefault;
import org.springframework.data.web.SortDefault.SortDefaults;
import org.springframework.web.context.request.NativeWebRequest;

import com.slmdev.jsonapi.simple.annotation.RequestJsonApiPage;
import com.slmdev.jsonapi.simple.resolver.JsonApiPageArgumentResolver;

public class PageDefaultsTest extends BaseTest {
    private static final String REQUEST_PAGE_ARGUMENT_NAME = "page";
    private static final int DEFAULT_PAGE_NUMBER = 0;
    private static final int DEFAULT_PAGE_SIZE = 10;

    private JsonApiPageArgumentResolver jsonApiPageArgumentResolver;

    @Mock
    private MethodParameter methodParameter;
    @Mock
    private RequestJsonApiPage requestJsonApiPage;
    @Mock
    private SortDefault sortDefault;
    @Mock
    private SortDefaults sortDefaults;
    @Mock
    private PageableDefault pageableDefault;
    @Mock
    private NativeWebRequest nativeWebRequest;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        Mockito.when(methodParameter.getParameterAnnotation(RequestJsonApiPage.class))
            .thenReturn(requestJsonApiPage);
        Mockito.when(requestJsonApiPage.name())
            .thenReturn(REQUEST_PAGE_ARGUMENT_NAME);

        jsonApiPageArgumentResolver = new JsonApiPageArgumentResolver();
    }

    @Test
    public void shouldParsePageWithPage2AndSize10AndNoSortParams() {

        // Mocks the PageableDefault annotation
        Mockito.when(methodParameter.getParameterAnnotation(PageableDefault.class))
            .thenReturn(pageableDefault);
        Mockito.when(pageableDefault.size())
            .thenReturn(2);
        Mockito.when(pageableDefault.page())
            .thenReturn(10);
        Mockito.when(pageableDefault.sort())
            .thenReturn(new String[0]);

        // Mocks the SortDefault and SortDefaults annotations
        Mockito.when(methodParameter.getParameterAnnotation(SortDefault.class))
            .thenReturn(null);

        // Mocks the SortDefaults annotation
        Mockito.when(methodParameter.getParameterAnnotation(SortDefaults.class))
            .thenReturn(null);

        shouldParsePageFromPageableDefault();
    }

    @Test
    public void shouldParsePageWithPage2AndSize10AndAscSortParams() {

        // Mocks the PageableDefault annotation
        Mockito.when(methodParameter.getParameterAnnotation(PageableDefault.class))
            .thenReturn(pageableDefault);
        Mockito.when(pageableDefault.size())
            .thenReturn(2);
        Mockito.when(pageableDefault.page())
            .thenReturn(10);
        Mockito.when(pageableDefault.direction())
            .thenReturn(Direction.ASC);
        Mockito.when(pageableDefault.sort())
            .thenReturn(new String[]{"foo", "bar"});

        // Mocks the SortDefault annotation
        Mockito.when(methodParameter.getParameterAnnotation(SortDefault.class))
            .thenReturn(null);

        // Mocks the SortDefaults annotation
        Mockito.when(methodParameter.getParameterAnnotation(SortDefaults.class))
            .thenReturn(null);

        shouldParsePageFromPageableDefault();
    }

    @Test
    public void shouldParsePageWithPage2AndSize10AndDescSortParams() {

        // Mocks the PageableDefault annotation
        Mockito.when(methodParameter.getParameterAnnotation(PageableDefault.class))
            .thenReturn(pageableDefault);
        Mockito.when(pageableDefault.size())
            .thenReturn(2);
        Mockito.when(pageableDefault.page())
            .thenReturn(10);
        Mockito.when(pageableDefault.direction())
            .thenReturn(Direction.DESC);
        Mockito.when(pageableDefault.sort())
            .thenReturn(new String[]{"foo", "bar"});

        // Mocks the SortDefault annotation
        Mockito.when(methodParameter.getParameterAnnotation(SortDefault.class))
            .thenReturn(null);

        // Mocks the SortDefaults annotation
        Mockito.when(methodParameter.getParameterAnnotation(SortDefaults.class))
            .thenReturn(null);

        shouldParsePageFromPageableDefault();
    }

    @Test
    public void shouldParsePageWithAscSortParams() {

        // Mocks the PageableDefault annotation
        Mockito.when(methodParameter.getParameterAnnotation(PageableDefault.class))
            .thenReturn(null);

        // Mocks the SortDefault annotation
        Mockito.when(methodParameter.getParameterAnnotation(SortDefault.class))
            .thenReturn(sortDefault);
        Mockito.when(sortDefault.sort())
            .thenReturn(new String[]{"foo", "bar"});
        Mockito.when(sortDefault.direction())
            .thenReturn(Direction.DESC);

        // Mocks the SortDefaults annotation
        Mockito.when(methodParameter.getParameterAnnotation(SortDefaults.class))
            .thenReturn(null);

        shouldParsePageFromSortDefault();
    }

    @Test
    public void shouldParsePageWithDescSortParams() {

        // Mocks the PageableDefault annotation
        Mockito.when(methodParameter.getParameterAnnotation(PageableDefault.class))
            .thenReturn(null);

        // Mocks the SortDefault annotation
        Mockito.when(methodParameter.getParameterAnnotation(SortDefault.class))
            .thenReturn(sortDefault);
        Mockito.when(sortDefault.sort())
            .thenReturn(new String[]{"foo", "bar"});
        Mockito.when(sortDefault.direction())
            .thenReturn(Direction.ASC);

        // Mocks the SortDefaults annotation
        Mockito.when(methodParameter.getParameterAnnotation(SortDefaults.class))
            .thenReturn(null);

        shouldParsePageFromSortDefault();
    }

    @Test
    public void shouldParsePageWithTwoAscAndOneDescSortParams() {

        // Mocks the PageableDefault annotation
        Mockito.when(methodParameter.getParameterAnnotation(PageableDefault.class))
            .thenReturn(null);

        // Mocks the SortDefault annotation
        Mockito.when(methodParameter.getParameterAnnotation(SortDefault.class))
            .thenReturn(null);

        // Mocks the SortDefaults annotation
        Mockito.when(methodParameter.getParameterAnnotation(SortDefaults.class))
            .thenReturn(sortDefaults);

        SortDefault sortDefaultMock1 = mock(SortDefault.class);
        Mockito.when(sortDefaultMock1.sort())
            .thenReturn(new String[]{"foo", "bar"});
        Mockito.when(sortDefaultMock1.direction())
            .thenReturn(Direction.ASC);

        SortDefault sortDefaultMock2 = mock(SortDefault.class);
        Mockito.when(sortDefaultMock2.sort())
            .thenReturn(new String[]{"baz"});
        Mockito.when(sortDefaultMock2.direction())
            .thenReturn(Direction.DESC);

        SortDefault[] sSortDefaultArrayMock = new SortDefault[] { sortDefaultMock1, sortDefaultMock2 };

        Mockito.when(sortDefaults.value())
            .thenReturn(sSortDefaultArrayMock);

        shouldParsePageFromSortDefaults();
    }

    @Test
    public void shouldParsePageWithOneAscAndTwoDescSortParams() {

        // Mocks the PageableDefault annotation
        Mockito.when(methodParameter.getParameterAnnotation(PageableDefault.class))
            .thenReturn(null);

        // Mocks the SortDefault annotation
        Mockito.when(methodParameter.getParameterAnnotation(SortDefault.class))
            .thenReturn(null);

        // Mocks the SortDefaults annotation
        Mockito.when(methodParameter.getParameterAnnotation(SortDefaults.class))
            .thenReturn(sortDefaults);

        SortDefault sortDefaultMock1 = mock(SortDefault.class);
        Mockito.when(sortDefaultMock1.sort())
            .thenReturn(new String[]{"foo"});
        Mockito.when(sortDefaultMock1.direction())
            .thenReturn(Direction.ASC);

        SortDefault sortDefaultMock2 = mock(SortDefault.class);
        Mockito.when(sortDefaultMock2.sort())
            .thenReturn(new String[]{"bar", "baz"});
        Mockito.when(sortDefaultMock2.direction())
            .thenReturn(Direction.DESC);

        SortDefault[] sSortDefaultArrayMock = new SortDefault[] { sortDefaultMock1, sortDefaultMock2 };

        Mockito.when(sortDefaults.value())
            .thenReturn(sSortDefaultArrayMock);

        shouldParsePageFromSortDefaults();
    }

    private void shouldParsePageFromPageableDefault() {

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

    private void shouldParsePageFromSortDefault() {

        // No page parameters should be passed in order for SortDefault to take effect
        final var pageParams = new HashMap<String, String[]>();

        Mockito.when(nativeWebRequest.getParameterMap())
            .thenReturn(pageParams);

        final Pageable page = jsonApiPageArgumentResolver.resolveArgument(methodParameter, null, nativeWebRequest, null);

        assertEquals(page.getPageSize(), DEFAULT_PAGE_SIZE);
        assertEquals(page.getPageNumber(), DEFAULT_PAGE_NUMBER);

        List<Order> pageOrders = page.getSort().toList();
        assertEquals(pageOrders.size(), sortDefault.sort().length);
        assertAll("all orders match", IntStream.range(0, page.getSort().toList().size()).mapToObj(i ->
            () -> {
                assertEquals(pageOrders.get(i).getDirection(), sortDefault.direction());
                assertEquals(pageOrders.get(i).getProperty(), sortDefault.sort()[i]);
            }
        ));

    }

    private void shouldParsePageFromSortDefaults() {

        // No page parameters should be passed in order for SortDefaults to take effect
        final var pageParams = new HashMap<String, String[]>();

        Mockito.when(nativeWebRequest.getParameterMap())
            .thenReturn(pageParams);

        final Pageable page = jsonApiPageArgumentResolver.resolveArgument(methodParameter, null, nativeWebRequest, null);

        assertEquals(page.getPageSize(), DEFAULT_PAGE_SIZE);
        assertEquals(page.getPageNumber(), DEFAULT_PAGE_NUMBER);

        List<Order> pageOrders = page.getSort().toList();
        SortDefault[] sortDefaultsArray = sortDefaults.value();
        String[] properties = Arrays.stream(sortDefaultsArray).map(SortDefault::sort).flatMap(Arrays::stream).toArray(String[]::new);
        Direction[] directions = Arrays.stream(sortDefaultsArray).map(sortDefault -> {
            int n = sortDefault.sort().length;
            Direction[] sortDefaultDirections = new Direction[n];
            Arrays.fill(sortDefaultDirections, sortDefault.direction());
            return sortDefaultDirections;
        }).flatMap(Arrays::stream).toArray(Direction[]::new);
        assertEquals(pageOrders.size(), properties.length);
        assertAll("all orders match", IntStream.range(0, page.getSort().toList().size()).mapToObj(i ->
            () -> {
                assertEquals(pageOrders.get(i).getDirection(), directions[i]);
                assertEquals(pageOrders.get(i).getProperty(), properties[i]);
            }
        ));

    }

}
