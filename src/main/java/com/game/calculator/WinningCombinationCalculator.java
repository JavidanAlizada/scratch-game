package com.game.calculator;

import com.game.config.GameConfig;
import com.game.enums.LinearSymbols;
import com.game.model.GameResult;
import com.game.model.Symbol;
import com.game.model.WinCombination;

import java.util.*;

public class WinningCombinationCalculator {

    public GameResult calculateWinningsCombination(String[][] matrix, double bettingAmount, GameConfig config) {
        double totalReward = 0;
        Map<String, List<String>> appliedCombinations = new HashMap<>();
        String appliedBonusSymbol = null;

        // Same symbols winning combinations (e.g., same_symbol_X_times)
        Map<Integer, Double> sameSymbolsWinMap = new HashMap<>();
        for (WinCombination winCombination : config.getWinCombinations().values()) {
            if (winCombination.getWhen().equals("same_symbols")) {
                sameSymbolsWinMap.put(winCombination.getCount(), winCombination.getRewardMultiplier());
            }
        }

        // Linear symbols winning combinations (e.g., horizontally_linear_symbols)
        Map<String, Map<Double, List<List<List<String>>>>> linearSymbolsWinMap = new HashMap<>();
        for (WinCombination winCombination : config.getWinCombinations().values()) {
            if (winCombination.getWhen().equals("linear_symbols")) {
                List<List<List<String>>> linearGroup = new ArrayList<>();
                linearGroup.add(winCombination.getCoveredAreas());
                linearSymbolsWinMap.computeIfAbsent(winCombination.getGroup(), k -> new HashMap<>())
                        .put(winCombination.getRewardMultiplier(), linearGroup);
            }
        }

        // Symbol counters
        Map<String, Integer> symbolCounters = countSymbols(matrix);
        Map<String, Integer> bonusSymbolCounters = countBonusSymbols(matrix, config);

        // Calculating score for same symbols
        Map<String, Double> sameSymbolScoreMap = calculateSameSymbolScores(symbolCounters, sameSymbolsWinMap, config, appliedCombinations);

        // Calculating score for linear symbols
        Map<String, Double> linearSymbolScoreMap = calculateLinearSymbolScores(matrix, linearSymbolsWinMap, bonusSymbolCounters, appliedCombinations);

        // Calculate total score
        boolean hasWinningCombinations = !sameSymbolScoreMap.isEmpty() || !linearSymbolScoreMap.isEmpty();
        if (hasWinningCombinations) {
            totalReward = sameSymbolScoreMap.entrySet().stream()
                    .map(e -> e.getValue() * linearSymbolScoreMap.getOrDefault(e.getKey(), 1.0) * bettingAmount)
                    .mapToDouble(Double::doubleValue)
                    .sum();

            totalReward = applyBonusesToScore(config.getSymbolMap(), bonusSymbolCounters, totalReward);
        }

        appliedBonusSymbol = findAppliedBonusSymbol(bonusSymbolCounters, config);

        GameResult gameResult = new GameResult();
        gameResult.setMatrix(matrix);
        gameResult.setTotalReward(totalReward);
        gameResult.setAppliedCombinations(appliedCombinations);
        gameResult.setAppliedBonusSymbol(appliedBonusSymbol);

        return gameResult;
    }

    private String findAppliedBonusSymbol(Map<String, Integer> bonusSymbolCounters, GameConfig config) {
        for (Map.Entry<String, Integer> entry : bonusSymbolCounters.entrySet()) {
            Symbol symbol = config.getSymbolMap().get(entry.getKey());
            if (symbol != null && symbol.isBonus()) {
                return entry.getKey();
            }
        }
        return null;
    }

    // Helper to count symbols in the matrix
    private Map<String, Integer> countSymbols(String[][] matrix) {
        Map<String, Integer> symbolCounters = new HashMap<>();
        for (String[] row : matrix) {
            for (String symbol : row) {
                symbolCounters.put(symbol, symbolCounters.getOrDefault(symbol, 0) + 1);
            }
        }
        return symbolCounters;
    }

