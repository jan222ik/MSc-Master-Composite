package com.github.jan222ik.model.validation

sealed class ValidationErrors(override val msg: String) : IValidationError {
    sealed class TextErrors(msg: String) : ValidationErrors(msg = msg) {
        class IsEmpty : TextErrors("The text may not be empty")

        class TooLong : TextErrors("The text is too long.")

        class WhiteSpaceNotAllowed : TextErrors("White Space is not allowed.")
    }

    sealed class NumericErrors(msg: String) : ValidationErrors(msg = msg) {
        class DoubleParseError : NumericErrors("Not a number: Please provide a real number")
        class OutOfValueBounds(min: Double) : NumericErrors("The value is out of range. It should be larger than $min")
    }

    class NotAOption(text: String) : ValidationErrors(msg = "'$text' is not a valid option")
}