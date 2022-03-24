package com.github.jan222ik.model.validation

/**
 * [IValidationError] describes the shape of a validation error.
 * Each validation error has to provide a message.
 */
interface IValidationError {
    val msg: String
}