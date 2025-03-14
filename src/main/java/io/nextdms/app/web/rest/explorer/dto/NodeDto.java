package io.nextdms.app.web.rest.explorer.dto;

import io.nextdms.app.validator.NodeDtoValidate;

@NodeDtoValidate
public record NodeDto(String path, String UUID) {}
