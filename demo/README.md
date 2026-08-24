# Demo data — Ruby Net::HTTP Reuse Companion

For capturing the real Marketplace screenshot:

1. `./gradlew runIde`
2. Open `demo/fetcher.rb` as a scratch/standalone file (or drop it
   into any sandbox project) inside the sandbox IDE.
3. The `Net::HTTP.get(URI(url))` call inside `fetch_all` shows the
   warning — hover it for the tooltip. `fetch_all_correctly`'s
   `Net::HTTP.start(...)` + `http.get(url)` pattern stays clean, for
   contrast.
4. Enter Full Screen (`View > Appearance > Enter Full Screen`), capture
   with `Win+Shift+S`, save directly to `docs/screenshots/` in this
   repo.
