package com.game.enums;

import lombok.Getter;

@Getter
public enum LinearSymbols {
    SAME_SYMBOLS_HORIZONTALLY("horizontally_linear_symbols", "same_symbols_horizontally"),
    SAME_SYMBOLS_VERTICALLY("vertically_linear_symbols", "same_symbols_vertically"),
    SAME_SYMBOLS_DIAGONALLY_LEFT_TO_RIGHT("ltr_diagonally_linear_symbols", "same_symbols_diagonally_left_to_right"),
    SAME_SYMBOLS_DIAGONALLY_RIGHT_TO_LEFT("rtl_diagonally_linear_symbols", "same_symbols_diagonally_right_to_left");

    private final String group;
    private final String name;

    LinearSymbols(String group, String name) {
        this.group = group;
        this.name = name;
    }

    public static String getWinCombinationNameBasedOnGroupName(String groupName) {
        for (LinearSymbols combination : LinearSymbols.values()) {
            if (groupName.equalsIgnoreCase(combination.getGroup()))
                return combination.getName();
        }

        throw new IllegalArgumentException("LinearSymbols group name is not valid!");
    }
}
