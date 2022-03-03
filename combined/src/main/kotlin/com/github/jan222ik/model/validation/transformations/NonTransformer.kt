package com.github.jan222ik.model.validation.transformations

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import arrow.core.validNel
import com.github.jan222ik.model.validation.FailStrategy
import com.github.jan222ik.model.validation.ValidationItem

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
class NonTransformer<T: Any>(
    strategy: FailStrategy = FailStrategy.Accumulation,
    validations: List<ValidationItem<T, T>> = emptyList(),
) : Transformer<T, T>(
    strategy = strategy,
    afterTransformValidations = validations,
) {
    override fun transformValue(input: T) = input.validNel()
}