package com.github.jan222ik.model.validation.transformations

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import arrow.core.ValidatedNel
import arrow.core.getOrElse
import arrow.core.zip
import com.github.jan222ik.model.validation.FailStrategy
import com.github.jan222ik.model.validation.IValidationError
import com.github.jan222ik.model.validation.Rules.runValidations
import com.github.jan222ik.model.validation.ValidationItem

@Stable
@Immutable
/**
 * The [Transformer] is an abstraction level implementing utility functions for
 * [ITransformation]s, it handles the execution of validations before and after the
 * transformation, as well as combining the result types.
 *
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
) : ITransformation<T, R> {

    /**
     * Abstract function that produces a transformed value [R] for an input [T].
     * As the Transformation may produce errors (of type [IValidationError]) the return value wrapped in a [ValidatedNel]
     *
     * @param input input to be transformed
     * @return [ValidatedNel] with error type [IValidationError] and value type [R]
     */
    abstract fun transformValue(input: T): ValidatedNel<IValidationError, R>

    private fun validateBefore(input: T): ValidatedNel<IValidationError, T>? {
        return beforeTransformValidations?.let {
            with(input) {
                runValidations(
                    strategy = strategy,
                    validations = beforeTransformValidations,
                    mapper = { this }
                )
            }
        }
    }

    private fun validateAfter(input: R): ValidatedNel<IValidationError, R>? {
        return afterTransformValidations?.let {
            with(input) {
                runValidations(
                    strategy = strategy,
                    validations = afterTransformValidations,
                    mapper = { this }
                )
            }
        }
    }

    override fun transform(input: T): ValidatedNel<IValidationError, R> {
        val prior = validateBefore(input)
        val transformedValue = transformValue(input)
        val post = transformedValue.getOrElse { null }?.let { validateAfter(it) }
        val tmp = prior?.zip(transformedValue) { _, it -> it } ?: transformedValue
        return post?.let { tmp.zip(it) { f, _ -> f } } ?: tmp
    }


}