## Description
Simple implementation of the JSON:API specification (only required output fields)

## Example responses
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
    public Response<Data<TestDto>> main() {
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
  "errors":[
    {
      "status":400,
      "code":"TOKEN_ERROR",
      "detail":"Some errors occurred!"
    }
  ],
  "meta":{
    "api":{
      "version":"1"
    },
    "page":{
      "maxSize":25,
      "total":0
    }
  }
}
```