package ru.practicum.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

public class NotBeforeHoursValidator implements ConstraintValidator<NotBeforeHours, LocalDateTime> {

    private int hours;

    @Override
    public void initialize(NotBeforeHours constraintAnnotation) {
        this.hours = constraintAnnotation.hours();
    }

    @Override
    public boolean isValid(LocalDateTime value, ConstraintValidatorContext context) {
        if (value == null) return false;
        return value.isAfter(LocalDateTime.now().plusHours(hours));
    }
}
