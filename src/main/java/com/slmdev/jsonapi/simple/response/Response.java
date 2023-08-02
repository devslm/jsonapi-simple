package com.slmdev.jsonapi.simple.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.slmdev.jsonapi.simple.annotation.JsonApiId;
import com.slmdev.jsonapi.simple.annotation.JsonApiType;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

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
@NoArgsConstructor
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

        private final Meta meta;

        private Data<V> dataObject;
        private List<Data<V>> dataList;
        private String jsonApiId;
        private String jsonApiType;
        private boolean isManualDataId;
        private boolean isManualDataType;
        private List<Error> errors;
        private String uriPrefix;

        public ResponseBuilder() {
            this.isManualDataType = false;
            this.uriPrefix = "";
            this.meta = new Meta(
                new Api(DEFAULT_API_VERSION),
                new Meta.Page(DEFAULT_MAX_PAGE_SIZE, -1, null, null),
                null,
                null
            );
        }

        /**
         * See {@link ResponseBuilder#uri(String, String...)}.
         *
         * @param uriPrefix uri prefix with/without spring placeholders
         * @throws IllegalArgumentException if number placeholders and arguments are different
         * @return self link
         */
        public ResponseBuilder<T, V> uri(final @NonNull String uriPrefix) {
            uri(uriPrefix, (String)null);

            return this;
        }

        /**
         * Set uri prefix for any generated links i.e. self, related etc.
         *
         * We can use spring placeholders the same as in the {@link RequestMapping} annotations:
         * <pre>
         *     /api/v1
         *     /api/v1/users/${userId}
         *     /api/v1/users/{userId}
         *     /api/v1/users/{user_id}
         * </pre>
         * Both underscore and camel case are supported.
         *
         * @param uriPrefix uri prefix with/without spring placeholders
         * @param uriArgs spring placeholders arguments if required
         * @throws IllegalArgumentException if number placeholders and arguments are different
         * @return self link
         */
        public ResponseBuilder<T, V> uri(final @NonNull String uriPrefix, final String... uriArgs) {
            final StringBuilder buffer = new StringBuilder(uriPrefix);

            if (uriArgs != null
                    && uriArgs.length > 0) {
                replaceUriPlaceholders(buffer, uriPrefix, uriArgs);
            }
            this.uriPrefix = buffer.toString();

            return this;
        }

        private void replaceUriPlaceholders(final @NonNull StringBuilder buffer,
                                            final @NonNull String uriPrefix,
                                            final String... uriArgs) {
            buffer.setLength(0);

            boolean isStartBracketFound = false;
            int numPlaceholders = 0;
            int position = 0;

            for (char symbol : uriPrefix.toCharArray()) {
                if (symbol != '{'
                        && symbol != '$'
                        && ! isStartBracketFound) {
                    buffer.append(symbol);
                } else if (symbol == '{' || symbol == '$') {
                    isStartBracketFound = true;
                } else if (isStartBracketFound && symbol == '}') {
                    if (position < uriArgs.length) {
                        buffer.append(uriArgs[position++]);
                    }
                    isStartBracketFound = false;

                    ++numPlaceholders;
                }
            }

            if (position != numPlaceholders) {
                throw new IllegalArgumentException(
                    "Could not replace placeholders: " + uriArgs + " in uri: " + uriPrefix + " because number of args different!"
                );
            }
        }

        /**
         * Set api version.
         *
         * @param apiVersion api version for meta data
         * @return self link
         */
        public ResponseBuilder<T, V> apiVersion(final @NonNull String apiVersion) {
            this.meta.getApi().setVersion(apiVersion);

            return this;
        }

        /**
         * Set manually json api id for response.
         *
         * If we need to set data id manually (without {@link JsonApiType} annotation or without DTO object)
         * this method must be called before method {@link Response#data},
         * otherwise will be thrown exception {@link RuntimeException}!
         *
         * @param id custom json api id for the response
         * @return self link
         */
        public ResponseBuilder<T, V> jsonApiId(final String id) {
            if (StringUtils.hasText(id)) {
                this.isManualDataId = true;
                this.jsonApiId = id;
            }
            return this;
        }

        /**
         * Set manually json api data type for response.
         *
         * If we need to set data type manually (without {@link JsonApiType} annotation or without DTO object)
         * this method must be called before method {@link Response#data},
         * otherwise will be thrown exception {@link RuntimeException}!
         *
         * @param type custom json api data type for the response
         * @return self link
         */
        public ResponseBuilder<T, V> jsonApiType(final String type) {
            if (StringUtils.hasText(type)) {
                this.isManualDataType = true;
                this.jsonApiType = type;
            }
            return this;
        }

        /**
         * Set data for response.
         *
         * @param data data object of {@link Collection} or {@link Object} type
         * @return self link
         */
        @SuppressWarnings("unchecked")
        public ResponseBuilder<T, V> data(final Object data) {
            if (data == null) {
                return this;
            }
            extractDataType(data);

            if (!uriPrefix.endsWith("/" + jsonApiType)) {
                uriPrefix += "/" + jsonApiType;
            }

            if (data instanceof Collection) {
                this.dataList = toJsonApiData((Collection<V>)data);
            } else {
                this.dataObject = toJsonApiData((V)data);
            }
            return this;
        }

        private void extractDataType(final Object object) {
            if (StringUtils.hasText(jsonApiType)) {
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
                    jsonApiType = "";

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
            jsonApiType = jsonApiTypeAnnotation.value();
        }

        private List<Data<V>> toJsonApiData(final Collection<V> data) {
            final List<Data<V>> datas = new ArrayList<>();

            for (final V object : data) {
                datas.add(toJsonApiData(object));
            }
            return datas;
        }

        private Data<V> toJsonApiData(final V data) {
            final String dataId;

            if (!isManualDataType) {
                dataId = getJsonApiIdFieldValue(data);
            } else {
                if (isManualDataId) {
                    dataId = jsonApiId;
                } else {
                    dataId = UUID.randomUUID().toString();

                    LOGGER.trace("Create JSON API response random response id: {}", dataId);
                }
            }
            return new Data<>(jsonApiType, dataId, data, buildDataLink(dataId));
        }

        private Data.Link buildDataLink(final @NonNull String dataId) {
            return new Data.Link(this.uriPrefix + "/" + dataId, null);
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
                errorValidationField,
                null,
                null
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
            return error(status, null, detail, null, null, null);
        }

        /**
         * @see ResponseBuilder#error(HttpStatus, String, String, String)
         *
         * @param status spring {@link HttpStatus} object
         * @param code internal error code (if exists)
         * @param detail detail information about error
         * @return self link
         */
        public ResponseBuilder<T, V> error(final HttpStatus status, final String code, final String detail) {
            return error(status, code, detail, null, null, null);
        }

        /**
         * Create errors object.
         *
         * <p>We can invoke this method as many times as we need and
         * result object will contain all errors we passed.
         *
         * @param status spring {@link HttpStatus} object
         * @param code internal error code (if exists)
         * @param detail detail information about error
         * @param errorValidationField field with failed validation
         * @return self link
         */
        public ResponseBuilder<T, V> error(final HttpStatus status,
                                           final String code,
                                           final @NonNull String detail,
                                           final String errorValidationField) {
            return error(status, code, detail, errorValidationField, null, null);
        }

        /**
         * Create errors object.
         *
         * <p>We can invoke this method as many times as we need and
         * result object will contain all errors we passed.
         *
         * @param status spring {@link HttpStatus} object
         * @param code internal error code (if exists)
         * @param detail detail information about error
         * @param links field with links for the details about error
         * @return self link
         */
        public ResponseBuilder<T, V> error(final HttpStatus status,
                                           final String code,
                                           final @NonNull String detail,
                                           final Error.ErrorLink links) {
            return error(status, code, detail, null, links, null);
        }

        /**
         * Create errors object.
         *
         * <p>We can invoke this method as many times as we need and
         * result object will contain all errors we passed.
         *
         * @param status spring {@link HttpStatus} object
         * @param code internal error code (if exists)
         * @param detail detail information about error
         * @param errorMeta field with meta info for the non-standard details about error
         * @return self link
         */
        public ResponseBuilder<T, V> error(final HttpStatus status,
                                           final String code,
                                           final @NonNull String detail,
                                           final Error.ErrorMeta errorMeta) {
            return error(status, code, detail, null, null, errorMeta);
        }

        /**
         * Create errors object.
         *
         * <p>We can invoke this method as many times as we need and
         * result object will contain all errors we passed.
         *
         * @param status spring {@link HttpStatus} object
         * @param code internal error code (if exists)
         * @param detail detail information about error
         * @param links field with links for the details about error
         * @param errorMeta field with meta info for the non-standard details about error
         * @return self link
         */
        public ResponseBuilder<T, V> error(final HttpStatus status,
                                           final String code,
                                           final @NonNull String detail,
                                           final Error.ErrorLink links,
                                           final Error.ErrorMeta errorMeta) {
            return error(status, code, detail, null, links, errorMeta);
        }

        /**
         * Create errors object.
         *
         * <p>We can invoke this method as many times as we need and
         * result object will contain all errors we passed.
         *
         * @param status spring {@link HttpStatus} object
         * @param code internal error code (if exists)
         * @param detail detail information about error
         * @param errorValidationField field with failed validation
         * @param links field with links for the details about error
         * @param errorMeta field with meta info for the non-standard details about error
         * @return self link
         */
        public ResponseBuilder<T, V> error(final HttpStatus status,
                                           final String code,
                                           final @NonNull String detail,
                                           final String errorValidationField,
                                           final Error.ErrorLink links,
                                           final Error.ErrorMeta errorMeta) {
            if (this.errors == null) {
                this.errors = new ArrayList<>();
            }
            Error.Source errorSource = null;

            if (StringUtils.hasText(errorValidationField)) {
                errorSource = new Error.Source(errorValidationField);
            }
            final Object errorMetaData = (errorMeta != null ? errorMeta.getMeta() : null);

            this.errors.add(
                new Error(status.value(), code, detail, errorSource, links, errorMetaData)
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
         * @param metaWebSocket websocket specific meta information
         * @return self link
         */
        public ResponseBuilder<T, V> metaWebSocket(final @NonNull Meta.WebSocket metaWebSocket) {
            meta.setWebSocket(metaWebSocket);

            return this;
        }

        /**
         * @param trace trace specific meta information
         * @return self link
         */
        public ResponseBuilder<T, V> metaTrace(final @NonNull Meta.Trace trace) {
            meta.setTrace(trace);

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
