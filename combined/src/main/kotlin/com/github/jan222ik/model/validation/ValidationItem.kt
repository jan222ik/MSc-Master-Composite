package com.github.jan222ik.model.validation

import arrow.core.ValidatedNel

fun interface ValidationItem<in T, out R> {
    fun T.validate(): ValidatedNel<IValidationError, R>
}