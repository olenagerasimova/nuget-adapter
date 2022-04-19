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
storage (binary, Amazon S3 objects) into a NuGet repository. It provides NuGet repository 
support for [Artipie](https://github.com/artipie) distribution and allows you to use `nuget` client
commands (such as `nuget push` and `nuget install`) to work with NuGet packages. Besides, NuGet-adapter
can be used as a library to parse `.nuget` packages files and obtain package metadata.

Similar solutions:

  * [Artifactory](https://www.jfrog.com/confluence/display/RTF/NuGet+Repositories)
  * [NuGet](https://www.nuget.org/)

Some valuable references:

  * [NuGet Documentation](https://docs.microsoft.com/en-us/nuget/)
  * [NuGet Sources](https://github.com/NuGet)

## NuGet-adapter SDK API

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

You may also use lower level classes to parse `.nupkg` files and read package `.nuspec` file:
```java
// create instance of NuGetPackage
final NuGetPackage pkg = new Nupkg(Files.newInputStream(Paths.get("my_example.nupkg")));
// read `.nuspec` metadata
final Nuspec nuspec = pkg.nuspec();

final NuspecField id = nuspec.id(); // get packages id
final NuspecField veersion = nuspec.version(); // get package version
```
Instance of `NuspecField` classes allows to obtain both raw and normalized 
(according to Nuget normalization rules) values of the fields. `Nuspec` allows to get description,
authors, packages types and any other `.nuspec` metadata fields value. 

Class `Version` can be used to normalise the version, it also implements `Comparable<Version>` 
interface and can be used to sort the package by versions.

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
