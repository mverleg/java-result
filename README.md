
[![javadoc](https://javadoc.io/badge2/nl.markv/result/javadoc.svg)](https://javadoc.io/doc/nl.markv/result/latest/nl/markv/result/Result.html)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/nl.markv/result/badge.svg)](https://search.maven.org/artifact/nl.markv/result)
[![Unit tests](https://github.com/mverleg/java-result/actions/workflows/test.yml/badge.svg)](https://github.com/mverleg/java-result/actions/workflows/test.yml)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](./LICENSE.txt)

# Java `Result` type

`Result` is a value that can be either `Ok` or `Err`, to signal whether an operation succeeded or failed. Each variant can contain data, e.g. `Result<User, ErrMsg>` contains a `User` if ok, and `ErrMsg` when it fails.

It can be used as a return value from functions to indicate if they succeeded or failed, similar to `Optional`, but with data about _why_ it failed.

## Project status

Java-result is feature-complete and can be used in Java 15+. It has extensive unit test coverage, but limited real-world testing.

## Examples

Functions can return `Result` to indicate whether they failed and why:

```java
@Nonnull
public static Result<Integer, DivError> divide(@Nullable Integer numerator, @Nullable Integer divisor) {
    if (null == numerator) {
        return Result.err(DivError.NUMERATOR_NULL);
    }
    if (null == divisor) {
        return Result.err(DivError.DIVISOR_NULL);
    }
    if (0 == divisor) {
        return Result.err(DivError.DIVISOR_ZERO);
    }
    return Result.ok(numerator / divisor);
}
```

* Only use the value **if successful**
  
   ```java
   Result<Integer, DivError> costPerPerson = divide(cost, people);
   if (costPerPerson.isOk()) {
       sendMessage("You need to pay " + costPerPerson.get());
   }
   ```

   or more tersely:

   ```java
   divide(cost, people)
        .ifOk(costPerPerson -> sendMessage("You need to pay " + costPerPerson));
   ```

* **Chain operations**, transforming the results only if it is successful:

   ```java
   divide(9, 0).map(result -> result / 2)
   // Err(DIVISOR_ZERO)
   ```
   
   of if the transformation can also fail:

   ```java
   divide(8, 2).flatMap(res -> divide(res, 2))
   // Ok(2)
   ```

* **Fallback** to a default in case of failure:

   ```java
   divide(8, 0).withoutErr().orElse(1)
   // Ok(1)
   divide(8, 0).withoutErr().orElseGet(() -> calculateFallback())
   // calculateFallback() computed only if failed
   divide(8, 0).withoutErr().recover(err -> calculateFallback2(err))
   // calculateFallback2(err) computed only if failed
   ```

* **From exception** to `Result`:

   ```java
   Result<String, Exception> userName = Result.attempt(() -> findUserName());
   ```

* If you have decided to **not handle errors**:

   ```java
   divide(8, divisor).getOrThrow("if you see this, sorry...")
   // throws
   ```
  
* Handle success result or **adjust type**:

   ```java
   Result<String, Exception> userNameResult = Result.attempt(() -> findUserName());
   if (userNameResult instanceof Ok<String, Exception> userName) {
       return doSomethingWithUsername(userName.get());
   } else {
       return userNameResult.adaptOk();
       // Changes the ok type, which is safe because this is a failed Tesult
   }
   ```

* **Keep only success** results, because `Result.stream` contains only the success value or nothing:

   ```java
   List<Result<Integer, String>> list = List.of(ok(1), ok(2), err("problem"), ok(4));
   List<Integer> successesOnly = list.stream()
       .flatMap(Result::stream).toList();
   // [1, 2, 4]
   ```

* Get all success values if all results are all successful, or the first error otherwise:

   ```java
   List<Result<Integer, String>> list = List.of(ok(1), ok(2), err("problem"), ok(4));
   Result<List<Integer>, String> listResult = Result.transpose(list);
   // Err(problem)
   ```
  
   There is also a `Stream` collector that does the same thing:

   ```java
   Result<List<Integer>, DivError> streamResult = Stream.of(2, 1, 0, -1, -2)
       .map(nr -> divide(10, nr))
       .collect(ResultCollector.toList());
   // Err(DIVISOR_ZERO)
   ```

There is a lot more, [have a look at the source](src/main/java/nl/markv/result/Result.java).

## Install

Java-result is available on Central: [nl.markv.result](https://search.maven.org/artifact/nl.markv/result).

### Maven

Add this dependency:

```xml
<dependency>
    <groupId>nl.markv</groupId>
    <artifactId>result</artifactId>
    <version>1.1.0</version>
</dependency>
```

For Java 15/16 this uses preview features. Java 14 and below are not supported.

### Gradle

For Java 15+, add this dependency:

```groovy
implementation 'nl.markv:result:1.1.0'
```

For Java 15/16 this uses preview features. Java 14 and below are not supported.

## Sealed types in Java

With sealed interfaces in Java 15 (preview feature), it finally has decent support for sum types - algebraic types that can have one out of a finite set of values. They are sometimes called unions or composite types.

You can think of it as an enhanced [`enum`](https://docs.oracle.com/en/java/javase/13/language/switch-expressions.html), where each variant is a different subtype, instead of single instance. Each variant can have a different structure, and can have any number of instances.

Many languages that support sum types, like [Kotlin](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-result/), [Haskell](https://hackage.haskell.org/package/base-4.14.1.0/docs/Data-Either.html), [Rust](https://doc.rust-lang.org/std/result/), [Swift](https://www.swiftbysundell.com/articles/the-power-of-result-types-in-swift/), [C++](https://bell0bytes.eu/expected/) or [others](https://en.wikipedia.org/wiki/Result_type), have some kind of type that indicates one of two options - for example, success or failure.  Java has [`Optional`](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html), but that cannot contain an error value.

Result is a popular example of such types, which has two variants: one for success and one for failure. It can be used for error handling.

If you are familiar with [monads](https://adambennett.dev/2020/05/the-result-monad/), `Result` is a monad with unit operations `ok`/`err`, bind operation `map`/`mapErr`, and a flattening operation `flatMap`/`flatMapErr` or `flatten`. 
