package com.game.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
public class BonusSymbolProbability {
    public Map<String, Integer> symbols;
}
