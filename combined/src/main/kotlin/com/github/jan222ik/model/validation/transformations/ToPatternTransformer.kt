package com.github.jan222ik.model.validation.transformations

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import arrow.core.ValidatedNel
import arrow.core.invalidNel
import arrow.core.validNel
import com.github.jan222ik.model.validation.FailStrategy
import com.github.jan222ik.model.validation.IValidationError
import com.github.jan222ik.model.validation.ValidationErrors
import com.github.jan222ik.model.validation.ValidationItem
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException


@Stable
@Immutable
/**
 * The [ToPatternTransformer] is used to transform a [String] to a [Pattern]
 * and perform validations before and after the transformation.
 *
 * @param strategy defines the [FailStrategy] of the validations, defaults [FailStrategy.Accumulation]
 * @param beforeValidations defines the validations to evaluate before transformation
 * @param afterValidations defines the validations to evaluate after transformation,
 *                         will not run if transformation unsuccessful.
 *
 * @see [Transformer] for abstract type and [ITransformation] for interface
 */
class ToPatternTransformer(
    strategy: FailStrategy = FailStrategy.Accumulation,
    beforeValidations: List<ValidationItem<String, String>>? = null,
    afterValidations: List<ValidationItem<Pattern, Pattern>>? = null
) : Transformer<String, Pattern>(
    strategy = strategy, beforeTransformValidations = beforeValidations, afterTransformValidations = afterValidations
) {
    override fun transformValue(input: String): ValidatedNel<IValidationError, Pattern> {
        return try {
            input.toPattern().validNel()
        } catch (e: PatternSyntaxException){
            ValidationErrors.TextErrors.PatternSyntaxException(e.description).invalidNel()
        }
    }
}