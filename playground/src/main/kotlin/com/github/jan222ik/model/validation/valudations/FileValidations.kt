package com.github.jan222ik.model.validation.valudations

import arrow.core.invalidNel
import arrow.core.validNel
import com.github.jan222ik.model.validation.IValidationError
import com.github.jan222ik.model.validation.ValidationItem
import java.io.File

object FileValidations {
    val checkNotExists: ValidationItem<File, File> = ValidationItem {
        if (exists()) {
            FileValidationErrors.FileAlreadyExistError(absolutePath).invalidNel()
        } else {
            validNel()
        }
    }

    val checkIsEmpty: ValidationItem<File, File> = ValidationItem {
        if (exists()) {
            if (isDirectory && listFiles().isNotEmpty()) {
                FileValidationErrors.DirectoryNotEmpty().invalidNel()
            } else validNel()
        } else validNel()
    }

    val checkExists: ValidationItem<File, File> = ValidationItem {
        if (!exists()) {
            FileValidationErrors.FileDoesNotExistError(absolutePath).invalidNel()
        } else {
            validNel()
        }
    }

    val checkDirectory: ValidationItem<File, File> = ValidationItem {
        if (isDirectory) {
            FileValidationErrors.FileNotDirectory().invalidNel()
        } else {
            validNel()
        }
    }

    sealed class FileValidationErrors(override val msg: String) : IValidationError {
        class FileDoesNotExistError(path: String) : FileValidationErrors("No file exists at path: $path.")
        class FileAlreadyExistError(path: String) : FileValidationErrors("File already exists at path: $path.")
        class FileNotDirectory() : FileValidationErrors("The file is not a directory.")
        class DirectoryNotEmpty : FileValidationErrors("The directory already contains files")
    }
}