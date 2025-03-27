package ru.practicum.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NotBeforeHoursValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotBeforeHours {
    String message() default "Дата должна быть не раньше, чем через {hours} часов от текущего времени";
    int hours() default 2;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
