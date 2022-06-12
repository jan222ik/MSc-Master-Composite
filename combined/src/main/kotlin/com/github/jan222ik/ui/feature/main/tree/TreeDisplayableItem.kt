package com.github.jan222ik.ui.feature.main.tree

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MouseClickScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.github.jan222ik.model.TMM
import com.github.jan222ik.model.TMMPath

@ExperimentalFoundationApi
abstract class TreeDisplayableItem(
    open val level: Int
) {
    abstract val icon: @Composable ((modifier: Modifier) -> Unit)?
    abstract val onPrimaryAction: (MouseClickScope.(idx: Int) -> Unit)?
    abstract val onDoublePrimaryAction: MouseClickScope.() -> Unit
    abstract val onSecondaryAction: MouseClickScope.(LazyListState, Int, ITreeContextFor) -> Unit
    abstract val displayName: @Composable () -> String
    abstract val canExpand: Boolean

    abstract val children: SnapshotStateList<TreeDisplayableItem>
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TreeDisplayableItem) return false

        if (level != other.level) return false
        if (icon != other.icon) return false
        if (onPrimaryAction != other.onPrimaryAction) return false
        if (onDoublePrimaryAction != other.onDoublePrimaryAction) return false
        if (onSecondaryAction != other.onSecondaryAction) return false
        if (displayName != other.displayName) return false
        if (canExpand != other.canExpand) return false
        if (children != other.children) return false

        return true
    }

    override fun hashCode(): Int {
        var result = level
        result = 31 * result + (icon?.hashCode() ?: 0)
        result = 31 * result + (onPrimaryAction?.hashCode() ?: 0)
        result = 31 * result + onDoublePrimaryAction.hashCode()
        result = 31 * result + onSecondaryAction.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + canExpand.hashCode()
        result = 31 * result + children.hashCode()
        return result
    }

    fun expandAll() {
        if (canExpand) {
            if (children.isEmpty()) {
                onDoublePrimaryAction.invoke(com.github.jan222ik.ui.feature.main.keyevent.EmptyClickContext)
            }
            children.onEach { it.expandAll() }
        }
    }

    fun expandToTarget(tmmPath: TMMPath<*>, idx: Int) {
        val tmm = this.getTMM()
        if (tmm != tmmPath.target) {
            if (tmm is TMM.IHasChildren<*>) {
                val find = tmm.children.find { it == tmmPath.nodes[idx.inc()] }
                if (find != null) {
                    if (children.isEmpty()) {
                        onDoublePrimaryAction.invoke(com.github.jan222ik.ui.feature.main.keyevent.EmptyClickContext)
                    }
                    children.find { it.getTMM() == find }?.expandToTarget(tmmPath, idx.inc())
                }
            }
        }
    }

    abstract fun getTMM() : TMM


}