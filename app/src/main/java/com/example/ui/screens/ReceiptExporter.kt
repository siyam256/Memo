package com.example.ui.screens

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.example.data.ReceiptEntity
import com.example.data.ReceiptItem
import com.example.data.SignatureSerializer
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

object ReceiptExporter {

    private fun generateQrCodeBitmap(text: String, sizePixels: Int): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, sizePixels, sizePixels)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            bmp
        } catch (e: Exception) {
            null
        }
    }

    // A unified drawing function which outputs to an arbitrary Android Canvas.
    // Handles scaling, layout sizing, and typography.
    private fun drawReceiptContent(
        canvas: Canvas,
        width: Float,
        receipt: ReceiptEntity
    ): Float {
        val paint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.BLACK
        }

        val padding = 30f
        var y = 40f

        // Draw background cream paper color
        val bgPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#FFFFF2")
        }
        canvas.drawRect(0f, 0f, width, canvas.height.toFloat(), bgPaint)

        // Decorative borders
        val borderPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#E2E8F0")
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
        canvas.drawRect(8f, 8f, width - 8f, canvas.height.toFloat() - 8f, borderPaint)

        // Draw a top accent line
        val accentPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#6366F1")
        }
        canvas.drawRect(8f, 8f, width - 8f, 22f, accentPaint)
        y += 15f

        // Draw Company Name
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(receipt.companyName.uppercase(), width / 2, y + 25f, paint)
        y += 40f

        // Address
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        canvas.drawText(receipt.companyAddress.uppercase(), width / 2, y + 15f, paint)
        y += 25f

        // Contact
        canvas.drawText("CONTACT: ${receipt.companyPhone}", width / 2, y + 15f, paint)
        y += 35f

        // Divider
        paint.textSize = 14f
        canvas.drawText("=================================", width / 2, y + 15f, paint)
        y += 30f

        // Meta (No, Date)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("NO: ${receipt.receiptNo}", padding, y + 15f, paint)
        
        val dateStr = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(Date(receipt.purchasedDate))
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("DATE: ${dateStr.uppercase()}", width - padding, y + 15f, paint)
        y += 30f

        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("---------------------------------", width / 2, y + 15f, paint)
        y += 25f

        // Student Info
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("STUDENT: ${receipt.studentName.uppercase()}", padding, y + 15f, paint)
        y += 25f
        canvas.drawText("COURSE : ${receipt.productName.uppercase()}", padding, y + 15f, paint)
        y += 25f
        canvas.drawText("CAT    : ${receipt.category.uppercase()}", padding, y + 15f, paint)
        y += 25f

        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("---------------------------------", width / 2, y + 15f, paint)
        y += 25f

        // Headers
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("ITEMS / DESCRIPTION", padding, y + 15f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("PRICE", width - padding, y + 15f, paint)
        y += 25f

        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        canvas.drawText("---------------------------------", width / 2, y + 15f, paint)
        y += 20f

        // Items
        val items = ReceiptItem.deserializeList(receipt.itemsJson)
        items.forEach { item ->
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(item.name.uppercase(), padding, y + 15f, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("${receipt.currencySymbol}${String.format("%.2f", item.price)}", width - padding, y + 15f, paint)
            y += 25f
        }

        canvas.drawText("---------------------------------", width / 2, y + 15f, paint)
        y += 25f

        // Subtotal
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("SUBTOTAL:", padding, y + 15f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("${receipt.currencySymbol}${String.format("%.2f", receipt.subtotal)}", width - padding, y + 15f, paint)
        y += 25f

        // Discount
        if (receipt.discountValue > 0.0) {
            val discountText = if (receipt.discountType == "PERCENTAGE") {
                "DISCOUNT (${receipt.discountValue.toInt()}%):"
            } else {
                "DISCOUNT:"
            }
            val subtotal = receipt.subtotal
            val discountAmt = if (receipt.discountType == "PERCENTAGE") {
                subtotal * (receipt.discountValue / 100.0)
            } else {
                receipt.discountValue
            }
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(discountText, padding, y + 15f, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("-${receipt.currencySymbol}${String.format("%.2f", discountAmt)}", width - padding, y + 15f, paint)
            y += 25f
        }

        canvas.drawText("---------------------------------", width / 2, y + 15f, paint)
        y += 25f

        // Grand Total
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("GRAND TOTAL:", padding, y + 15f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("${receipt.currencySymbol}${String.format("%.2f", receipt.totalAmount)}", width - padding, y + 15f, paint)

        // Draw physical PAID Rubber Stamp on Canvas
        canvas.save()
        val stampX = width - padding - 85f
        val stampY = y - 90f // Overlapping subtotal/grand total area
        canvas.translate(stampX, stampY)
        canvas.rotate(-12f)
        
        val stampWidth = 110f
        val stampHeight = 52f
        
        // Draw double line red border
        val stampPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#EF4444")
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        val rectF = android.graphics.RectF(-stampWidth/2f, -stampHeight/2f, stampWidth/2f, stampHeight/2f)
        canvas.drawRoundRect(rectF, 8f, 8f, stampPaint)
        
        // Inner thin border
        stampPaint.strokeWidth = 1f
        val innerRectF = android.graphics.RectF(-stampWidth/2f + 4f, -stampHeight/2f + 4f, stampWidth/2f - 4f, stampHeight/2f - 4f)
        canvas.drawRoundRect(innerRectF, 6f, 6f, stampPaint)

        // Draw Stamp Texts
        stampPaint.style = Paint.Style.FILL
        stampPaint.textAlign = Paint.Align.CENTER
        
        // "পরিশোধিত"
        stampPaint.textSize = 9f
        stampPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        canvas.drawText("পরিশোধিত", 0f, -4f, stampPaint)
        
        // "PAID"
        stampPaint.textSize = 17f
        stampPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        canvas.drawText("PAID", 0f, 16f, stampPaint)
        
        canvas.restore()

        y += 35f

        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("=================================", width / 2, y + 15f, paint)
        y += 30f

        if (receipt.remarks.isNotBlank()) {
            paint.textAlign = Paint.Align.LEFT
            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            canvas.drawText("NOTE: ${receipt.remarks.uppercase()}", padding, y + 15f, paint)
            y += 25f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("---------------------------------", width / 2, y + 15f, paint)
            y += 25f
        }

        // Draw QR code and Signature beside each other
        val rowY = y
        val qrSize = 100f
        
        // QR Code Left
        val qrText = """
        === VERIFIED RECEIPT ===
        No: ${receipt.receiptNo}
        Paid: ${receipt.currencySymbol}${receipt.totalAmount}
        Student: ${receipt.studentName}
        Course: ${receipt.productName}
        Category: ${receipt.category}
        """.trimIndent()
        
        val qrBitmap = generateQrCodeBitmap(qrText, 180)
        if (qrBitmap != null) {
            canvas.drawBitmap(qrBitmap, padding + 10f, rowY, paint)
            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            paint.textAlign = Paint.Align.LEFT
            paint.textSize = 10f
            canvas.drawText("SECURE VERIFY QR", padding + 5f, rowY + qrSize + 15f, paint)
        }

        // Signature Right
        val sigX = width - padding - 150f
        val sigWidth = 150f
        val sigHeight = 65f
        
        val strokes = SignatureSerializer.deserialize(receipt.signaturePointsJson)
        if (strokes.isNotEmpty()) {
            val allPoints = strokes.flatten()
            if (allPoints.isNotEmpty()) {
                val minX = allPoints.minOf { it.x }
                val maxX = allPoints.maxOf { it.x }
                val minY = allPoints.minOf { it.y }
                val maxY = allPoints.maxOf { it.y }
                
                val widthPoints = (maxX - minX).coerceAtLeast(1f)
                val heightPoints = (maxY - minY).coerceAtLeast(1f)

                val scaleX = sigWidth / widthPoints
                val scaleY = sigHeight / heightPoints
                val scale = minOf(scaleX, scaleY) * 0.85f

                val offsetX = sigX + (sigWidth - widthPoints * scale) / 2f - minX * scale
                val offsetY = rowY + (sigHeight - heightPoints * scale) / 2f - minY * scale

                val strokePaint = Paint().apply {
                    color = android.graphics.Color.parseColor("#1E3080") // Navy signature paint
                    strokeWidth = 3f
                    style = Paint.Style.STROKE
                    strokeCap = Paint.Cap.ROUND
                    isAntiAlias = true
                }

                strokes.forEach { stroke ->
                    for (i in 0 until stroke.size - 1) {
                        val p1x = stroke[i].x * scale + offsetX
                        val p1y = stroke[i].y * scale + offsetY
                        val p2x = stroke[i+1].x * scale + offsetX
                        val p2y = stroke[i+1].y * scale + offsetY
                        canvas.drawLine(p1x, p1y, p2x, p2y, strokePaint)
                    }
                }
            }
        } else {
            val placeholderPaint = Paint().apply {
                color = android.graphics.Color.LTGRAY
                textSize = 9f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            }
            canvas.drawText("[ NO SIGNATURE ]", sigX + 20f, rowY + 30f, placeholderPaint)
        }

        // Signature description below signature
        paint.color = android.graphics.Color.BLACK
        paint.textSize = 12f
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        canvas.drawText(receipt.signatureName.uppercase(), width - padding - 75f, rowY + sigHeight + 15f, paint)

        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        paint.textSize = 10f
        canvas.drawText(receipt.signatureDesignation.uppercase(), width - padding - 75f, rowY + sigHeight + 30f, paint)

        y = rowY + sigHeight + 50f

        // Thank you notice
        canvas.drawText("=================================", width / 2, y + 10f, paint)
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        paint.textSize = 12f
        canvas.drawText("THANK YOU FOR YOUR PAYMENT", width / 2, y + 30f, paint)

        return y + 60f
    }

    // Export PDF
    fun exportReceiptAsPdf(context: Context, receipt: ReceiptEntity) {
        try {
            val pdfDocument = PdfDocument()
            // Estimate height based on items
            val itemsCount = ReceiptItem.deserializeList(receipt.itemsJson).size
            val height = 750 + itemsCount * 25 + (if (receipt.remarks.isNotBlank()) 50 else 0)
            
            val pageInfo = PdfDocument.PageInfo.Builder(480, height, 1).create()
            val page = pdfDocument.startPage(pageInfo)

            drawReceiptContent(page.canvas, 480f, receipt)
            pdfDocument.finishPage(page)

            val fileName = "Receipt_${receipt.receiptNo}.pdf"
            val mimeType = "application/pdf"

            val uri = saveFileToDownloads(context, fileName, mimeType) { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            pdfDocument.close()

            if (uri != null) {
                Toast.makeText(context, "$fileName ডাউনলোড ফোল্ডারে সেভ হয়েছে!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "ডাউনলোড ফাইল সেভ করতে ব্যর্থ হয়েছে!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    // Export PNG Image
    fun exportReceiptAsImage(context: Context, receipt: ReceiptEntity) {
        try {
            val itemsCount = ReceiptItem.deserializeList(receipt.itemsJson).size
            val height = 750 + itemsCount * 25 + (if (receipt.remarks.isNotBlank()) 50 else 0)
            val width = 480

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            drawReceiptContent(canvas, width.toFloat(), receipt)

            val fileName = "Receipt_${receipt.receiptNo}.png"
            val mimeType = "image/png"

            val uri = saveFileToDownloads(context, fileName, mimeType) { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }

            if (uri != null) {
                Toast.makeText(context, "$fileName গ্যালারি/ডাউনলোড ফোল্ডারে সেভ হয়েছে!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "ইমেজ সেভ করতে ব্যর্থ হয়েছে!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper using MediaStore for safe download folder writes without storage permissions on modern SDKs
    private fun saveFileToDownloads(
        context: Context,
        fileName: String,
        mimeType: String,
        writeBlock: (OutputStream) -> Unit
    ): Uri? {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
        }

        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        } else {
            // Older device fallback
            val targetDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = java.io.File(targetDir, fileName)
            try {
                java.io.FileOutputStream(file).use { outputStream ->
                    writeBlock(outputStream)
                }
                return Uri.fromFile(file)
            } catch (e: Exception) {
                null
            }
        }

        if (uri != null) {
            try {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    writeBlock(outputStream)
                }
                return uri
            } catch (e: Exception) {
                resolver.delete(uri, null, null)
            }
        }
        return null
    }
}
