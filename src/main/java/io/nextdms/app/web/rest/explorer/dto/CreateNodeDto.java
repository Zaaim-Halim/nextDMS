package io.nextdms.app.web.rest.explorer.dto;

import io.nextdms.app.validator.NodeDtoValidate;
import io.nextdms.dto.explorer.JcrNode;

public record CreateNodeDto(
    @NodeDtoValidate NodeDto parent,
    JcrNode node
    //TODO : add files
) {}
