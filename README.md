# About

This Gradle Plugin adds tasks for creating launcher scripts and for creating
installation and distribution mechanism that are not provided with the default
tasks designed for these purposes.

## Gradle compatibility note

Unfortunately it seems impossible to implement this plugin such that it does
not trigger any warnings on Gradle 7+ while maintaining backwards compatibility
with Gradle 6 and below. While it is possible to branch within the
implementation based on `GradleVersion.current()`, the resulting plugin is still
incompatible with Gradle 6-. This seems to be caused by Gradle 7 using Groovy 3
while Gradle 6- use Groovy 2. See https://github.com/gradle/gradle/issues/18570

# License

This software is released under the terms of the GNU Lesser General Public
License.

See  [LGPL.md](LGPL.md) and [GPL.md](GPL.md) for details.
