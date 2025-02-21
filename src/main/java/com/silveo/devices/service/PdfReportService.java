package com.silveo.devices.service;

import com.lowagie.text.DocumentException;
import com.silveo.devices.entity.dto.DeviceInstanceReportDto;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PdfReportService {
    private final SpringTemplateEngine templateEngine;

    public byte[] generatePdfReport(DeviceInstanceReportDto report) throws IOException, DocumentException {
        // Генерируем HTML из шаблона
        Context context = new Context();
        context.setVariable("report", report);
        String html = templateEngine.process("report_template", context);

        // Конвертируем HTML в PDF
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();

            // Указываем путь к шрифту с поддержкой кириллицы
            renderer.getFontResolver().addFont(
                    "static/fonts/arial.ttf",
                    "Identity-H",
                    true
            );

            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);
            return outputStream.toByteArray();
        }
    }
}