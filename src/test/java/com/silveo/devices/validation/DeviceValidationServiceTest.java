package com.silveo.devices.validation;

import com.silveo.devices.entity.embed.DeviceParameterTemplate;
import com.silveo.devices.entity.enums.ParameterType;
import com.silveo.devices.service.DeviceValidationService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeviceValidationServiceTest {
    private final DeviceValidationService validator = new DeviceValidationService();

    // -------------------- BOOLEAN --------------------
    @Test
    void boolean_shouldReturnTrueForValidValue() {
        DeviceParameterTemplate template = createTemplate(ParameterType.BOOLEAN, "true");
        assertTrue(validator.validateParameter(template, "true"));
        assertTrue(validator.validateParameter(template, "TRUE")); // Проверка регистра
    }

    @Test
    void boolean_shouldReturnFalseForInvalidValue() {
        DeviceParameterTemplate template = createTemplate(ParameterType.BOOLEAN, "false");
        assertFalse(validator.validateParameter(template, "true")); // Ожидается false, пришло true
        assertFalse(validator.validateParameter(template, "yes"));  // Невалидный формат
    }

    // -------------------- NOT_EQUALS --------------------
    @Test
    void notEquals_shouldReturnTrueWhenValuesDiffer() {
        DeviceParameterTemplate template = createTemplate(ParameterType.NOT_EQUALS, "error");
        assertTrue(validator.validateParameter(template, "success"));
        assertTrue(validator.validateParameter(template, "0"));
    }

    @Test
    void notEquals_shouldReturnFalseWhenValuesMatch() {
        DeviceParameterTemplate template = createTemplate(ParameterType.NOT_EQUALS, "ok");
        assertFalse(validator.validateParameter(template, "ok"));
        assertFalse(validator.validateParameter(template, "OK")); // Проверка регистра
    }

    // -------------------- GREATER_THAN --------------------
    @Test
    void greaterThan_valid() {
        DeviceParameterTemplate template = createTemplate(ParameterType.GREATER_THAN, "10");
        assertTrue(validator.validateParameter(template, "15"));  // 15 > 10 → OK
        assertFalse(validator.validateParameter(template, "5"));   // 5 > 10 → Ошибка
        assertFalse(validator.validateParameter(template, "10"));  // 10 > 10 → Ошибка
    }

    // -------------------- LESS_THAN --------------------
    @Test
    void lessThan_valid() {
        DeviceParameterTemplate template = createTemplate(ParameterType.LESS_THAN, "20");
        assertTrue(validator.validateParameter(template, "15"));   // 15 < 20 → OK
        assertFalse(validator.validateParameter(template, "25"));  // 25 < 20 → Ошибка
        assertFalse(validator.validateParameter(template, "20"));  // 20 < 20 → Ошибка
    }

    // -------------------- EQUALS (числа) --------------------
    @Test
    void equals_valid() {
        DeviceParameterTemplate template = createTemplate(ParameterType.EQUALS, "100");
        assertTrue(validator.validateParameter(template, "100"));  // 100 == 100 → OK
        assertFalse(validator.validateParameter(template, "101"));  // 101 == 100 → Ошибка
    }

    // -------------------- EQUALS_STRING --------------------
    @Test
    void equalsString_valid() {
        DeviceParameterTemplate template = createTemplate(ParameterType.EQUALS_STRING, "active");
        assertTrue(validator.validateParameter(template, "active"));  // active == active → OK
        assertFalse(validator.validateParameter(template, "inactive")); // inactive != active → Ошибка
    }

    // -------------------- RANGE --------------------
    @Test
    void range_valid() {
        DeviceParameterTemplate template = createRangeTemplate("10", "20");
        assertTrue(validator.validateParameter(template, "15"));   // 15 ∈ [10-20] → OK
        assertTrue(validator.validateParameter(template, "10"));   // 10 ∈ [10-20] → OK
        assertTrue(validator.validateParameter(template, "20"));   // 20 ∈ [10-20] → OK
        assertFalse(validator.validateParameter(template, "5"));    // 5 ∉ [10-20] → Ошибка
        assertFalse(validator.validateParameter(template, "25"));   // 25 ∉ [10-20] → Ошибка
    }

    // -------------------- DEVIATION --------------------
    @Test
    void deviation_valid() {
        DeviceParameterTemplate template = createDeviationTemplate("100", "5");
        assertTrue(validator.validateParameter(template, "102"));   // 102 ∈ [95-105] → OK
        assertTrue(validator.validateParameter(template, "95"));    // 95 ∈ [95-105] → OK
        assertFalse(validator.validateParameter(template, "94"));   // 94 ∉ [95-105] → Ошибка
        assertFalse(validator.validateParameter(template, "106"));  // 106 ∉ [95-105] → Ошибка
    }


    // -------------------- Edge Cases --------------------
    @Test
    void invalidNumberFormat_returnsFalse() {
        DeviceParameterTemplate template = createTemplate(ParameterType.GREATER_THAN, "10");
        assertFalse(validator.validateParameter(template, "not_a_number")); // Нечисловое значение → Ошибка
    }

    // -------------------- Вспомогательный метод --------------------
    private DeviceParameterTemplate createTemplate(ParameterType type, String value) {
        DeviceParameterTemplate template = new DeviceParameterTemplate();
        template.setType(type);
        template.setValue(value);
        return template;
    }

    private DeviceParameterTemplate createRangeTemplate(String min, String max) {
        DeviceParameterTemplate template = new DeviceParameterTemplate();
        template.setType(ParameterType.RANGE);
        template.setMinValue(min);
        template.setMaxValue(max);
        return template;
    }

    private DeviceParameterTemplate createDeviationTemplate(String value, String tolerance) {
        DeviceParameterTemplate template = new DeviceParameterTemplate();
        template.setType(ParameterType.DEVIATION);
        template.setValue(value);
        template.setTolerancePercent(tolerance);
        return template;
    }
}