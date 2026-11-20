#!/bin/bash
# RooTerror Pixel Dungeon — prepares and builds the Sailfish OS RPMs (aarch64 and armv7hl).
# Usage: sailfish/packaging/prepara-pacchetto.sh [aarch64|armv7hl|tutte]   (default: tutte = both)
#
#  1. game jar: gradle sailfish:release (needs a full JDK 17)
#  2. small Java runtime for each arch with jlink (host jlink + the target JDK's jmods;
#     no --strip-debug: it would run the host objcopy on ARM files)
#  3. staging/ next to this script (jar, jre, icons, licence)
#  4. sfdk build (compiles the launcher and the Wayland touch library, packages)
# Output: sailfish/packaging/RPMS/rooterror-pixel-dungeon-*.{aarch64,armv7hl}.rpm
#
# Needs (paths can be changed with the variables below):
#  - JDK_HOST:    Temurin JDK 17 for the build machine (x64), e.g. jdk-17.0.17+10
#  - JDK_AARCH64: Temurin JDK 17 for Linux aarch64 (only its jmods/ are used)
#  - JDK_ARM32:   Temurin JDK 17 for Linux arm (32 bit, hard float; only jmods/)
#  - SFDK:        the Sailfish SDK command line tool, with the SailfishOS-<ver>-<arch> targets
#  Temurin downloads: https://adoptium.net/temurin/releases/?version=17
set -euo pipefail
QUI="$(cd "$(dirname "$0")" && pwd)"            # sailfish/packaging
REPO="$(cd "$QUI/../.." && pwd)"                # root of the game repository
STRUMENTI="${STRUMENTI:-$REPO/../strumenti}"    # where the JDKs are kept outside the repo
JDK_HOST="${JDK_HOST:-$STRUMENTI/jdk/x64/jdk-17.0.17+10}"
JDK_AARCH64="${JDK_AARCH64:-$(ls -d "$STRUMENTI"/jdk/aarch64/jdk-*/ 2>/dev/null | head -1)}"
JDK_ARM32="${JDK_ARM32:-$(ls -d "$STRUMENTI"/jdk/arm32/jdk-*/ 2>/dev/null | head -1)}"
SFDK="${SFDK:-$HOME/SailfishOS/bin/sfdk}"
SFOS="${SFOS:-5.1.0.11}"                        # target release of the SDK
S="$QUI/staging"
OUT="$QUI/build"                                # jlink output (ignored by git)

case "${1:-tutte}" in
    aarch64|armv7hl) ARCHS="$1" ;;
    tutte) ARCHS="aarch64 armv7hl" ;;
    *) echo "usage: $0 [aarch64|armv7hl|tutte]" >&2; exit 2 ;;
esac
[ -x "$JDK_HOST/bin/jlink" ] || { echo "JDK_HOST not found: $JDK_HOST" >&2; exit 1; }

echo "== 1. jar"
( cd "$REPO" && JAVA_HOME="$JDK_HOST" ./gradlew -q \
    -Dorg.gradle.java.installations.paths="$JDK_HOST" sailfish:release )

rm -rf "$QUI/RPMS.tutti" && mkdir -p "$QUI/RPMS.tutti" "$OUT"
for ARCH in $ARCHS; do
    case $ARCH in
        aarch64) A="$JDK_AARCH64" ;;
        armv7hl) A="$JDK_ARM32" ;;
    esac
    [ -d "${A%/}/jmods" ] || { echo "jmods for $ARCH not found: $A" >&2; exit 1; }

    echo "== 2. Java runtime $ARCH"
    rm -rf "$OUT/jre-$ARCH"
    "$JDK_HOST/bin/jlink" --module-path "${A%/}/jmods" \
        --add-modules java.base,java.desktop,jdk.unsupported \
        --strip-native-commands --strip-java-debug-attributes \
        --no-header-files --no-man-pages --compress=2 \
        --output "$OUT/jre-$ARCH"

    echo "== 3. staging $ARCH"
    rm -rf "$S" && mkdir -p "$S/icons"
    cp -a "$OUT/jre-$ARCH" "$S/jre"
    cp "$REPO/sailfish/build/libs/rooterror-pixel-dungeon.jar" "$S/"
    cp "$REPO/LICENSE.txt" "$S/LICENSE"
    for s in 86 108 128 172 256; do
        mkdir -p "$S/icons/${s}x${s}"
        cp "$REPO/sailfish/grafica/icone/${s}x${s}/rooterror-pixel-dungeon.png" "$S/icons/${s}x${s}/"
    done

    echo "== 4. sfdk $ARCH"
    cd "$QUI"
    rm -rf RPMS rooterror-pixel-dungeon librootouch.so
    # no-fix-version: inside a git repository sfdk would take the version from the tags
    # (Shattered's own, e.g. 4.0.3.beta) instead of the spec
    "$SFDK" -c no-fix-version -c target="SailfishOS-$SFOS-$ARCH" build 2>&1 | grep -v "SOFT ASSERT"
    # sfdk's exit code is hidden behind the pipe: check that the RPM is there
    ls RPMS/*.$ARCH.rpm >/dev/null
    mv RPMS/*.$ARCH.rpm RPMS.tutti/
done
rm -rf "$QUI/RPMS" && mv "$QUI/RPMS.tutti" "$QUI/RPMS"
rm -rf "$S" "$QUI/rooterror-pixel-dungeon" "$QUI/librootouch.so"
ls -la "$QUI/RPMS/"
