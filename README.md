## Overview
Simple implementation of the [JSON:API](https://jsonapi.org) specification (only required output fields).
This library implements only top-level fields: **data**, **links**, **errors** and **meta** without any **relationships**, **includes**
and others. 

Often we only need standard of output all our endpoints especially when using many types of communications like HTTP query,
websockets, queues etc and don't need complex entities inner relationships in our API but it's good if implementing some
exists standards so this library for this goals.

## Docs
[See project page](https://slm-dev.com/jsonapi-simple/)

## Usage
Add dependency to your project:
```xml
<dependency>
    <groupId>com.slm-dev</groupId>
    <artifactId>jsonapi-simple</artifactId>
    <version>1.10.0</version>
</dependency>
```

> **Warning:**
> The id field is included in the attributes because it more convenient for API clients.

### Build Response

See documentation part: [response structure](https://jsonapi.org/format/#document-structure)

Each response DTO should contain the annotation ```@JsonApiType("resource-type")``` with any resource type identifier and
the annotation ```@JsonApiId``` without arguments on field which will be unique identifier this item, usually this 
field is ```id```.

If we want to use data types like lists, maps etc. in the response we can set manually data type to avoid exception 
(see example below). 

Each response may contain generics (if you planning to use SWAGGER) or may not (without SWAGGER), for example both 
variants correct:
```java
public class RestController {
    // The simplest option without SWAGGER support
    public Response responseWithoutGenerics() {
        return Response.builder()
           .build();
    }
  
    // This option will display correctly in SWAGGER with all DTO fields 
    public Response<SomeDto> responseWithGenerics() {
        return Response.<SomeDto, SomeDto>builder()
           .build();
    }
}
```

Parametrized response may be 2 types:
  - as list - first parameter in response builder must be only ```<List<Data<YourDto>>>```:
    ```java
    public class RestController {
        public Response<List<Data<SomeDto>>> responseAsList() {
            return Response.<List<Data<SomeDto>>, SomeDto>builder()
               .build();
        }
    }
    ```
  - as object:
    ```java
    public class RestController {
        public Response<SomeDto> responseAsObject() {
            return Response.<SomeDto, SomeDto>builder()
               .build();
        }
    }
    ```
  - as empty response with ```Void``` class as parameter:
    ```java
    public class RestController {
        public Response<Void> responseAsObject() {
            return Response.<Void, Void>builder()
               .build();
        }
    }
    ```

Data types other than the DTO class (lists, maps etc.) can't have required annotations, so we can set data type manually:
```java
@RestController
@RequestMapping(value = "/api/v1/app", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class RestController {
    @GetMapping
    public Response<SomeDto> responseAsObject() {
        return Response.<SomeDto, SomeDto>builder()
            .jsonApiId("some-id")
            .jsonApiType("some-data-type")
            .data(Map.of("name", "Test string"))
            .build();
    }
}
```
This produces response like:
```json
{
  "data": {
    "type":"some-data-type",
    "id":"<random-uuid-id> or <fixed-id>",
    "attributes": {
      "id":"<random-uuid-id> or <fixed-id>",
      "name": "Test string"
    },
    "links": {
      "self": "/some-data-type/<random-uuid-id>"
    }
  },
  "meta": {
    "api": {
      "version":"1"
    },
    "page": {
      "maxSize":25,
      "total":1
    }
  }
}
```
#### Method **jsonApiType** must be called before method **data**! With invalid calls order exception will be thrown!
#### When set manually data type random response id (UUID) will be generated! You can set manually by method **jsonApiId**

You can add **URI** prefix for self links, by default using prefix only from **@JsonApiType** annotation
for example:
```java
@RestController
@RequestMapping(value = "/api/v1/app", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class RestController {
    @GetMapping
    public Response<SomeDto> responseAsObject() {
        return Response.<SomeDto, SomeDto>builder()
            .uri("/api/v1/app")
            .build();
    }
}
```
This produces self links like (data fields are omitted):
```json
{
  "data": { 
    "links": {
      "self": "/api/v1/app/test-object/7a543e90-2961-480e-b1c4-51249bf0c566"
    }
  },
  "meta": {}
}
```
We can also use spring placeholders in the uri the same with the **@RequestMapping** value. In this case
we must add all placeholder values in the same order as in the uri. Supported placeholders format:
  - {some_id}
  - {someId}
  - ${some_id}
  - ${someId}

```java
@RestController
@RequestMapping(value = "/api/v1/books/{book_id}/users/${userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class RestController {
    @GetMapping
    public Response<SomeDto> responseAsObject() {
        final int bookId = 1024;
        final int userId = 2048;
 
        return Response.<SomeDto, SomeDto>builder()
            .uri("/api/v1/books/{book_id}/users/${userId}", bookId, userId)
            .build();
    }
}
```
This produces self links like (data fields are omitted):
```json
{
  "data": { 
    "links": {
      "self": "/api/v1/books/1024/users/2048/test-object/7a543e90-2961-480e-b1c4-51249bf0c566"
    }
  },
  "meta": {}
}
```

### Filtering

See documentation part: [fetching-filtering](https://jsonapi.org/format/#fetching-filtering)

If you want to use request filters with annotation ```@RequestJsonApiFilter``` add argument resolver in your configuration 
for example:
```java
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig implements WebMvcConfigurer {
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new JsonApiFilterArgumentResolver());
    }
}
```

Then you can use annotation ```@RequestJsonApiFilter``` in controllers with one of the filtering operators:
  - in
  - not_in
  - eq
  - ne
  - gt
  - gte
  - lt
  - lte
  - contain
  - not_contain

**If filter operator omitted ```eq``` will be used by default!** 

You can get typed params from filter using next methods:
  - listOfStringValues(param)
  - listOfIntegerValues(param)
  - listOfUuidValues(param)
  - stringValue(param)
  - intValue(param)
  - longValue(param)
  - boolValue(param)
  - uuidValue(param)

For example:
```java
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(value = "/app", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class RestController {
    @GetMapping
    public Response<Void> get(final @RequestJsonApiFilter Filter filter) throws Exception {
        if (filter.hasParam("key")) {
            final List<String> filterValues = filter.listOfStringValues("key");
            final UUID filterUuidValue = filter.uuidValue("key");

            // do something with filter param
        }
        return Response.<Void, Void>builder()
           .build();
    }
}
```
Now if request will be contained fields like ```filter[key1][in]=value1,value2&filter[key2]=value1``` we can get them in 
the ```Filter``` object.

### Sparse fieldsets

See documentation part: [fetching-sparse-fieldsets](https://jsonapi.org/format/#fetching-sparse-fieldsets)

If you want to use request sparse fieldsets with annotation ```@RequestJsonApiFieldSet``` add argument resolver in your 
configuration for example:
```java
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig implements WebMvcConfigurer {
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new JsonApiSpreadFieldSetArgumentResolver());
    }
}
```

Then you can use annotation ```@RequestJsonApiFieldSet``` in controllers.

For example:
```java
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(value = "/app", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class RestController {
    @GetMapping
    public Response<Void> get(final @RequestJsonApiFieldSet FieldSet fieldSet) throws Exception {
        if (fieldSet.hasResource("resource-type")) {
            final Set<String> resourceFields = fieldSet.getFieldsByResourceType("resource-type");

            // do something with resource fields
        }

        if (fieldSet.isEmpty()) {
            // do something with empty resource fields
            // there we can add for example any default fields
            fieldSet.addFields("resource-type", Set.of("default_field"));
        }

        if (!fieldSet.containsFields(Set.of("required_field_1", "required_field_2"))) {
            // do something when required fields in resource fields
            // are absent
        }
        return Response.<Void, Void>builder()
           .build();
    }
}
```
Now if request will be contained fields like ```fields[resource-type-1]=field_1,field_2&fields[resource-type-2]=field_3```
we can get them in the ```FieldSet``` object.

### Pagination

See documentation part: [fetching-pagination](https://jsonapi.org/format/#fetching-pagination)

If you want to use request page with annotation ```@RequestJsonApiPage``` add argument resolver in your configuration 
for example:
```java
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig implements WebMvcConfigurer {
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new JsonApiPageArgumentResolver());
    }
}
```

Then you can use annotation ```@RequestJsonApiPage``` in controllers and get standard spring Pageable object.

For example:
```java
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(value = "/app", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class RestController {
    @GetMapping
    public Response<Void> get(final @RequestJsonApiPage Pageable page) throws Exception {
        return Response.<Void, Void>builder()
            .build();
    }
}
```

Now if request will be contained fields like ```page[number]=3&page[size]=10``` we can get them in the ```Pageable``` object.

Supported fields for the page number are:
  - **page[number]**
  - **page[page]**
  
And for the page size are:
  - **page[size]**
  - **page[limit]**

#### Request page always starts from 0 for compatible with spring repositories and etc.!
#### If request page number < 1 resolver always return number = 0 and if size < 1 it always returns default value = 25!

### Other response examples
Example response with one data object:
```java
// Pseudo code
@JsonApiType("test-object")
public static class TestDto {
    @JsonApiId
    private UUID id;
    private String name;
    private LocalDateTime createDate;
}

public class Test {
    public Response<TestDto> main() {
        return Response.<TestDto, TestDto>builder()
            .data(
                TestDto.builder()
                    .id(UUID.randomUUID())
                    .name("Test string")
                    .createDate(LocalDateTime.now(ZoneOffset.UTC))
                    .build()
            ).build();
    }
}
```
```json
{
  "data": {
    "type":"test-object",
    "id":"7a543e90-2961-480e-b1c4-51249bf0c566",
    "attributes": {
      "id":"7a543e90-2961-480e-b1c4-51249bf0c566",
      "name":"Test string",
      "createDate":"2019-10-08T18:46:53.40297"
    }, 
    "links": {
      "self": "/test-object/7a543e90-2961-480e-b1c4-51249bf0c566"
    }
  },
  "meta": {
    "api": {
      "version":"1"
    },
    "page": {
      "maxSize":25,
      "total":1
    }
  }
}
```

Example response with list of data objects:
```java
// Pseudo code
@JsonApiType("test-object")
public static class TestDto {
    @JsonApiId
    private UUID id;
    private String name;
    private LocalDateTime createDate;
}

public class Test {
    public Response<List<Data<TestDto>>> main() {
        return Response.<List<Data<TestDto>>, TestDto>builder()
            .data(
                Arrays.asList(
                    TestDto.builder()
                        .id(UUID.randomUUID())
                        .name("Test string 1")
                        .createDate(LocalDateTime.now(ZoneOffset.UTC))
                        .build(),
                    TestDto.builder()
                        .id(UUID.randomUUID())
                        .name("Test string 2")
                        .createDate(LocalDateTime.now(ZoneOffset.UTC))
                        .build()
                )
            ).build();
    }
}
```
```json
{
  "data": [ 
    {
      "type":"test-object",
      "id":"7a543e90-2961-480e-b1c4-51249bf0c566",
      "attributes": {
        "id":"7a543e90-2961-480e-b1c4-51249bf0c566",
        "name":"Test string 1",
        "createDate":"2019-10-08T18:46:53"
      }, 
      "links": {
        "self": "/test-object/7a543e90-2961-480e-b1c4-51249bf0c566"
      }
    },
    {
      "type":"test-object", 
      "id":"b4070518-e9fc-11e9-81b4-2a2ae2dbcce4",
      "attributes": {
        "id":"b4070518-e9fc-11e9-81b4-2a2ae2dbcce4",
        "name":"Test string 2",
        "createDate":"2019-10-08T18:46:51"
      }, 
      "links": {
        "self": "/test-object/b4070518-e9fc-11e9-81b4-2a2ae2dbcce4"
      }
    }
  ],
  "meta": {
    "api": {
      "version":"1"
    },
    "page": {
      "maxSize":25,
      "total":2
    }
  }
}
```

Example response with error (data is empty, so we use ```Void``` class as generic parameter):
```java
// Pseudo code
public class Test {
    public Response<Void> main() {
        return Response.<Void, Void>builder()
            .error(
                HttpStatus.BAD_REQUEST,
                "20045",
                "Some errors occurred!"
            ).build();
    }
}
```
```json
{
  "errors": [
    {
      "status": 400,
      "code": "20045",
      "detail": "Some errors occurred!"
    }
  ],
  "meta": {
    "api": {
      "version": "1"
    },
    "page": {
      "maxSize": 25,
      "total": 0
    }
  }
}
```

Example response with error with links (data is empty, so we use ```Void``` class as generic parameter):
```java
// Pseudo code
public class Test {
    public Response<Void> main() {
        final Error.ErrorLink errorLink = Error.ErrorLink
            .builder()
            .about("https://example.org/docs/TOKEN_ERROR")
            .build();
        
        return Response.<Void, Void>builder()
            .error(
                HttpStatus.BAD_REQUEST,
                "20045",
                "Some errors occurred!",
                errorLink
            ).build();
    }
}
```
```json
{
  "errors": [
    {
      "status": 400,
      "code": "20045",
      "detail": "Some errors occurred!",
      "links": {
          "about": "https://example.org/docs/20045"
      }
    }
  ],
  "meta": {
    "api": {
      "version": "1"
    },
    "page": {
      "maxSize": 25,
      "total": 0
    }
  }
}
```

Example response with validation error and field name with invalid data (data is empty so we use ```Void``` 
class as generic parameter):
```java
// Pseudo code
public class Test {
    public Response<Void> main() {
        return Response.<Void, Void>builder()
            .validationError("field-name", "Field must be greater 0!")
            .build();
    }
}
```
```json
{
  "errors": [
    {
      "status": 400,
      "code": "VALIDATION_ERROR",
      "detail": "Field must be greater 0!",
      "source": {
        "parameter": "field-name"
      }
    }
  ],
  "meta": {
    "api": {
      "version": "1"
    },
    "page": {
      "maxSize": 25,
      "total": 0
    }
  }
}
```

Example response with trace id (trace id can be string or UUID):
```java
// Pseudo code
public class Test {
    public Response<Void> main() {
        return Response.<Void, Void>builder()
            .data(
                TestDto.builder()
                    .id(UUID.randomUUID())
                    .name("Test string")
                    .createDate(LocalDateTime.now(ZoneOffset.UTC))
                    .build()
            ).metaTrace(new Meta.Trace(UUID.randomUUID()))
            .build();
    }
}
```
```json
{
    "data": {
        "type":"test-object",
        "id":"7a543e90-2961-480e-b1c4-51249bf0c566",
        "attributes": {
            "id":"7a543e90-2961-480e-b1c4-51249bf0c566",
            "name":"Test string",
            "createDate":"2019-10-08T18:46:53.40297"
        },
        "links": {
            "self": "/test-object/7a543e90-2961-480e-b1c4-51249bf0c566"
        }
    },
    "meta": {
        "api": {
            "version":"1"
        },
        "page": {
            "maxSize":25,
            "total":1
        },
        "trace": {
            "id": "1a443e90-6961-480e-b1c4-51249bf0c566"
        }
    }
}
```
