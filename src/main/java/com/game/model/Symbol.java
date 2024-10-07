package com.game.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.game.enums.BonusImpact;
import com.game.enums.SymbolType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Symbol {
    @JsonProperty("reward_multiplier")
    private double rewardMultiplier;
    private String type;
    private Double extra;
    private String impact;

    private BonusImpact getBonusImpact() {
        if (SymbolType.BONUS.getType().equals(this.getType())) {
            return BonusImpact.fromValue(this.getImpact());
        }
        return null;
    }

    public boolean isBonus() {
        return SymbolType.BONUS.getType().equals(this.getType());
    }

    public boolean isMultiplicationBonus() {
        return BonusImpact.MULTIPLY_REWARD.equals(this.getBonusImpact());
    }

    public boolean isMissBonus() {
        return BonusImpact.MISS.equals(this.getBonusImpact());
    }

    public boolean isExtraBonus() {
        return BonusImpact.EXTRA_BONUS.equals(this.getBonusImpact());
    }

    public double applyBonus(double currentReward) {
        if (isMultiplicationBonus()) {
            return currentReward * rewardMultiplier;
        } else if (isExtraBonus()) {
            return currentReward + extra;
        }
        return currentReward;
    }
}
