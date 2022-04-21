package com.github.jan222ik.canvas.math

import androidx.compose.ui.geometry.Offset

fun linearFunctionPointProvider(
    fx: (x: Float) -> Float
) = ILinearFunctionPointProvider {
    val start = context.viewport.minX.let{
        Offset(
            x = with(context) { it.toRendererX() },
            y = with(context) { it.let(fx).toRendererY() }
        )
    }
    val end = context.viewport.maxX.let{
        Offset(
            x = with(context) { it.toRendererX() },
            y = with(context) { it.let(fx).toRendererY() }
        )
    }
    return@ILinearFunctionPointProvider LinearFunctionResult(start, end)
}
