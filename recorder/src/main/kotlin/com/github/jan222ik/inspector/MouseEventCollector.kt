package com.github.jan222ik.inspector

import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.awt.event.MouseEvent
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicInteger

class MouseEventCollector(
    private val workbook: XSSFWorkbook
) {
    private val idx = AtomicInteger(0)
    private val header = listOf(
        "timestamp" to CellType.NUMERIC,
        "pos-x" to CellType.NUMERIC,
        "pos-y" to CellType.NUMERIC,
        "button" to CellType.NUMERIC,
        "CTRL" to CellType.BOOLEAN,
        "SHIFT" to CellType.BOOLEAN,
        "ALT" to CellType.BOOLEAN,
        "type" to CellType.STRING
    )
    private val sheet = workbook.createSheet("Mouse Movements").also {
        it.createRow(idx.getAndIncrement()).apply {
            header.forEachIndexed { index, (name, type) ->
                createCell(index, type).setCellValue(name)
            }
        }
    }

    fun addEvent(evt: MouseEvent, type: String) {
        sheet.createRow(idx.getAndIncrement()).apply {
            listOf(
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                evt.x,
                evt.y,
                evt.button,
                evt.isControlDown,
                evt.isShiftDown,
                evt.isAltDown || evt.isAltGraphDown,
                type
            ).forEachIndexed { index, value ->
                createCell(index, header[index].second).setCellValue(value.toString())
            }
        }
    }
}