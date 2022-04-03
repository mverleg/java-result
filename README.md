
# Java `Result` type

`Result` is a value that can be either `Ok` or `Err`, to signal whether an operation succeeded or failed. Each variant can contain data, e.g. `Result<User, ErrMsg>` contains a `User` if ok, and `ErrMsg` when it fails.

It can be used as a return value from functions to indicate if they succeeded or failed, similar to `Optional`, but with data about _why_ it failed.

## Project status

Java-result is feature-complete and can be used in non-critical projects in Java 17+ (or 15+ with preview features). It has extensive unit test coverage, but limited real-world testing.

## Examples



## Install

Result is available on Central: [nl.markv.result](https://search.maven.org/artifact/nl.markv/result/1.1.0/jar).

### Maven

For maven 17+, add this dependency:

```
<dependency>
    <groupId>nl.markv</groupId>
    <artifactId>result</artifactId>
    <version>1.1.0</version>
</dependency>
```

If you are using Java 15 or 16, in addition to adding the dependency above, you need to enable preview features:

```
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.8.1</version>
    <configuration>
        <source>15</source>
        <target>15</target>
        <compilerArgs>--enable-preview</compilerArgs>
    </configuration>
</plugin>
```

Maven 14 or earlier is not supported (because there are no sealed types).

### Gradle

For Java 17+, add this dependency:

```
implementation 'nl.markv:result:1.1.0'
```

## Sealed types in Java

With sealed interfaces in Java 15 (preview feature), it finally has decent support for sum types - algebraic types that can have one out of a finite set of values. They are sometimes called unions or composite types.

You can think of it as an enhanced [`enum`](https://docs.oracle.com/en/java/javase/13/language/switch-expressions.html), where each variant is a different subtype, instead of single instance. Each variant can have a different structure, and can have any number of instances.

Many languages that support sum types, like [Kotlin](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-result/), [Haskell](https://hackage.haskell.org/package/base-4.14.1.0/docs/Data-Either.html), [Rust](https://doc.rust-lang.org/std/result/), [Swift](https://www.swiftbysundell.com/articles/the-power-of-result-types-in-swift/), [C++](https://bell0bytes.eu/expected/) or [others](https://en.wikipedia.org/wiki/Result_type), have some kind of type that indicates one of two options - for example, success or failure.  Java has [`Optional`](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html), but that cannot contain an error value.

Result is a popular example of such types, which has two variants: one for success and one for failure. It can be used for error handling.

If you are familiar with [monads](https://adambennett.dev/2020/05/the-result-monad/), `Result` is a monad with unit operations `ok`/`err`, bind operation `map`/`mapErr` and a flattening operation `flatMap`.  
