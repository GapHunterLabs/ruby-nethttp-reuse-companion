# Ruby Net::HTTP Reuse Companion

Warning on a `Net::HTTP.get`/`.get_response`/`.post` shorthand call
found inside a loop/iterator block (`.each do`, `.times do`, `while`,
`for`, brace-block `{ }`) — Ruby's own `Net::HTTP` documentation
explains why this matters: "Creating a new HTTP connection for every
request involves an extra TCP round-trip and causes TCP congestion
avoidance negotiation to start over". The shorthand class methods open
and close a connection on every single call, so using them inside a
loop means paying that cost on every iteration instead of once via
`Net::HTTP.start(...)`.

## Why it exists

```ruby
urls.each do |url|
  response = Net::HTTP.get(URI(url))
end
```

compiles and runs fine — it just pays a full TCP handshake (and
congestion-avoidance restart) on every single URL instead of reusing
one persistent connection, silently making a bulk fetch far slower
than it needs to be.

## Why built this way

- **100% static text analysis** — a `do`/`end`/brace nesting-depth
  line scanner, not a real Ruby parser, so it works whether the Ruby
  plugin is installed or not.

## v0.1 scope — stated honestly, not exhaustively

Only recognizes the common `.each do`/`.times do`/`while`/
`for ... do`/brace-block loop-header shapes — a loop built some other
way (recursion, an unusual `loop { }` form) isn't specially tracked.
Matches by simple text, not real call resolution — an unrelated
`.get`/`.post` method on some other object is a possible (rare) false
positive.

## Usage

Open any `.rb` file. A `Net::HTTP.get`/`.get_response`/`.post` call
inside a loop shows a warning.

## Enterprise / Team Licensing

Need enterprise features, custom rules, or team licensing? Contact us at
**gaphunterlabs@gmail.com**.

## Development

```
./gradlew test           # unit tests
./gradlew buildPlugin    # generates build/distributions/*.zip
./gradlew verifyPlugin   # checks compatibility against real IDEs
```

## License

Apache-2.0. See `LICENSE`.
