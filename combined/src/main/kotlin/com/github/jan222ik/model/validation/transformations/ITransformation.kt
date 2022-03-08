package com.github.jan222ik.model.validation.transformations

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import arrow.core.ValidatedNel
import com.github.jan222ik.model.validation.IValidationError

@Stable
@Immutable
/**
 * Defines a Transformation between two types that may produce errors.
 *
 * @param T input value type
 * @param R output value type
 */
interface ITransformation<T, R> {
    /**
     * Transformation between input type [T] and [R]
     * @param input to be transformed
     * @return [ValidatedNel] with errors of type [IValidationError] and valid value [R]
     */
    fun transform(input: T): ValidatedNel<IValidationError, R>

}