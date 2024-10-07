package com.game.enums;

import lombok.Getter;

@Getter
public enum SymbolType {
    STANDARD("standard"), BONUS("bonus");

    private final String type;

    SymbolType(String type) {
        this.type = type;
    }

}
