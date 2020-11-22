package io.github.seregaslm.jsonapi.simple.request;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.util.*;

/**
 * Class contains sparse fields set from {@code GET} requests.
 */
@ToString
@AllArgsConstructor
@EqualsAndHashCode
public class FieldSet {
    private final Map<String, Set<String>> requestFields;

    public FieldSet() {
        this.requestFields = new HashMap<>();
    }

    /**
     * Check if fields set has required resource type.
     *
     * @param resourceType fields set resource type
     * @return true if exists and false otherwise
     */
    public boolean hasResource(final @NonNull String resourceType) {
        return requestFields.containsKey(resourceType);
    }

    /**
     * Check if fields set is empty.
     *
     * @return true if empty and false otherwise
     */
    public boolean isEmpty() {
        return requestFields.isEmpty();
    }

    /**
     * Get all parsed resource types with fields.
     *
     * @return map with resource types and fields
     */
    public Map<String, Set<String>> getAllFields() {
        return requestFields;
    }

    /**
     * Get fields by resource type.
     *
     * @param resourceType resource type
     * @return resource type fields if present and empty set otherwise
     */
    public Set<String> getFieldsByResourceType(final @NonNull String resourceType) {
        if (!hasResource(resourceType)) {
            return Collections.emptySet();
        }
        return requestFields.get(resourceType);
    }

    /**
     * Add fields to existing resource type fields.
     *
     * <p>If resource type does no exists we create them.
     *
     * @param resourceType resource type
     * @param newFields fields to be added
     */
    public void addFields(final @NonNull String resourceType, final @NonNull Set<String> newFields) {
        requestFields.computeIfAbsent(resourceType, key -> new HashSet<>());

        requestFields.get(resourceType).addAll(newFields);
    }

    /**
     * Check if resource type contains required field.
     *
     * @return true if contains and false otherwise
     */
    public boolean containsField(final @NonNull String resourceType, final @NonNull String requiredField) {
        return containsFields(resourceType, Set.of(requiredField));
    }

    /**
     * Check if resource type contains required fields set.
     *
     * @return true if contains and false otherwise
     */
    public boolean containsFields(final @NonNull String resourceType, final @NonNull Set<String> requiredFields) {
        if (!hasResource(resourceType)) {
            return false;
        }
        return requestFields.get(resourceType)
            .containsAll(requiredFields);
    }
}
