package com.github.jan222ik.ui.feature.main.tree

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MouseClickScope
import mu.KLogging
import org.eclipse.emf.common.notify.Notifier

@ExperimentalFoundationApi
sealed class ModelTreeItem(
    override val level: Int,
    override val displayName: String,
    override val canExpand: Boolean
) : TreeDisplayableItem(level = level) {

    companion object : KLogging() {
        fun parseItem(
            level: Int,
            element: Notifier
        ): TreeDisplayableItem? {
            return when (element) {
                is org.eclipse.uml2.uml.Package -> {
                    logger.debug { "Package" }
                    PackageItem(
                        level = level,
                        displayName = "Package" + element.name,
                        canExpand = element.ownedElements.isNotEmpty(),
                        umlPackage = element
                    )
                }
                is org.eclipse.uml2.uml.Class -> {
                    logger.debug { "Class" }
                    ClassItem(
                        level = level,
                        displayName = "Class:" + element.name,
                        canExpand = element.ownedElements.isNotEmpty(),
                        umlClass = element
                    )
                }
                is org.eclipse.uml2.uml.PackageImport -> {
                    logger.debug { "PackageImport" }
                    ImportItem(
                        level = level,
                        displayName = "Import:" + element.importingNamespace.name,
                        canExpand = element.ownedElements.isNotEmpty(),
                        umlImport = element
                    )
                }
                is org.eclipse.uml2.uml.Property -> {
                    logger.debug { "Property" }
                    PropertyItem(
                        level = level,
                        displayName = "Property:" + element.name,
                        canExpand = element.ownedElements.isNotEmpty(),
                        umlProperty = element
                    )
                }
                is org.eclipse.uml2.uml.ValueSpecification -> {
                    logger.debug { "ValueSpec" }
                    ValueItem(
                        level = level,
                        displayName = "Val:" + element.name,
                        canExpand = element.ownedElements.isNotEmpty(),
                        umlValue = element
                    )
                }
                else -> {
                    logger.debug { "Item can not be converted to a element in the tree. ${element.javaClass}" }
                    null
                }
            }
        }
    }

    abstract fun getElement() : org.eclipse.uml2.uml.Element

    override val onPrimaryAction: (MouseClickScope.(idx: Int) -> Unit)? = null

    override val onDoublePrimaryAction: MouseClickScope.() -> Unit
        get() = {
            if (canExpand) {
                if (children.isNotEmpty()) {
                    children = emptyList()
                } else {
                    FileTree.eContentsToModelTreeItem(getElement(), this@ModelTreeItem)
                }
            }
        }

    override val onSecondaryAction: MouseClickScope.() -> Unit
        get() = {
            logger.debug { "TODO: Secondary Action" }
        }

    class PackageItem(
        level: Int,
        displayName: String,
        canExpand: Boolean,
        val umlPackage: org.eclipse.uml2.uml.Package
    ) : ModelTreeItem(
        level = level,
        displayName = displayName,
        canExpand = canExpand
    ) {
        override fun getElement() = umlPackage

    }

    class ClassItem(
        level: Int,
        displayName: String,
        canExpand: Boolean,
        val umlClass: org.eclipse.uml2.uml.Class
    ) : ModelTreeItem(
        level = level,
        displayName = displayName,
        canExpand = canExpand
    ) {
        override fun getElement() = umlClass

    }

    class ImportItem(
        level: Int,
        displayName: String,
        canExpand: Boolean,
        val umlImport: org.eclipse.uml2.uml.PackageImport
    ) : ModelTreeItem(
        level = level,
        displayName = displayName,
        canExpand = canExpand
    ) {
        override fun getElement() = umlImport

    }

    class PropertyItem(
        level: Int,
        displayName: String,
        canExpand: Boolean,
        val umlProperty: org.eclipse.uml2.uml.Property
    ) : ModelTreeItem(
        level = level,
        displayName = displayName,
        canExpand = canExpand
    ) {
        override fun getElement() = umlProperty

    }

    class ValueItem(
        level: Int,
        displayName: String,
        canExpand: Boolean,
        val umlValue: org.eclipse.uml2.uml.ValueSpecification
    ) : ModelTreeItem(
        level = level,
        displayName = displayName,
        canExpand = canExpand
    ) {
        override fun getElement() = umlValue

    }

}

