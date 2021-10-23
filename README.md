
# Java `Result` type

**`Result` is a value that can be either `Ok` or `Err`, to signal whether an operation succeeded or failed. Each variant can contain data, e.g. `Result<User, ErrMsg>` contains a `User` if ok, and `ErrMsg` when it fails.**

With sealed interfaces in Java 15 (preview feature), it finally has decent support for sum types - algebraic types that can have one out of a finite set of values. They are sometimes called unions or composite types.

You can think of it as an enhanced [`enum`](https://docs.oracle.com/en/java/javase/13/language/switch-expressions.html), where each variant is a different subtype, instead of single instance. Each variant can have a different structure, and can have any number of instances.

Many languages that support sum types, like [Kotlin](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-result/), [Haskell](https://hackage.haskell.org/package/base-4.14.1.0/docs/Data-Either.html), [Rust](https://doc.rust-lang.org/std/result/), [Swift](https://www.swiftbysundell.com/articles/the-power-of-result-types-in-swift/), [C++](https://bell0bytes.eu/expected/) or [others](https://en.wikipedia.org/wiki/Result_type), have some kind of type that indicates one of two options - for example, success or failure.  Java has [`Optional`](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html), but that cannot contain an error value.

## Usage



## Installing

### Java 17+



### Java 15 or 16

This library relies on sealed interfaces, which are a preview feature in Java 15 and 16. You will need at least language level 15 to use this library, and at least 17 if you do not want to use preview features.

In Java 15 or 16, you will need to compile with `--enable-preview`. If you use Maven::

    <plugins>
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
    </plugins>








