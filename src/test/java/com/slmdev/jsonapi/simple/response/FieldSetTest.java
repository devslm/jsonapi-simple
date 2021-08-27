package com.slmdev.jsonapi.simple.response;

import com.slmdev.jsonapi.simple.annotation.RequestJsonApiFieldSet;
import com.slmdev.jsonapi.simple.request.FieldSet;
import com.slmdev.jsonapi.simple.resolver.JsonApiSpreadFieldSetArgumentResolver;
import lombok.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;

public class FieldSetTest {
    private static final String REQUEST_FIELD_SET_ARGUMENT_NAME = "fields";
    private static final String TEST_FIELD_SET_RESOURCE_TYPE_1_NAME = "resource-type-1";
    private static final Set<String> TEST_FIELD_SET_FIELDS_1 = Set.of("field_1", "field_2");
    private static final String TEST_FIELD_SET_RESOURCE_TYPE_2_NAME = "resource-type-2";
    private static final Set<String> TEST_FIELD_SET_FIELDS_2 = Set.of("field_3", "field_4", "field_5", "field_6");

    private JsonApiSpreadFieldSetArgumentResolver jsonApiSpreadFieldSetArgumentResolver;

    @Mock
    private MethodParameter methodParameter;
    @Mock
    private RequestJsonApiFieldSet requestJsonApiFieldSet;
    @Mock
    private NativeWebRequest nativeWebRequest;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        Mockito.when(methodParameter.getParameterAnnotation(any()))
            .thenReturn(requestJsonApiFieldSet);
        Mockito.when(requestJsonApiFieldSet.name())
            .thenReturn(REQUEST_FIELD_SET_ARGUMENT_NAME);

