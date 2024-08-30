# Changelog

## [1.12.0] - 2024-08-30
### Added:
- Support for sort field

### Changed:
  - Update all dependencies
  - Replace **springfox-swagger** with the **springdoc-openapi-starter-webmvc-ui** dependency

## [1.11.0] - 2023-08-02
### Added:
  - Error meta field for meta object containing non-standard meta-information about the error

## [1.10.1] - 2023-06-17
### Fixed:
  - Missing **@NoArgsConstructor** annotation on **Meta** class for proper jackson response deserialization

## [1.10.0] - 2023-02-21
### Changed:
  - Add id field to attributes (more usable for api clients)
  - Update all dependencies versions

## [1.9.0] - 2022-11-07
### Added:
  - Trace object with id field to meta object

## [1.8.0] - 2022-11-01
### Added:
  - Error links object support

### Changed:
  - Update all dependencies versions

## [1.7.0] - 2022-08-22
### Added:
  - Methods to get typed params from filter (i.e. **stringValue**, **listOfUuidValues** etc.)
  
### Changed:
  - Page now starts from 0 instead of 1

### Changed:
  - Update all dependencies versions

## [1.6.1] - 2022-02-15
### Changed:
  - Replace @Setter lombok annotation in Page class on @Data and add @JsonInclude(JsonInclude.Include.NON_NULL) annotation

## [1.6.0] - 2022-02-10
### Added:
  - Now possible to deserialize string value as JSON API response
  - Method to set manually JSON API id without **@JsonApiId** annotation when set manually JSON API data type.

## [1.5.0] - 2022-02-09
### Added:
  - Pagination support
  - Method to set manually JSON API data type without **@JsonApiType** annotation to use different data types like maps, lists etc.

### Changed:
  - Update mockito version 

## [1.4.0] - 2022-01-27
### Added:
  - Methods to get all filter keys and values

### Changed:
  - Update all dependencies versions

## [1.3.0] - 2021-08-27
### Changed:
  - Maven groupId now com.slm-dev
  - Update all dependencies versions
  - Update README file

## [1.2.0] - 2020-11-22
### Added:
  - Sparse fieldsets support 
  - Examples for sparse fieldsets to README
  
### Changed:
  - Default JDK version 11
  - Update all dependencies versions
  - Remove id field from attributes field by JSON:API specification

## [1.1.0] - 2019-12-18
### Added:
  - Conditions in filters (in(), eq() etc.)
  - Resources self links support
  - Method uri() to change base URI in resources self links
  
### Changed:
  - Add examples for a filter to README

## [1.0.0] - 2019-10-16
### Added:
  - Basic logic
  - README file with documentation
  - CHANGELOG.md file
  - Global lombok config
