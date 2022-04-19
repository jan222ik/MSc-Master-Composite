package com.github.jan222ik.inspector

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File

class CompoundCollector(
    private val outdir: File,
    private val name: String = "collected-data.xlsx"
) {
    private val workbook = XSSFWorkbook()
    val mouseMovement = MouseEventCollector(workbook)
    val keyPressCollector = KeyPressCollector(workbook)
    val keyShortCutCollector = KeyShortcutCollector(workbook)

    fun save() {
        val file = File(outdir, name)
        file.createNewFile()
        val outputStream = file.outputStream()
        workbook.write(outputStream)
    }
}