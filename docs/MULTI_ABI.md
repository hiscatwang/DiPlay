# DiPlay v12 Android 8.1 multi-ABI build

This local 2026-10-06 build extends the original v12 (siri) package with
`armeabi-v7a` (32-bit ARMv7) and `arm64-v8a` (64-bit ARMv8). Existing `x86` and
`x86_64` support is retained in the same universal APK. Both the mobile packaging
filter and the shared module native-build filter include all four ABIs, so each
architecture receives its own compiled JNI libraries.

Minimum Android remains 8.1 / API 27. The package name stays
`com.shihab.diplay.ora81`; versionCode stays 41 and the version name gains
`-universal`. Use the original signing key to replace the same-version APK while
preserving settings. A newer installed version cannot normally be downgraded to
this version. ARM hardware running a 32-bit OS uses armeabi-v7a; a 64-bit app process
uses arm64-v8a.

The archived v12 source is the baseline. Changes are limited to the two
ABI build filters, the return-to-car branding and migration, their tests and
documentation. The default CarPlay return entry now reuses the existing green
DiPlay icon (`drawable/ic_carplay.png`, copied unchanged to `raw/ic_car_home.png`)
and is labelled "返回车机". Persisted ORA/BYD default labels migrate on load;
other custom labels and user-selected icons remain intact. The icon picker text
is updated in all six existing locales. The previous ORA raw image is removed.

The original DiPlay app name, audio-focus behavior, routing and connection
behavior stay as in that version. The separately customized v13 CarPlay-name
build is not part of this work. The APKs and matching source archives are distributed in the [universal release](https://github.com/hiscatwang/DiPlay/releases/tag/v0.2.10-universal-v10-v12).

Build with JDK 25, SDK 37, NDK 28.2.13676358, and the included Gradle wrapper:

```sh
DIPLAY_AUTH_ASSETS_DIR=/absolute/external/runtime-assets ./gradlew :mobile:assembleStandaloneDebug :common:testDebugUnitTest :shared:testDebugUnitTest :common:lintDebug :shared:lintDebug :mobile:lintDebug
```

Runtime authentication assets are supplied outside the source tree and excluded
from the source archive. Preserve all existing upstream and third-party licenses.
This expands CPU architecture coverage; it does not establish compatibility with
every ARM head unit, vendor audio route, or firmware. See the package validation
report for build/ELF checks and any device tests actually performed. No real ARM
head-unit verification has been performed during this local build.
