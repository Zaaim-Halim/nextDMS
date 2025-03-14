package io.nextdms.app.validator.impl;

import io.nextdms.app.validator.NodeDtoValidate;
import io.nextdms.app.web.rest.explorer.dto.NodeDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

public class NodeDtoValidator implements ConstraintValidator<NodeDtoValidate, NodeDto> {

    @Override
    public void initialize(NodeDtoValidate constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(NodeDto value, ConstraintValidatorContext context) {
        return StringUtils.hasText(value.path()) || StringUtils.hasText(value.UUID());
    }
}
