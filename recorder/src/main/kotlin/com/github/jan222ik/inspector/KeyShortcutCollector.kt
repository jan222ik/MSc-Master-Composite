package com.github.jan222ik.inspector

import androidx.compose.ui.input.key.*
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicInteger

class KeyShortcutCollector(workbook: XSSFWorkbook) {
    private val idx = AtomicInteger(0)
    private val header = listOf(
        "timestamp" to CellType.NUMERIC,
        "keyText" to CellType.STRING,
        "keyCode" to CellType.NUMERIC,
        "CTRL" to CellType.BOOLEAN,
        "SHIFT" to CellType.BOOLEAN,
        "ALT" to CellType.BOOLEAN,
    )
    private val sheet = workbook.createSheet("Pressed Shortcuts").also {
        it.createRow(idx.getAndIncrement()).apply {
            header.forEachIndexed { index, (name, type) ->
                createCell(index, type).setCellValue(name)
            }
        }

    }

    fun recordAction(key: Key, isCtrlPressed: Boolean, isShiftPressed: Boolean, isAltPressed: Boolean) {
        sheet.createRow(idx.getAndIncrement()).apply {
            listOf(
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                java.awt.event.KeyEvent.getKeyText(key.nativeKeyCode),
                key.keyCode,
                isCtrlPressed,
                isShiftPressed,
                isAltPressed
            ).forEachIndexed { index, any ->
                createCell(index, header[index].second).setCellValue(any.toString())
            }
        }
    }
}
