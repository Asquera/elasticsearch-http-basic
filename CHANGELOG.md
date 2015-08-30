# Change Log

All notable changes to this project will be documented in this
file. This file is structured according to http://keepachangelog.com/
Use  `date "+%Y-%m-%d"` to get the correct date formatting

- - -
## [1.5.1][2015-08-30]
### - Added
- allow HEAD root url authentication #39
- log http method on any request. #42
- doc: 1.6.0, 1.7.0 support #52
### - Fix
- test: adapt to method signature change after 1.5.1 #55
- test: run custom install and test commands in ci

## [1.5.0][2015-07-04]

### - Added
- allow disabling ipwhitelist by setting its value to `false`
- updated pom to depend on elasticsearch-parent project
- travis test matrix for different ES versions

### Changed
- restored default healthcheck for authenticated users
- unauthenticated healthcheck for `/` returns `"{\"OK\":{}}"`
- thanks @feaster83

## [1.4.0]
## [1.0.3]

### - Added
- Changelog
- Disable Authentication for `/`, allowing it to be used for healtchecks.
  - thanks @archiloque
