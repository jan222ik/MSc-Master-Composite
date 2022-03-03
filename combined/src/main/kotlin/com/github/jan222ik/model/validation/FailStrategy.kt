package com.github.jan222ik.model.validation

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
@Immutable
/**
 * [FailStrategy] offers two behaviours to follow in case of a failure collection process.
 */
sealed class FailStrategy {
    @Stable
    @Immutable
    /**
     * Indicates that the evaluation process should terminate instantly ([Fast]) and should not collect any other errors.
     */
    object Fast : FailStrategy()

    @Stable
    @Immutable
    /**
     * Indicates that the evaluation process should continue ([Accumulation]) and collect more errors.
     */
    object Accumulation : FailStrategy()
}