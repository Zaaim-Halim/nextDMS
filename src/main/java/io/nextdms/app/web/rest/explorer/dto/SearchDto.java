package io.nextdms.app.web.rest.explorer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SearchDto(
    @NotBlank(message = "query must not be blank!") String query,
    @NotBlank(message = "query Type must not be blank!.")
    @Pattern(regexp = "^(xpath|JCR-SQL2)$", message = "query Type supported values are : xpath, JCR-SQL2")
    String type
) {}
