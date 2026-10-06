<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Ruby Net::HTTP Reuse Companion Changelog

## [Unreleased]

### Added

- A description page for the inspection in **Settings | Editor |
  Inspections**, which showed "Under construction".

### Changed

- The rating prompt's local counter keeps one-way fingerprints of findings
  instead of their file paths, and deletes the list that earlier versions
  kept.
- `PRIVACY.md` describes the values the plugin keeps in the IDE's local
  settings.

## [0.1.1]

### Fixed

- Review/star CTA now links to this plugin's own Marketplace
  reviews page instead of the vendor's generic plugin list.

## [0.1.0]

### Added

- Warning on a `Net::HTTP.get`/`.get_response`/`.post` shorthand call
  found inside a loop/iterator block -- pays a fresh TCP round-trip
  per iteration instead of reusing one persistent connection.
- 100% static text analysis, no Ruby plugin dependency, no network
  calls, no telemetry. Free.

[Unreleased]: https://github.com/GapHunterLabs/ruby-nethttp-reuse-companion/compare/0.1.1...HEAD
[0.1.1]: https://github.com/GapHunterLabs/ruby-nethttp-reuse-companion/compare/0.1.0...0.1.1
[0.1.0]: https://github.com/GapHunterLabs/ruby-nethttp-reuse-companion/commits/0.1.0
