# treekt

`treekt` is a `tree`-like tool written in Kotlin.

The standard Windows' _tree_ command lacks of configurability.
The _treee_ alternative did not convince me either.

So I started this one.

## How to Install

Unzip to a directory of your choice and set the _path_ to the `bin` directory.

You should also be able to use it from within a Gradle build (did not try this yet).

## Usage

`treekt`'s default: print the current directory and all its children recursively (as `tree` does).

However, you might need to tweak the output a bit.
There are some options:

    > treekt -h
    Usage: treekt options_list
    Options:
    --depth, -l [256] -> number of levels (max. 256) { Int }
    --dir, -d [C:\git\treekt-dev] -> directory { String }
    --showFiles, -f [false] -> show files
    --skip, -s -> skip pattern { String }
    --skipDir, -sd -> skip directory { String }
    --skipFile, -sf -> skip file { String }
    --hideSystemDirs, -hsd [false] -> hide system directories
    --hideSystemFiles, -hsf [false] -> hide system files
    --limitDirsTo, -td [2147483647] -> limit the number of displayed directories { Int }
    --limitFilesTo, -tf [2147483647] -> limit the number of displayed files { Int }
    --out, -o -> output file { String }
    --help, -h -> Usage info

## Example 1

Ignore system files and directories as well as `build` and `out` directory and all `settings`.
Show 3 levels.
Output goes to `out.txt`:

    treekt -f --depth 3 -hsf -hsd -sd build -sd out -s ".*settings.*" -o "out.txt" 

Output is something like:

    <dir>\treekt
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
    README.md

## Example 2

Sometimes the sheer number of directories obstructs the view of essential parts.
In that case limit the displayed files.

The example lists 4 levels, limits to 3 directories and 2 files.

    > treekt -f -l 4 -td 3 -tf 2 -sd buildSrc 

Output is something like:

    ----
    <dir>\treekt-dev
    +---.git
    |   +---hooks
    |   |   applypatch-msg.sample
    |   |   commit-msg.sample
    |   |   ...
    |   +---info
    |   |   exclude
    |   +---logs
    |   |   \---refs
    |   |       +---heads
    |   |       \---remotes
    |   |   HEAD
    |   \---...
    |   COMMIT_EDITMSG
    |   config
    |   ...
    +---gradle
    |   \---wrapper
    |       gradle-wrapper.jar
    |       gradle-wrapper.properties
    +---treekt
    |   \---src
    |       +---main
    |       |   +---kotlin
    |       |   \---resources
    |       \---test
    |           +---kotlin
    |           \---resources
    |   build.gradle.kts
    \---...
    .gitattributes
    .gitignore
    ...
    ----

## What's next?

At the moment I am quite happy with it.
It satisfies my personal requirements.
However, there are still ideas:

- make the tool accept repeated `-s`.
- other output formats (e.g., non-ascii)
- try it from a Gradle build for auto-documentation
