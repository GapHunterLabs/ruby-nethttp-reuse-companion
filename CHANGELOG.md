<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Ruby Net::HTTP Reuse Companion Changelog

## [Unreleased]

## [0.1.0]

### Added

- Warning on a `Net::HTTP.get`/`.get_response`/`.post` shorthand call
  found inside a loop/iterator block -- pays a fresh TCP round-trip
  per iteration instead of reusing one persistent connection.
- 100% static text analysis, no Ruby plugin dependency, no network
  calls, no telemetry. Free.

[Unreleased]: https://github.com/GapHunterLabs/ruby-nethttp-reuse-companion/compare/0.1.0...HEAD
[0.1.0]: https://github.com/GapHunterLabs/ruby-nethttp-reuse-companion/commits/0.1.0
