# mp3-parser [![Build](https://github.com/digital-dj-tools/mp3-parser/workflows/Build/badge.svg)](https://github.com/digital-dj-tools/mp3-parser/actions?query=workflow%3ABuild)

A library for parsing headers of mp3 files, including MPEG header frame data and any Xing/Lame tags.

## Features

- Runs on Clojure and ClojureScript
- Parse and report existence of an ID3V2 header
- Skip any ID3V2 tag, if it exists
- Parse and report MPEG header frame data
- Parse and report existence of a Xing tag in the header frame
- Parse and report Xing tag data, if it exists
- Parse and report existence of a Lame tag in the header frame
- Parse and report Lame tag data, if it exists
- Calculate the CRC-16 of the Lame tag data, compare it with the stored CRC-16, and report if it is valid or not

## Status

**Alpha**

## Dependencies

- Java 8
- Clojure [command line tools](https://clojure.org/guides/getting_started#_clojure_installer_and_cli_tools
)
- NodeJS (if using ClojureScript)

Tested with Clojure 1.10.1, ClojureScript 1.10.520

## Usage

### Clojure deps.edn with Git

Add the following to `:deps` in the `deps.edn` file for the project:

```
digital-dj-tools/mp3-parser {:git/url "https://github.com/digital-dj-tools/mp3-parser.git"
                             :sha "515c4838dc0b1eadebe64ec5f0a9ed827cfed54c"}
```

### Clojure deps.edn with Clojars

TODO

### Sync API Example

Go to the project directory and start a REPL:
```
clj
```
Enter the following code:
```
(require
 '[mp3-parser.app :as app])

(def mp3-file
  "test-resources/lame-mp3-cbr-stereo-44100khz-192kbps-id3v2.mp3")

(app/parse mp3-file)
```
The following result should be printed:
```
{:id3v2-offset 42005
 :mpeg-valid? true
 :mpeg {:mp3-parser.mpeg/bit-rate 192000
        :mp3-parser.mpeg/frame-length 626.0
        :mp3-parser.mpeg/has-padding? false
        :mp3-parser.mpeg/header [-1 -5 -80 100]
        :mp3-parser.mpeg/layer 3
        :mp3-parser.mpeg/num-channels 2
        :mp3-parser.mpeg/num-samples 1152
        :mp3-parser.mpeg/sample-rate 44100
        :mp3-parser.mpeg/version 1}
 :xing-tag? true
 :xing {:mp3-parser.xing/keyword "Info", 
        :mp3-parser.xing/offset 36}
 :lame-tag? true
 :lame {:mp3-parser.lame/crc-valid? true 
        :mp3-parser.lame/encoder "LAME3.99r", 
        :mp3-parser.lame/revision 0}}
```

### Async API Example

TODO

### API Docs

TODO

## Developers

### Running the Tests

```
clj -Adev:test
```

```
clj -Adev:test-cljs
```

### Starting a CIDER-compatible NREPL Server
```
clj -Adev:nrepl-server
```

## License

Copyright © 2018 Digital DJ Tools

Released under the MIT license.
