## Overview
Simple implementation of the [JSON:API](https://jsonapi.org) specification (only required output fields).
This library implements only top-level fields: **data**, **links**, **errors** and **meta** without any **relationships**, **includes**
and others. 

Often we only need standard of output all our endpoints especially when using many types of communications like HTTP query,
websockets, queues etc and don't need complex entities inner relationships in our API but it's good if implementing some
exists standards so this library for this goals.

## Docs
[See project page](https://seregaslm.github.io/jsonapi-simple/)

## Usage
Add dependency to your project:
```xml
<dependency>
    <groupId>io.github.seregaslm</groupId>
    <artifactId>jsonapi-simple</artifactId>
    <version>1.0.0</version>
</dependency>
```

Each response DTO should contain the annotation ```@JsonApiType("resource-type")``` with any resource type identifier and
the annotation ```@JsonApiId``` without arguments on field which will be unique identifier this item, usually this 
field is ```id```.

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
            final List<String> filterValues = filter.getParam("key");

            // do something with filter param
        }
        return Response.<Void, Void>builder()
           .build();
    }
}
```
Now if request will be contain fields like ```filter[key1][in]=value1,value2&filter[key2]=value1``` we can get them in the
```Filter``` object.

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

Example response with error (data is empty so we use ```Void``` class as generic parameter):
```java
// Pseudo code
public class Test {
    public Response<Void> main() {
        return Response.<Void, Void>builder()
            .error(
                HttpStatus.BAD_REQUEST,
                "TOKEN_ERROR",
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
      "code": "TOKEN_ERROR",
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