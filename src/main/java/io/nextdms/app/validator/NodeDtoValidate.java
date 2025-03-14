package io.nextdms.app.validator;

import io.nextdms.app.validator.impl.NodeDtoValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NodeDtoValidator.class)
public @interface NodeDtoValidate {
    String message() default "At least one of the properties {path ,UUID} must be non-empty and non-null and non-blank";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
