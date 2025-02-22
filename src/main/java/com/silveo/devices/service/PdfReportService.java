package com.silveo.devices.service;

import com.lowagie.text.DocumentException;
import com.silveo.devices.entity.DeviceType;
import com.silveo.devices.entity.dto.DeviceInstanceReportDto;
import com.silveo.devices.entity.embed.DeviceParameterTemplate;
import com.silveo.devices.entity.enums.ParameterType;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    public byte[] generateDeviceTypePdfReport(DeviceType deviceType) throws IOException, DocumentException {
        // Преобразуем параметры в формат для шаблона
        Map<String, String> formattedParameters = formatDeviceParameters(deviceType.getParameters());

        Context context = new Context();
        context.setVariable("deviceType", deviceType);
        context.setVariable("formattedParameters", formattedParameters);

        String html = templateEngine.process("device_type_report_template", context);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
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

    private Map<String, String> formatDeviceParameters(List<DeviceParameterTemplate> parameters) {
        return parameters.stream()
                .filter(Objects::nonNull)
                .filter(p -> p.getName() != null)
                .collect(Collectors.toMap(
                        DeviceParameterTemplate::getName,
                        this::formatParameterValue
                ));
    }

    private String formatParameterValue(DeviceParameterTemplate param) {
        return switch (param.getType()) {
            case ParameterType.RANGE -> {
                if (param.getMinValue() == null || param.getMaxValue() == null) {
                    yield "Invalid range configuration";
                }
                yield String.format("[%s - %s]", param.getMinValue(), param.getMaxValue());
            }
            case ParameterType.ENUM -> {
                if (param.getAllowedValues() == null || param.getAllowedValues().isEmpty()) {
                    yield "No allowed values specified";
                }
                yield "Allowed: " + param.getAllowedValues();
            }
            case ParameterType.DEVIATION -> String.format(
                    "%s ±%s%%",
                    param.getValue(),
                    param.getTolerancePercent()
            );
            case ParameterType.GREATER_THAN -> ">" + param.getValue();
            case ParameterType.LESS_THAN -> "<" + param.getValue();
            case ParameterType.EQUALS, EQUALS_STRING -> param.getValue();
            case ParameterType.BOOLEAN -> "Должно быть: " + param.getValue();
            case ParameterType.NOT_EQUALS -> "Не должно быть: " + param.getValue();
            default -> param.getValue() != null ? param.getValue() : "N/A";
        };
    }
}