    // Helper to count bonus symbols in the matrix
    private Map<String, Integer> countBonusSymbols(String[][] matrix, GameConfig config) {
        Map<String, Integer> bonusSymbolCounters = new HashMap<>();
        for (String[] row : matrix) {
            for (String symbol : row) {
                Symbol symbolDetails = config.getSymbolMap().get(symbol);
                if (symbolDetails != null && symbolDetails.isBonus()) {
                    bonusSymbolCounters.put(symbol, bonusSymbolCounters.getOrDefault(symbol, 0) + 1);
                }
            }
        }
        return bonusSymbolCounters;
    }

    // Helper to calculate scores for same symbol combinations
    private Map<String, Double> calculateSameSymbolScores(Map<String, Integer> symbolCounters,
                                                          Map<Integer, Double> sameSymbolsWinMap,
                                                          GameConfig config,
                                                          Map<String, List<String>> appliedCombinations) {
        Map<String, Double> sameSymbolScoreMap = new HashMap<>();
        List<Integer> countWins = new ArrayList<>(sameSymbolsWinMap.keySet());
        Collections.sort(countWins);

        symbolCounters.forEach((key, value) -> {
            if (countWins.contains(value)) {
                sameSymbolScoreMap.put(key, sameSymbolsWinMap.get(value) * config.getSymbolMap().get(key).getRewardMultiplier());
                appliedCombinations.computeIfAbsent(key, k -> new ArrayList<>())
                        .add(String.format("same_symbol_%s_times", value));
            } else if (value > countWins.get(countWins.size() - 1)) {
                sameSymbolScoreMap.put(key, sameSymbolsWinMap.get(countWins.get(countWins.size() - 1)) * value * config.getSymbolMap().get(key).getRewardMultiplier());
                appliedCombinations.computeIfAbsent(key, k -> new ArrayList<>())
                        .add(String.format("same_symbol_%s_times", countWins.get(countWins.size() - 1)));
            }
        });

        return sameSymbolScoreMap;
    }

    // Helper to calculate scores for linear symbol combinations
    private Map<String, Double> calculateLinearSymbolScores(String[][] matrix,
                                                            Map<String, Map<Double, List<List<List<String>>>>> linearSymbolsWinMap,
                                                            Map<String, Integer> bonusSymbolCounters,
                                                            Map<String, List<String>> appliedCombinations) {
        Map<String, Double> linearSymbolScoreMap = new HashMap<>();
        linearSymbolsWinMap.forEach((group, groupWithScore) -> {
            groupWithScore.forEach((multiplier, patternList) -> {
                for (List<List<String>> patternGroup : patternList) {
                    for (List<String> pattern : patternGroup) {
                        String tempSymbol = "";
                        boolean isSameRow = true;
                        for (String position : pattern) {
                            String[] coordinates = position.split(":");
                            int x = Integer.parseInt(coordinates[0]);
                            int y = Integer.parseInt(coordinates[1]);
                            String symbol = matrix[x][y];
                            if (tempSymbol.isEmpty()) {
                                tempSymbol = symbol;
                            }
                            if (!tempSymbol.equals(symbol) || bonusSymbolCounters.containsKey(symbol)) {
                                isSameRow = false;
                                break;
                            }
                        }
                        if (isSameRow) {
                            linearSymbolScoreMap.put(tempSymbol, linearSymbolScoreMap.getOrDefault(tempSymbol, 1.0) * multiplier);
                            appliedCombinations.computeIfAbsent(tempSymbol, k -> new ArrayList<>())
                                    .add(LinearSymbols.getWinCombinationNameBasedOnGroupName(group));
                        }
                    }
                }
            });
        });

        return linearSymbolScoreMap;
    }

    // Apply bonus multipliers
    private double applyBonusesToScore(Map<String, Symbol> symbols, Map<String, Integer> bonusSymbolCounters, double score) {
        for (Map.Entry<String, Integer> entry : bonusSymbolCounters.entrySet()) {
            Symbol symbol = symbols.get(entry.getKey());
            if (symbol != null) {
                for (int i = 1; i <= entry.getValue(); i++) {
                    score = symbol.applyBonus(score);
                }
            }
        }
        return score;
    }
}
