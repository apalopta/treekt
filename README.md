# treekt

`treekt` is a `tree`-like tool written in Kotlin.

The standard Windows' _tree_ command lacks of configurability.
The _treee_ alternative did not convince me either.

So I took the chance and started my own one.

## Usage

    > treekt -h
    Usage: treekt options_list
    Options:
    --depth, -l [256] -> number of levels (max. 256) { Int }
    --dir, -d [C:\git\treekt-dev] -> directory { String }
    --skip, -s -> skip pattern { String }
    --skipDir, -sd -> skip directory { String }
    --skipFile, -sf -> skip file { String }
    --hideSystemDirs, -hsd [false] -> hide system directories
    --hideSystemFiles, -hsf [false] -> hide system files
    --out, -o -> output file { String }
    --help, -h -> Usage info

## Example

To output file ignoring system files and directories as well as `build` and `out` directory and all `settings`:

    treekt --depth 3 -hsd -hsf -o "out.txt" -sd build -sd out -s ".*settings.*"

## Output

    C:\git\treekt
    +---gradle
    |   \---wrapper
    |       gradle-wrapper.jar
    |       gradle-wrapper.properties
    +---treekt
    |   \---src
    |       +---main
    |       \---test
    |   build.gradle.kts
    gradlew
    gradlew.bat
    out.txt
    README.md
