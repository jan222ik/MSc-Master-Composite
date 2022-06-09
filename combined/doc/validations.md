# Validation & Transformation Framework

Validations can be used to validate the user input
and Transformations can be used for transforming data types.

## Validation

Validation is mostly handled by the different states supporting validations.

A prominent example is the ValidatedTextState for TextField interactions.
```kotlin
class ValidatedTextState<R>(
    private val initial: String,
    private val transformation: ITransformation<String, R>,
    private val onValidValue: ((R, Boolean) -> Unit)? = null
)
```
1. It accepts the initial text for the input
> :warning: **It is assumed that the initial value is valid!!**
2. A transformation, more info below
3. Callback if a valid value was produced by the transformation and validation

## Transformation

Defines a Transformation between two types that may produce errors

```kotlin
interface ITransformation<T, R> {
    /**
     * Transformation between input type [T] and [R]
     * @param input to be transformed
     * @return [ValidatedNel] with errors of type [IValidationError] and valid value [R]
     */
    fun transform(input: T): ValidatedNel<IValidationError, R>

}
```

## Transformer

The Transformer is an abstraction level implementing utility functions for
ITransformations, it handles the execution of validations before and after the
transformation, as well as combining the result types.

```kotlin
/**
 * @param T type before Transformation
 * @param R type after Transformation
 * @param strategy defines the [FailStrategy] of the validations
 * @param beforeTransformValidations defines the validations to evaluate before the transformation, defaults null
 * @param afterTransformValidations defines the validations to evaluate after the transformation, defaults null
 */
abstract class Transformer<T : Any, R : Any>(
    private val strategy: FailStrategy,
    private val beforeTransformValidations: List<ValidationItem<T, T>>? = null,
    private val afterTransformValidations: List<ValidationItem<R, R>>? = null
) : ITransformation<T, R> 
```

Transformers are the recommended way to use transformations and validations if
applicable to the given types.

For common data type conversions transformers exist:

<b>1) No Transformation, just Validation: </b>

```kotlin
    class NonTransformer<T : Any>(/*...*/) : Transformer<T, T>(/*...*/)
```

2) String -> Double

```kotlin
    class ToDoubleTransformer(/*...*/) : Transformer<String, Double>(/*...*/)
```

3) String -> Int

```kotlin
    class ToIntTransformer(/*...*/) : Transformer<String, Int>(/*...*/)
```

### Strategy

FailStrategy offers two behaviours to follow in case of a failure collection process.

```kotlin
sealed class FailStrategy {
    /**
     * Indicates that the evaluation process should terminate instantly ([Fast]) and should not collect any other errors.
     */
    object Fast : FailStrategy()

    /**
     * Indicates that the evaluation process should continue ([Accumulation]) and collect more errors.
     */
    object Accumulation : FailStrategy()
}
```

Most of the default implementations will use the Accumulation strategy for errors

## Usage

The framework can be used as follows:

1. Create a transformation with validation

```kotlin
 val transformation = remember { NonTransformer(
    validations = listOf(Rules.StringBased.checkNotEmpty)
)}
```

2. Creation of a State
```kotlin
val textState = remember(initialValue, transformation) {
        ValidatedTextState(
            initial = initialValue,
            transformation = transformation,
            onValidValue = null
        )
    }
```

3. Use textState, below full example

```kotlin
@Composable
fun ExampleComponent(
    initialValue: String
) {
    val transformation = remember { NonTransformer(
        validations = listOf(Rules.StringBased.checkNotEmpty)
    )}
    val textState = remember(initialValue, transformation) {
        ValidatedTextState(
            initial = initialValue,
            transformation = transformation,
            onValidValue = null
        )
    }
    TextField(
        value = textState.tfv,
        onValueChange = textState::onValueChange,
        isError = textState.errors.isNotEmpty()
    )
    textState.errors.forEach {  /* it :ValidationError -> */
        Text(
            text = it.msg,
            color = MaterialTheme.colors.error
        )
    }
}
```

## Extension

### New Validation

To create a new validation the functional interface ValidationItem has to be implemented.
If a new error is needed the interface IValidationError has to be implemented as well.

Signatures:

```kotlin
fun interface ValidationItem<in T, out R> {
    fun T.validate(): ValidatedNel<IValidationError, R>
}

interface IValidationError {
    val msg: String
}
```
For more information for ValidatedNel look at the [arrow documentation page](./arrow.md)

Advanced Usage Example:
Create Error Class:

```kotlin
class NotInRangeValidationError(override val msg: String = "Value not in range.") : IValidationError
```

Create Validation:

```kotlin
fun validateValueInRange(lower: Double, upper: Double) = ValidationItem<Double, Double> {
    val range = lower.rangeTo(upper)
    return@ValidationItem if (range.contains(this)) this.validNel() else NotInRangeValidationError().invalidNel()
}
```
For more information for ".validNel()" and ".invalidNel()" look at the [arrow documentation page](./arrow.md)

Creating the ``ValidationItem`` inside the function allows to capture additional values like
the bounds in the closure. Thus, allowing easier reuse of the validation with different values.
