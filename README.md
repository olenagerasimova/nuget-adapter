<img src="https://www.artipie.com/logo.svg" width="64px" height="64px"/>

[![EO principles respected here](https://www.elegantobjects.org/badge.svg)](https://www.elegantobjects.org)
[![DevOps By Rultor.com](http://www.rultor.com/b/artipie/nuget-adapter)](http://www.rultor.com/p/artipie/nuget-adapter)
[![We recommend IntelliJ IDEA](https://www.elegantobjects.org/intellij-idea.svg)](https://www.jetbrains.com/idea/)

[![Javadoc](http://www.javadoc.io/badge/com.artipie/nuget-adapter.svg)](http://www.javadoc.io/doc/com.artipie/nuget-adapter)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/com.artipie/nuget-adapter/blob/master/LICENSE.txt)
[![codecov](https://codecov.io/gh/artipie/nuget-adapter/branch/master/graph/badge.svg)](https://codecov.io/gh/artipie/nuget-adapter)
[![Hits-of-Code](https://hitsofcode.com/github/artipie/nuget-adapter)](https://hitsofcode.com/view/github/artipie/nuget-adapter)
[![Maven Central](https://img.shields.io/maven-central/v/com.artipie/nuget-adapter.svg)](https://maven-badges.herokuapp.com/maven-central/com.artipie/nuget-adapter)
[![PDD status](http://www.0pdd.com/svg?name=artipie/nuget-adapter)](http://www.0pdd.com/p?name=artipie/nuget-adapter)

This Java library turns your binary [ASTO](https://github.com/artipie/asto) 
storage into a NuGet repository.

Similar solutions:

  * [Artifactory](https://www.jfrog.com/confluence/display/RTF/NuGet+Repositories)
  * [NuGet](https://www.nuget.org/)

Some valuable references:

  * [NuGet Documentation](https://docs.microsoft.com/en-us/nuget/)
  * [NuGet Sources](https://github.com/NuGet)

## Getting started

Add dependency to `pom.xml`:

```xml
<dependency>
  <groupId>com.artipie</groupId>
  <artifactId>nuget-adapter</artifactId>
  <version>[...]</version>
</dependency>
```

Save NuGet package ZIP file like `package.nupkg` (particular name does not matter)
to [ASTO](https://github.com/artipie/asto) storage. 
Then, make an instance of `Repository` class with storage as an argument.
Finally, instruct `Repository` to add the package to repository:

```java
import com.artipie.nuget;
Repository repo = new Repository(storage);
repo.add(new Key.From("package.nupkg"));
```

## Project status

- [x] Adding package to repository [#1](https://github.com/artipie/nuget-adapter/issues/1)
- [x] HTTP support for installing package [#19](https://github.com/artipie/nuget-adapter/issues/19)
- [x] HTTP support for adding package [#20](https://github.com/artipie/nuget-adapter/issues/20)
- [ ] HTTP support for listing package versions [#29](https://github.com/artipie/nuget-adapter/issues/29)

## How to contribute

Fork repository, make changes, send us a pull request. We will review
your changes and apply them to the `master` branch shortly, provided
they don't violate our quality standards. To avoid frustration, before
sending us your pull request please run full Maven build:

```
$ mvn clean install -Pqulice
```

To avoid build errors use Maven 3.2+.

The test suite of this project include some integration tests which require NuGet client to be installed.
NuGet client may be downloaded from official site [nuget.org](https://www.nuget.org/downloads).
Integration tests could also be skipped using Maven's `skipITs` options:

```
$ mvn clean install -Pqulice -DskipITs
```
