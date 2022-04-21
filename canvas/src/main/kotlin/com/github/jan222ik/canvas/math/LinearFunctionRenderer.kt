package com.github.jan222ik.canvas.math

import com.github.jan222ik.canvas.axis.drawer.simpleAxisLineDrawer
import com.github.jan222ik.canvas.line.ILineDrawer

fun linearFunctionRenderer(
    lineDrawer: ILineDrawer = simpleAxisLineDrawer(),
    linearFunctionPointProvider: ILinearFunctionPointProvider
) = ILinearFunctionRenderer {
    with(linearFunctionPointProvider) {
        val (start, end) = provide()
        with(lineDrawer) {
            draw(start, end)
        }
    }
}
