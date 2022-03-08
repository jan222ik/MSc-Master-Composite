package com.github.jan222ik.model.validation.transformations

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import arrow.core.ValidatedNel
import arrow.core.validNel
import com.github.jan222ik.model.validation.FailStrategy
import com.github.jan222ik.model.validation.IValidationError
import com.github.jan222ik.model.validation.ValidationItem
import java.io.File


@Stable
@Immutable
/**
 * The [ToFileTransformer] is used to transform a [String] to a [java.io.File]
 * and perform validations before and after the transformation.
 *
 * @param strategy defines the [FailStrategy] of the validations, defaults [FailStrategy.Accumulation]
 * @param beforeValidations defines the validations to evaluate before transformation
 * @param afterValidations defines the validations to evaluate after transformation,
 *                         will not run if transformation unsuccessful.
 *
 * @see [Transformer] for abstract type and [ITransformation] for interface
 */
class ToFileTransformer(
    strategy: FailStrategy = FailStrategy.Accumulation,
    beforeValidations: List<ValidationItem<String, String>>? = null,
    afterValidations: List<ValidationItem<File, File>>? = null
) : Transformer<String, File>(
    strategy = strategy, beforeTransformValidations = beforeValidations, afterTransformValidations = afterValidations
) {
    override fun transformValue(input: String): ValidatedNel<IValidationError, File> {
        return File(input).validNel()
    }
}