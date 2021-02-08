
Result type (Java 15+)
===============================

With sealed interfaces in Java 15 (preview feature), it finally has decent support for sum types - algebraic types that can have one out of multiple values.

You can think of it as an enhanced [`enum`](https://docs.oracle.com/en/java/javase/13/language/switch-expressions.html), where each variant can be a different type. Or as an [`Optional`](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html) that can contain an error instead of being empty.

Many languages that support sum types, like [Kotlin](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-result/), [Haskell](https://hackage.haskell.org/package/base-4.14.1.0/docs/Data-Either.html), [Rust](https://doc.rust-lang.org/std/result/), [Swift](https://www.swiftbysundell.com/articles/the-power-of-result-types-in-swift/), [C++](https://bell0bytes.eu/expected/) or [others](https://en.wikipedia.org/wiki/Result_type), have some kind of type that indicates one of two options - for example, success or failure.

How to use
-------------------------------








