package com.github.jan222ik.ui.feature.main.menu_tool_bar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.github.jan222ik.ui.adjusted.DebugCanvas
import com.github.jan222ik.ui.components.breadcrumbs.BreadCrumbItem
import com.github.jan222ik.ui.components.breadcrumbs.BreadcrumbsRow
import com.github.jan222ik.ui.feature.main.diagram.EditorManager
import com.github.jan222ik.ui.feature.main.footer.progress.BackgroundJobComponent
import com.github.jan222ik.ui.feature.main.footer.progress.IProgressMonitor
import com.github.jan222ik.ui.feature.main.footer.progress.JobHandler
import com.github.jan222ik.ui.feature.main.footer.progress.percentageFloat
import com.github.jan222ik.ui.feature.main.tree.FileTree
import com.github.jan222ik.ui.value.EditorColors
import com.github.jan222ik.ui.value.Space
import com.github.jan222ik.util.HorizontalDivider
import org.eclipse.uml2.uml.Element
import org.eclipse.uml2.uml.NamedElement

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ToolBarComponent(modifier: Modifier, jobHandler: JobHandler) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(color = EditorColors.backgroundGray),
        ) {
            val crumbsRoot = FileTree.loadedClients.value
                .map { it.value }
                .map { projectData ->
                    fun Element.toBreadCrumbItem(): BreadCrumbItem? {
                        if (this is NamedElement) {
                            if (this is org.eclipse.uml2.uml.Package) {
                                return BreadCrumbItem.Package(
                                    name = this.name,
                                    children = this.ownedElements
                                        .mapNotNull { it.toBreadCrumbItem() } + projectData.diagrams
                                        .filter { it.location == this.qualifiedName }
                                        .map { BreadCrumbItem.Diagram(name = it.name, type = it.diagramType) })
                            }
                        }
                        return null
                    }
                    if (projectData.diagrams.isNotEmpty()) {
                        projectData.ecore.model.toBreadCrumbItem()
                    } else null
                }.filterNotNull()
            val name = FileTree.loadedClients.value.values.firstOrNull()?.ecore?.model?.name ?: ""
            val path =
                EditorManager.activeEditorTab.value?.observableDiagram?.let { "${it.location}::${it.diagramName.tfv.text}" }
                    ?.split("::")?.toTypedArray() ?: arrayOf(name)
            BreadcrumbsRow(
                modifier = Modifier.align(Alignment.CenterStart).padding(start = Space.dp16),
                activePath = path,
                root = BreadCrumbItem.Package(
                    "root", children = crumbsRoot
                )
            )
            Row(modifier = Modifier.align(Alignment.CenterEnd).width(IntrinsicSize.Min).padding(end = Space.dp16)) {
                Box {
                    val component = remember(jobHandler) { BackgroundJobComponent(jobHandler) }
                    component.render()
                }
                Icon(
                    modifier = modifier.size(Space.dp16).clickable { DebugCanvas.debugCanvasVisible.value = true },
                    imageVector = Icons.Filled.Android,
                    contentDescription = null
                )
            }
        }
        val minBy = jobHandler.jobs
            .associateWith { it.progressTicksState.value }
            .filter { it.value != IProgressMonitor.UNKNOWN_PROGRESS_ADVANCE && it.value > 0 }
            .minByOrNull { it.value }

        if (minBy != null) {
            val anim = animateFloatAsState(minBy.key.progressTicksState.percentageFloat())
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                progress = anim.value,
                backgroundColor = EditorColors.dividerGray
            )
        } else {
            HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = EditorColors.dividerGray)
        }
    }
}