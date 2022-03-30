package com.github.jan222ik.model.validation.valudations

import arrow.core.invalidNel
import arrow.core.validNel
import com.github.jan222ik.model.validation.ValidationErrors
import com.github.jan222ik.model.validation.ValidationItem

object ListValidations {

    fun <T> inCollection(list: List<T>) = ValidationItem<T, T> {
        if (list.contains(this)) {
            this.validNel()
        } else {
            ValidationErrors.NotAOption(text = this.toString()).invalidNel()
        }
    }
}