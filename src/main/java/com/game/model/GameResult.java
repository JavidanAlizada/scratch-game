package com.game.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
public class GameResult {
    private String[][] matrix;
    @JsonProperty("reward")
    private double totalReward;
    @JsonProperty("applied_winning_combinations")
    private Map<String, List<String>> appliedCombinations;
    @JsonProperty("applied_bonus_symbol")
    private String appliedBonusSymbol;
}
