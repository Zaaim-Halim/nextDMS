package io.nextdms.dto.explorer;

public record JcrProperty(String name, int type, boolean multiValue, boolean readOnly, JcrValue[] values) {}
