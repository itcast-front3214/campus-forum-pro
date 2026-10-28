package com.campus.forum.common.enums;

import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.Getter;

@Getter
public enum AuditStatus implements IEnum<String> {
    PENDING("PENDING", "待审核"),
    REVIEWING("REVIEWING", "审核中"),
    APPROVED("APPROVED", "已通过"),
    REJECTED("REJECTED", "已拒绝");

    private final String value;
    private final String desc;

    AuditStatus(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