        jsonApiSpreadFieldSetArgumentResolver = new JsonApiSpreadFieldSetArgumentResolver();
    }

    @Test
    public void shouldReturnEmptyFieldSetsWhenArgumentNotPresent() {
        Mockito.when(nativeWebRequest.getParameterMap())
            .thenReturn(Collections.emptyMap());

        final FieldSet fieldSet = resolveFieldSet();

        assertThat(fieldSet, is(notNullValue()));
        assertThat(fieldSet.getAllFields(), anEmptyMap());

        assertThat(fieldSet.isEmpty(), is(true));
    }

    @Test
    public void shouldParseFieldSetsWith1ResourceType() {
        final Map<String, String[]> fieldSetMap = prepareFieldSetMapWith1Resource();

        Mockito.when(nativeWebRequest.getParameterMap())
            .thenReturn(fieldSetMap);

        final FieldSet fieldSet = resolveFieldSet();

        assertFieldsSet(fieldSet, TEST_FIELD_SET_RESOURCE_TYPE_1_NAME, TEST_FIELD_SET_FIELDS_1);
    }

    @Test
    public void shouldParseFieldSetsWith2ResourceTypes() {
        final Map<String, String[]> fieldSetMap = prepareFieldSetMapWith2Resources();

        Mockito.when(nativeWebRequest.getParameterMap())
            .thenReturn(fieldSetMap);

        final FieldSet fieldSet = resolveFieldSet();

        assertFieldsSet(fieldSet, TEST_FIELD_SET_RESOURCE_TYPE_1_NAME, TEST_FIELD_SET_FIELDS_1);
        assertFieldsSet(fieldSet, TEST_FIELD_SET_RESOURCE_TYPE_2_NAME, TEST_FIELD_SET_FIELDS_2);
    }

    @Test
    public void shouldReturnTrueWhenFieldsSetContainsResourceTypeAndRequiredFields() {
        final Map<String, String[]> fieldSetMap = prepareFieldSetMapWith2Resources();

        Mockito.when(nativeWebRequest.getParameterMap())
            .thenReturn(fieldSetMap);

        final FieldSet fieldSet = resolveFieldSet();

        assertThat(fieldSet, is(notNullValue()));
        assertThat(fieldSet.containsField(TEST_FIELD_SET_RESOURCE_TYPE_1_NAME, "field_3"), is(false));
        assertThat(fieldSet.containsFields(TEST_FIELD_SET_RESOURCE_TYPE_1_NAME, Set.of("field_3", "field_5")), is(false));
        assertThat(fieldSet.containsField(TEST_FIELD_SET_RESOURCE_TYPE_2_NAME, "field_3"), is(true));
        assertThat(fieldSet.containsFields(TEST_FIELD_SET_RESOURCE_TYPE_2_NAME, Set.of("field_3", "field_5")), is(true));
    }

    @Test
    public void shouldReturnValidAllFieldsWith2ResourceTypes() {
        final Map<String, String[]> fieldSetMap = prepareFieldSetMapWith2Resources();

        Mockito.when(nativeWebRequest.getParameterMap())
            .thenReturn(fieldSetMap);

        final FieldSet fieldSet = resolveFieldSet();

        assertThat(fieldSet, is(notNullValue()));
        assertThat(
            fieldSet.getAllFields().keySet(),
            containsInAnyOrder(TEST_FIELD_SET_RESOURCE_TYPE_1_NAME, TEST_FIELD_SET_RESOURCE_TYPE_2_NAME)
        );
        fieldSet.getAllFields().forEach((key, value) -> assertThat(
            fieldSet.getFieldsByResourceType(key),
            containsInAnyOrder(value.toArray())
        ));
    }

    @Test
    public void shouldReturnFalseWhenFieldsSetNotContainsRequiredResourceType() {
        final Map<String, String[]> fieldSetMap = prepareFieldSetMapWith1Resource();

        Mockito.when(nativeWebRequest.getParameterMap())
            .thenReturn(fieldSetMap);

        final FieldSet fieldSet = resolveFieldSet();

        assertThat(fieldSet, is(notNullValue()));
        assertThat(fieldSet.containsField(TEST_FIELD_SET_RESOURCE_TYPE_2_NAME, "field_1"), is(false));
    }

    @Test
    public void shouldAddNewFieldsToExistingFieldSet() {
        final Map<String, String[]> fieldSetMap = prepareFieldSetMapWith1Resource();

        Mockito.when(nativeWebRequest.getParameterMap())
            .thenReturn(fieldSetMap);

        final FieldSet fieldSet = resolveFieldSet();
        fieldSet.addFields(TEST_FIELD_SET_RESOURCE_TYPE_2_NAME, TEST_FIELD_SET_FIELDS_2);

        assertFieldsSet(fieldSet, TEST_FIELD_SET_RESOURCE_TYPE_1_NAME, TEST_FIELD_SET_FIELDS_1);
        assertFieldsSet(fieldSet, TEST_FIELD_SET_RESOURCE_TYPE_2_NAME, TEST_FIELD_SET_FIELDS_2);
    }

    @Test
    public void shouldThrowExceptionWhenArgumentInvalidFormat() {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> {
                final Map<String, String[]> fieldSetMap = Map.of(REQUEST_FIELD_SET_ARGUMENT_NAME + "[]", new String[0]);

                Mockito.when(nativeWebRequest.getParameterMap())
                    .thenReturn(fieldSetMap);

                resolveFieldSet();
            }
        );
    }

    private Map<String, String[]> prepareFieldSetMapWith1Resource() {
        return Map.of(
            REQUEST_FIELD_SET_ARGUMENT_NAME + "[" + TEST_FIELD_SET_RESOURCE_TYPE_1_NAME + "]",
            new String[] {String.join(",", TEST_FIELD_SET_FIELDS_1)}
        );
    }

    private Map<String, String[]> prepareFieldSetMapWith2Resources() {
        return Map.of(
            REQUEST_FIELD_SET_ARGUMENT_NAME + "[" + TEST_FIELD_SET_RESOURCE_TYPE_1_NAME + "]",
            new String[] {String.join(",", TEST_FIELD_SET_FIELDS_1)},
            REQUEST_FIELD_SET_ARGUMENT_NAME + "[" + TEST_FIELD_SET_RESOURCE_TYPE_2_NAME + "]",
            new String[] {String.join(",", TEST_FIELD_SET_FIELDS_2)}
        );
    }

    private FieldSet resolveFieldSet() {
        return (FieldSet)jsonApiSpreadFieldSetArgumentResolver.resolveArgument(
            methodParameter,
            null,
            nativeWebRequest,
            null
        );
    }

    private void assertFieldsSet(final FieldSet fieldSet,
                                 final @NonNull String resourceType,
                                 final @NonNull Set<String> requiredFields) {
        assertThat(fieldSet, is(notNullValue()));
        assertThat(fieldSet.hasResource(resourceType), is(true));
        assertThat(
            fieldSet.getFieldsByResourceType(resourceType),
            containsInAnyOrder(requiredFields.toArray())
        );
    }
}
