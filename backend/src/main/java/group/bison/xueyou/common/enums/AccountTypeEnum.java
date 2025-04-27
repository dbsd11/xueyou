package group.bison.xueyou.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 记账类型枚举
 */
public enum AccountTypeEnum {
    INCOME("收入"),
    EXPENSE("支出"),
    TRANSFER("转账");

    private final String description;

    AccountTypeEnum(String description) {
        this.description = description;
    }

    @JsonValue
    public String getDescription() {
        return description;
    }
}