package com.yqhp.console.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class TreeNodeMoveEvent {
    @NotBlank(message = "from不能为空")
    private String from;
    @NotBlank(message = "to不能为空")
    private String to;
    @NotNull(message = "type不能为空")
    private Type type;

    public boolean isInner() {
        return Type.INNER.equals(type);
    }

    public boolean isBefore() {
        return Type.BEFORE.equals(type);
    }

    public boolean isAfter() {
        return Type.AFTER.equals(type);
    }

    enum Type {
        BEFORE,
        AFTER,
        INNER
    }
}
