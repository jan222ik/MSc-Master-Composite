package com.github.jan222ik.model.validation.transformations

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import arrow.core.*
import com.github.jan222ik.model.validation.FailStrategy
import com.github.jan222ik.model.validation.IValidationError
import com.github.jan222ik.model.validation.ValidatedTextState
import com.github.jan222ik.model.validation.ValidationItem
import com.github.jan222ik.model.validation.valudations.Rules

@Stable
@Immutable
/**
 * The [NonTransformer] can be used for just evaluating validations.
 * @param T defines the input type of the validations
 * @param strategy defines the [FailStrategy] of the validations, defaults [FailStrategy.Accumulation]
 * @param validations defines the validations to evaluate
 *
 * @see [Transformer] for abstract type and [ITransformation] for interface
 */
class NonTransformer<T : Any>(
    strategy: FailStrategy = FailStrategy.Accumulation,
    validations: List<ValidationItem<T, T>> = emptyList(),
) : Transformer<T, T>(
    strategy = strategy,
    afterTransformValidations = validations,
) {
    override fun transformValue(input: T) = input.validNel()
}

fun main() {
    class NotInRangeValidationError(override val msg: String = "Value not in range.") : IValidationError

    fun validateRange(lower: Double, upper: Double) = ValidationItem<Double, Double> {
        val range = lower.rangeTo(upper)
        return@ValidationItem if (range.contains(this)) this.validNel() else NotInRangeValidationError().invalidNel()
    }

    @Composable
    fun ExampleComponent(
        initialValue: String
    ) {
        val transformation = NonTransformer(
            validations = listOf(Rules.StringBased.checkNotEmpty)
        )
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

    val a: ValidatedNel<String, Int> = 5.validNel()
    val b: ValidatedNel<String, Int> = "Something went wrong".invalidNel()
    val c: ValidatedNel<String, Int> = "Something went wrong".invalidNel()

    val zip: ValidatedNel<String, Int> = a.zip(b, c) { A, B, C ->
        /* Handle multiple valid values, reduce to one */ A
    }

}