package com.game.enums;

import lombok.Getter;

@Getter
public enum BonusImpact {
    MULTIPLY_REWARD("multiply_reward"), EXTRA_BONUS("extra_bonus"), MISS("miss");

    private final String impact;

    BonusImpact(String impact) {
        this.impact = impact;
    }

    public static BonusImpact fromValue(String impact) {
        for (BonusImpact bonusImpact : BonusImpact.values()) {
            if (impact.equalsIgnoreCase(bonusImpact.getImpact())) {
                return bonusImpact;
            }
        }
        throw new IllegalArgumentException("Bonus impact value is not valid!");
    }
}
