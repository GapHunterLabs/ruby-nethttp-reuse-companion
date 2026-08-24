require "net/http"
require "uri"

# Demo data for Ruby Net::HTTP Reuse Companion — used with
# `./gradlew runIde` to capture the real Marketplace screenshot. Open
# this file, the warning should appear on the Net::HTTP.get line
# inside fetch_all.
def fetch_all(urls)
  urls.each do |url|
    # A fresh connection opened+closed on every URL -- FLAGGED.
    response = Net::HTTP.get(URI(url))
    puts response
  end
end

def fetch_all_correctly(urls, host, port)
  Net::HTTP.start(host, port) do |http|
    urls.each do |url|
      # Reuses the one persistent connection -- NOT flagged.
      response = http.get(url)
      puts response
    end
  end
end
