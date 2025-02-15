package io.nextdms.dto.explorer;

import java.util.List;
import java.util.Map;

public record JcrNode(
    String uuid,
    String name,
    String path,
    String primaryNodeType,
    List<String> mixinTypes,
    Map<String, JcrProperty> properties
) {}
