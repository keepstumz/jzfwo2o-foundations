package com.jzo2o.foundations.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Mushengda
 * @version 1.0
 * @time 2024-04-03 11:28
 */
@AllArgsConstructor
@Getter
public enum FoundationIsHotEnum {
    NOTHOT(0,"非热门"),
    HOT(2,"热门");
    private int status;
    private String description;

    public boolean equals(Integer status) {
        return this.status == status;
    }

    public boolean equals(FoundationIsHotEnum foundationIsHotEnum) {
        return foundationIsHotEnum != null && foundationIsHotEnum.status == this.getStatus();
    }
}
