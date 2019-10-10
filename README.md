## Overview
Simple implementation of the [JSON:API](https://jsonapi.org) specification (only required output fields).
This library implements only top-level fields: **data**, **errors** and **meta** without any **relationships**, **includes**
and others. 

Often we only need standard of output all our endpoints especially when using many types of communications like HTTP query,
websockets, queues etc and don't need complex entities inner relationships in our API but it's good if implementing some
exists standards so this library for this goals.

## Usage
Each response DTO should contain the annotation ```@JsonApiType("resource-type")``` with any resource type identifier and
the annotation ```@JsonApiId``` without arguments on field which will be unique identifier this item, usually this 
field is ```id```.

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

Then you can use annotation ```@RequestJsonApiFilter``` in controllers for example:
```java
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(value = "/app", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class RestController {
    @JsonGetMapping
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
Now if request will be contain fields like ```filter[key1]=value1,value2&filter[key2]=value1``` we can get them in the
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
      }
    },
    {
      "type":"test-object",
      "id":"b4070518-e9fc-11e9-81b4-2a2ae2dbcce4",
      "attributes": {
        "id":"b4070518-e9fc-11e9-81b4-2a2ae2dbcce4",
        "name":"Test string 2",
        "createDate":"2019-10-08T18:46:51"
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
            .error(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR", // This is app internal code for frontend to determine that is validation error
                "Field must be greater 0!",
                "field-name"
            ).build();
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