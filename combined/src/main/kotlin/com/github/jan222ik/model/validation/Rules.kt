package com.github.jan222ik.model.validation

import arrow.core.*
import arrow.core.computations.either


object Rules {

    private fun <T, R, R2> T.nestedImpl(
        primary: ValidationItem<T, R>,
        nestedStrategy: FailStrategy,
        vararg otherValidations: ValidationItem<R, R2>
    ): ValidatedNel<IValidationError, R> {
        val primaryValidated = with(primary) { validate() }
        if (primaryValidated.isValid) {
            primaryValidated.map {
                with(it) {
                    return runValidations(
                        strategy = nestedStrategy,
                        validations = otherValidations.toList(),
                        mapper = { this }
                    )
                }
            }
            // Should not happen
            return primaryValidated
        } else {
            return primaryValidated
        }
    }

    /**
     * Validates the primary item and uses their result as the input for the nested validations.
     *
     * @param T Input type of the first validation
     * @param R Output type of the first validation and input for nested validations
     * @param primary first validation
     * @param nestedValidations nested validations
     * @param nestedStrategy strategy [FailStrategy] to use for nested elements
     *
     * @return [ValidationItem] to be used in a validation tree
     */
    fun <T, R, R2> nested(
        primary: ValidationItem<T, R>,
        nestedStrategy: FailStrategy,
        vararg nestedValidations: ValidationItem<R, R2>
    ): ValidationItem<T, R> = ValidationItem {
        nestedImpl(
            primary = primary,
            nestedStrategy = nestedStrategy,
            otherValidations = nestedValidations
        )
    }

    object StringBased {
        val checkNotEmpty: ValidationItem<String, String> = ValidationItem {
            if (this.isEmpty()) {
                ValidationErrors.TextErrors.IsEmpty().invalidNel()
            } else {
                validNel()
            }
        }


        val checkMaxLength: ValidationItem<String, String> = ValidationItem {
            if (this.length > 10) {
                ValidationErrors.TextErrors.TooLong().invalidNel()
            } else {
                validNel()
            }
        }


        val checkForContainedWhiteSpace: ValidationItem<String, String> = ValidationItem {
            if (this.contains("\\s".toRegex())) {
                ValidationErrors.TextErrors.WhiteSpaceNotAllowed().invalidNel()
            } else {
                validNel()
            }
        }
    }

    object ValueBased {
        val checkToDouble: ValidationItem<String, Double> = ValidationItem {
            toDoubleOrNull()?.validNel() ?: ValidationErrors.NumericErrors.DoubleParseError().invalidNel()
        }

        fun checkBiggerThan(min: Double): ValidationItem<Double, Double> = ValidationItem {
            if (this > min) validNel() else ValidationErrors.NumericErrors.OutOfValueBounds(min = min).invalidNel()
        }
    }

    fun <T, R, R2> T.runValidations(
        strategy: FailStrategy,
        validations: List<ValidationItem<T, R>>,
        mapper: (List<R>) -> R2
    ): ValidatedNel<IValidationError, R2> {
        return when (strategy) {
            FailStrategy.Accumulation -> {
                validations.traverseValidated { with(it) { validate() } }.map { mapper.invoke(it) }
            }
            FailStrategy.Fast -> {
                either.eager<Nel<IValidationError>, R2> {
                    val l = validations.map { v -> with(v) { validate().bind() } }.toList()
                    mapper.invoke(l)
                }.toValidated()

            }
        }
    }
}