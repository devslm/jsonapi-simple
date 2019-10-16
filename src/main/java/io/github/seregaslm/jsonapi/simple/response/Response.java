package io.github.seregaslm.jsonapi.simple.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.seregaslm.jsonapi.simple.annotation.JsonApiId;
import io.github.seregaslm.jsonapi.simple.annotation.JsonApiType;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Rest response data type in JSON API format.
 *
 * <p>Implement only root JSON API fields as data, errors and meta.
 * See specification on: <a href="https://jsonapi.org/format/#document-top-level">jsonapi: document-top-level</a>
 * All other fields such as {@code relationships} and others
 * are skipped because we don't need them.
 *
 * <p>Response can contain only one of the {@code data} or {@code errors}
 * object at the same time and meta object is present always.
 *
 * <p>Meta object contains application api version and pagination data.
 *
 * @param <T> response data type (may be object or array only)
 */
@Slf4j
@Getter
@ToString
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {
    @ApiModelProperty("Response data if no errors")
    protected T data;
    @ApiModelProperty("Response errors if exists, data field will be absent")
    protected List<Error> errors;
    @ApiModelProperty(value = "Any additional information such as api version, pagination and etc.", required = true)
    protected Meta meta;

    /**
     * Builder that construct response entity in JSON API format.
     *
     * <p>Each response entity must contain annotations: {@link JsonApiType} and
     * {@link JsonApiId}.
     * {@link JsonApiType} on the class describe the type of the entity and {@link JsonApiId}
     * describe id of this entity
     * (see: <a href="https://jsonapi.org/format/#document-resource-object-identification">document-resource-object-identification</a>).
     *
     * @param <T> response data type (may be object or array only)
     * @param <V> primary object type we work with, this object will be
     *            packed in the {@link Data} object
     */
    public static class ResponseBuilder<T, V> {
        private static final String DEFAULT_API_VERSION = "1";
        private static final int DEFAULT_MAX_PAGE_SIZE = 25;

        private Data<V> dataObject;
        private List<Data<V>> dataList;
        private String dataType;
        private List<Error> errors;
        private Meta meta;

        public ResponseBuilder() {
            this.meta = new Meta(
                new Api(DEFAULT_API_VERSION),
                new Meta.Page(DEFAULT_MAX_PAGE_SIZE, -1, null, null)
            );
        }

        /**
         * Set data for response.
         *
         * @param data data object of {@link Collection} or {@link Object} type.
         * @return self link.
         */
        @SuppressWarnings("unchecked")
        public ResponseBuilder<T, V> data(final Object data) {
            if (data == null) {
                return this;
            }
            extractDataType(data);

            if (data instanceof Collection) {
                this.dataList = collectionToJsonApiData((Collection<V>)data);
            } else {
                this.dataObject = new Data<>(dataType, getJsonApiIdFieldValue(data), (V)data);
            }
            return this;
        }

        private void extractDataType(final Object object) {
            if (StringUtils.hasText(dataType)) {
                return;
            }
            JsonApiType jsonApiTypeAnnotation = null;

            if (object instanceof Collection) {
                if (((Collection)object).size() > 0) {
                    jsonApiTypeAnnotation = ((Collection)object).stream()
                        .findFirst()
                        .get()
                        .getClass()
                        .getAnnotation(JsonApiType.class);
                } else {
                    dataType = "";

                    return;
                }
            } else {
                jsonApiTypeAnnotation = object.getClass().getAnnotation(JsonApiType.class);
            }

            if (jsonApiTypeAnnotation == null) {
                throw new RuntimeException(
                    "Could not create response! Response entity must contain the class annotation @JsonApiType! " +
                        "See: https://jsonapi.org/format/#document-resource-object-identification for more information"
                );
            }
            dataType = jsonApiTypeAnnotation.value();
        }

        private List<Data<V>> collectionToJsonApiData(final Collection<V> data) {
            final List<Data<V>> datas = new ArrayList<>();

            for (final V object : data) {
                datas.add(
                    new Data<>(dataType, getJsonApiIdFieldValue(object), object)
                );
            }
            return datas;
        }

        private String getJsonApiIdFieldValue(final Object object) {
            String value = null;
            boolean isJsonApiIdFieldPresent = false;
            Class<?> type = object.getClass();

            while (type != null) {
                for (final Field field : type.getDeclaredFields()) {
                    if (field.isAnnotationPresent(JsonApiId.class)) {
                        try {
                            field.setAccessible(true);

                            value = field.get(object).toString();
                            isJsonApiIdFieldPresent = true;

                            field.setAccessible(false);
                        } catch (Exception exception) {
                            LOGGER.error("Could not retrieve json api id field from: {}! Reason: {}",
                                object.getClass().getName(), exception.getMessage());
                        }
                        break;
                    }
                }
                type = type.getSuperclass();
            }

            if (isJsonApiIdFieldPresent
                    && value == null) {
                throw new RuntimeException(
                    "Could not create response! Response entity must contain the field with @JsonApiId annotation! " +
                        "See: https://jsonapi.org/format/#document-resource-object-identification for more information"
                );
            }
            return value;
        }

        /**
         * Add validation error with specified field name with invalid data.
         *
         * <p>Each invocation add new error object with field and error details
         * to response.
         *
         * @param errorValidationField with name with invalid data
         * @param detail detail information about validation problem
         * @return self link
         */
        public ResponseBuilder<T, V> validationError(final @NonNull String errorValidationField,
                                                     final @NonNull String detail) {
            return error(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                detail,
                errorValidationField
            );
        }

        /**
         * @see ResponseBuilder#error(HttpStatus, String, String, String)
         *
         * @param status spring {@link HttpStatus} object
         * @param detail detail information about error
         * @return self link
         */
        public ResponseBuilder<T, V> error(final HttpStatus status, final String detail) {
            return error(status, null, detail, null);
        }

        /**
         * Create errors object.
         *
         * <p>We can invoke this method as many times as we need and
         * result object will contain all errors we passed.
         *
         * @param status spring {@link HttpStatus} object
         * @param code internal SMM error code (if exists)
         * @param detail detail information about error
         * @param errorValidationField field with failed validation
         * @return self link
         */
        public ResponseBuilder<T, V> error(final HttpStatus status,
                                           final String code,
                                           final @NonNull String detail,
                                           final String errorValidationField) {
            if (this.errors == null) {
                this.errors = new ArrayList<>();
            }
            Error.Source errorSource = null;

            if (StringUtils.hasText(errorValidationField)) {
                errorSource = Error.Source.builder()
                    .parameter(errorValidationField)
                    .build();
            }
            this.errors.add(
                new Error(status.value(), code, detail, errorSource)
            );
            this.dataList = null;
            this.dataObject = null;

            return this;
        }

        /**
         * Override total size of the response collection.
         *
         * <p>If not passed the total field will be calculated automatically
         * by the data collection size (and wiil be {@code 1} when object passed.
         *
         * @param total collection size
         * @return self link
         */
        public ResponseBuilder<T, V> total(final long total) {
            return page(DEFAULT_MAX_PAGE_SIZE, total);
        }

        /**
         * Override page size and total size of the response collection.
         *
         * @param maxSize max page size
         * @param total collection size
         * @return self link
         */
        public ResponseBuilder<T, V> page(final int maxSize, final long total) {
            this.meta.getPage().setMaxSize(maxSize);
            this.meta.getPage().setTotal(total);

            return this;
        }

        /**
         * Override page size only.
         *
         * @param maxSize page size
         * @return self link
         */
        public ResponseBuilder<T, V> pageSize(final int maxSize) {
            this.meta.getPage().setMaxSize(maxSize);

            return this;
        }

        /**
         * Facebook previous page (cursor before).
         *
         * @param prev previous page cursor
         * @return self link
         */
        public ResponseBuilder<T, V> pagePrev(final String prev) {
            this.meta.getPage().setPrev(prev);

            return this;
        }

        /**
         * Facebook next page (cursor after).
         *
         * @param next next page cursor
         * @return self link
         */
        public ResponseBuilder<T, V> pageNext(final String next) {
            this.meta.getPage().setNext(next);

            return this;
        }

        /**
         * Build response.
         *
         * @return response with the passed data type
         */
        @SuppressWarnings("unchecked")
        public Response<T> build() {
            prepareMetaInfo();

            if (dataList != null) {
                return new Response(dataList, errors, meta);
            } else {
                return new Response(dataObject, errors, meta);
            }
        }

        private void prepareMetaInfo() {
            if (meta.getPage().getTotal() >= 0) {
                return;
            }

            if (dataList == null
                    && dataObject == null) {
                meta.getPage().setTotal(0);
            } else if (meta.getPage().getTotal() < 0) {
                if (dataList != null) {
                    meta.getPage().setTotal(dataList.size());
                } else {
                    meta.getPage().setTotal(1);
                }
            } else if (dataList != null) {
                meta.getPage().setTotal(dataList.size());
            } else {
                meta.getPage().setTotal(1);
            }
        }
    }

    /**
     * @see ResponseBuilder
     *
     * @param <T> response data object type
     * @param <V> data object type using for prepare data field in response
     * @return {@link ResponseBuilder} new instance
     */
    public static <T, V> ResponseBuilder<T, V> builder() {
        return new ResponseBuilder<>();
    }
}
