package com.github.jan222ik.ui.feature.wizard

import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import com.github.jan222ik.model.validation.ValidatedTextState
import com.github.jan222ik.model.validation.transformations.NonTransformer
import com.github.jan222ik.model.validation.transformations.ToFileTransformer
import com.github.jan222ik.model.validation.valudations.FileValidations
import com.github.jan222ik.model.validation.valudations.Rules
import java.io.File

class CreateProjectWizard(
    private val onCreationFinished: (Project) -> Unit,
    private val onDismissRequest: () -> Unit
) {

    private val projectLocationState =
        ValidatedTextState(
            initial = File("").absolutePath,
            transformation = ToFileTransformer(
                beforeValidations = listOf(
                    Rules.StringBased.checkNotEmpty
                ),
                afterValidations = listOf(
                    FileValidations.checkIsEmpty
                )
            )
        )

    private val projectNameState =
        ValidatedTextState(
            initial = "",
            transformation = NonTransformer(
                validations = listOf(
                    Rules.StringBased.checkNotEmpty,
                    Rules.StringBased.checkForContainedWhiteSpace
                )
            )
        )

    private fun createProject(file: File, name: String) {
        onCreationFinished(Project.create(root = file, name = name))
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun render(visible: Boolean) {
        if (visible) {
            AlertDialog(
                onDismissRequest = onDismissRequest,
                confirmButton = {
                    Button(
                        enabled = projectLocationState.errors.isEmpty() && projectNameState.errors.isEmpty(),
                        onClick = {
                            projectLocationState.transformResult?.let {
                                createProject(
                                    file = it,
                                    name = projectNameState.tfv.text
                                )
                            }
                        }
                    ) {
                        Text("Continue")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.error)
                    ) {
                        Text("Cancel")
                    }
                },
                title = {
                    Text("Create Project")
                },
                text = {
                    Column {
                        TextField(
                            value = projectLocationState.tfv,
                            onValueChange = projectLocationState::onValueChange
                        )
                        projectLocationState.errors.forEach {
                            Text(text = it.msg)
                        }
                        TextField(
                            value = projectNameState.tfv,
                            onValueChange = projectNameState::onValueChange
                        )
                        projectNameState.errors.forEach {
                            Text(text = it.msg)
                        }
                    }

                }
            )
        }
    }
}