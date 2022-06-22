package com.github.jan222ik.model.command.commands

import androidx.compose.ui.geometry.Offset
import com.github.jan222ik.model.command.ICommand
import com.github.jan222ik.ui.adjusted.CompoundBoundingShape
import com.github.jan222ik.ui.adjusted.arrow.Arrow
import com.github.jan222ik.ui.feature.main.footer.progress.JobHandler

class UpdateArrowPathCommand(
    val target: Arrow,
    val before: Pair<List<Offset>, CompoundBoundingShape>,
    val after: Pair<List<Offset>, CompoundBoundingShape>
) : ICommand {
    override fun isActive(): Boolean = true
    override suspend fun execute(handler: JobHandler) {
        target.applyPath(after)
    }

    override fun canUndo(): Boolean = true

    override suspend fun undo() {
        target.applyPath(before)
    }

}