package ru.practicum.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NotOnlySpacesValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotOnlySpaces {
    String message() default "Category name cannot be only whitespace";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
