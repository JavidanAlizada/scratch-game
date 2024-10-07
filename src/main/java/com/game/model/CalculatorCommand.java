package com.game.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CalculatorCommand {
    private Map<String, Integer> symbolCounters;
    private Map<String, Double> sameSymbolsWinMap;
    private Map<String, Map<Double, List<List<List<String>>>>> linearSymbolsWinMap;
    private Map<String, List<String>> appliedWinningCombinations;
}
