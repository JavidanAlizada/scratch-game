package com.game.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
public class StandardSymbolProbability {
    private int column;
    private int row;
    private Map<String, Integer> symbols;
}
