package com.github.jan222ik.ui.adjusted.helper

sealed class AlignmentMoveDirection {
    sealed class Horizontal : AlignmentMoveDirection() {
        object None : Horizontal()
        object West : Horizontal()
        object East : Horizontal()

        override fun toString(): String {
            return when(this) {
                East -> "East"
                None -> "None"
                West -> "West"
            }
        }
    }

    sealed class Vertical : AlignmentMoveDirection() {
        object None : Vertical()
        object North : Vertical()
        object South : Vertical()

        override fun toString(): String {
            return when(this) {
                None -> "None"
                North -> "North"
                South -> "South"
            }
        }
    }

    data class Combined(
        val vertical: Vertical = Vertical.None,
        val horizontal: Horizontal = Horizontal.None
    ) : AlignmentMoveDirection() {
        companion object {
            val NONE = Combined()
        }

        override fun toString(): String {
            return if (this == NONE) {
                "NONE"
            } else {
                "(v: $vertical, h:$horizontal)"
            }
        }
    }
}