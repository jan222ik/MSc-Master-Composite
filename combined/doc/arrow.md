# Arrow
The library ArrowKT is used in this project for validation purposes using
different error handling patterns.
Documentation: https://arrow-kt.io/docs/core/

## Either
The functional programming construct of Either is used for actions that can fail.
Doc: https://arrow-kt.io/docs/apidocs/arrow-core/arrow.core/-either/

Either has a left side for errors and a right side for valid values.
Example below shows how an Either can be created for both instances (error or value).
Example:
```kotlin
val right: Either<String, Int> = Either.Right(5)
val left: Either<String, Int> = Either.Left("Something went wrong")
```

Either is mostly used for a single fail value, for multiple Validated can be used.

## Validated

Validated is similar to either, its left side is called Invalid and the right side is called Valid.

```kotlin
val valid: Validated<String, Int> = Validated.Valid(5)
val inValid: Validated<String, Int> = Validated.Invalid("Something went wrong")
```

For this action extension methods exist
```kotlin
val valid: Validated<String, Int> = 5.valid()
val inValid: Validated<String, Int> = "Something went wrong".invalid()
```

As mentioned Validated offers better support for multiple errors with the usage of NotEmptyLists.

The special type of Validated is called ValidatedNel

## ValidatedNel

ValidatedNel stands for the full type of ``Validated<NotEmptyList<E>, T>``
where E is the invalid error type and T the valid value type.

Also, extention methods for creating a ValidatedNel exist.
```kotlin
val valid: ValidatedNel<String, Int> = 5.validNel()
val inValid: ValidatedNel<String, Int> = "Something went wrong".invalidNel()
```

ValidatedNel can be combined with the zip method.
```kotlin
    val a: ValidatedNel<String, Int> = 5.validNel()
val b: ValidatedNel<String, Int> = "Something went wrong".invalidNel()
val c: ValidatedNel<String, Int> = "Something went wrong".invalidNel()

val zip: ValidatedNel<String, Int> = a.zip(b, c) { A, B, C -> /* Handle multiple valid values, reduce to one */ A }
```