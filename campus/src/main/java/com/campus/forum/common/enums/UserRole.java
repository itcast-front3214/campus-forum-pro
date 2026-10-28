package com.campus.forum.common.enums;

import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.Getter;

@Getter
public enum UserRole implements IEnum<String> {
    STUDENT("STUDENT", "普通学生"),
    MODERATOR("MODERATOR", "版主"),
    ADMIN("ADMIN", "管理员");

    private final String value;
    private final String desc;

    UserRole(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public String getSpringRole() {
        return "ROLE_" + this.value;
    }
}
