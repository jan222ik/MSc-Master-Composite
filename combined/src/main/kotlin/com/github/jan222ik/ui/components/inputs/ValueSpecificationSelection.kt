package com.github.jan222ik.ui.components.inputs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.github.jan222ik.ui.components.tooltips.TitleWithTooltip
import com.github.jan222ik.ui.value.descriptions.IPropertyViewElement

@ExperimentalFoundationApi
@Composable
fun ValueSpecificationSelection(
    propViewElement: IPropertyViewElement?
) {
    Column {
        propViewElement?.let { TitleWithTooltip(it) }
        ValueSpecificationWithValue(type = ValueSpecificationType.LiteralBoolean, true)
        ValueSpecificationWithValue(type = ValueSpecificationType.LiteralInteger, 23)
        ValueSpecificationWithValue(type = ValueSpecificationType.LiteralReal, 2.3)
        ValueSpecificationWithValue(type = ValueSpecificationType.LiteralString, "test")
        ValueSpecificationWithValue(type = ValueSpecificationType.LiteralNull, null)
    }
}

