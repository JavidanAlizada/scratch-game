package com.game.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class Probability {
    @JsonProperty("standard_symbols")
    private List<StandardSymbolProbability> standardSymbols;
    @JsonProperty("bonus_symbols")
    private BonusSymbolProbability bonusSymbols;
}
