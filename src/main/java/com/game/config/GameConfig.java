package com.game.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.game.model.Probability;
import com.game.model.Symbol;
import com.game.model.WinCombination;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
public class GameConfig {
    private int columns;
    private int rows;

    @JsonProperty("symbols")
    private Map<String, Symbol> symbolMap;

    private Probability probabilities;

    @JsonProperty("win_combinations")
    private Map<String, WinCombination> winCombinations;

    @Override
    public String toString() {
        return "GameConfig{" +
                "columns=" + columns +
                ", rows=" + rows +
                ", symbolMap=" + symbolMap +
                ", probabilities=" + probabilities +
                ", winCombinations=" + winCombinations +
                '}';
    }
}
