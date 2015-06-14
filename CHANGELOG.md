# Change Log

All notable changes to this project will be documented in this
file. This file is structured according to http://keepachangelog.com/

- - -
## [1.5.0][unreleased]
### - Added
- allow disabling ipwhitelist by setting its value to `false`
- updated pom to depend on elasticsearch-parent project
- better travis test for different ES versions

### Changed
- restored default healthcheck for authenticated users &&
- unauthenticated healthcheck for `/` returns `"{\"OK\":{}}"`
- thanks @feaster83

## [1.4.0]
## [1.0.3]

### - Added
- Changelog
- Disable Authentication for `/`, allowing it to be used for healtchecks.
  - thanks @archiloque
