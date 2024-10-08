package com.game.calculator;

import com.game.config.GameConfig;
import com.game.enums.LinearSymbols;
import com.game.model.GameResult;
import com.game.model.Symbol;

import java.util.*;

public class WinningCombinationCalculator {

    public GameResult calculateWinningsCombination(String[][] matrix, double bettingAmount, GameConfig config) {
        double totalReward = 0.0;
        Map<String, List<String>> appliedCombinations = new HashMap<>();
        Optional<String> appliedBonusSymbol;

        Map<Integer, Double> sameSymbolsWinMap = extractSameSymbolsWinMap(config);
        Map<String, Map<Double, List<List<List<String>>>>> linearSymbolsWinMap = extractLinearSymbolsWinMap(config);

        Map<String, Integer> symbolCounters = countSymbols(matrix);
        Map<String, Integer> bonusSymbolCounters = countBonusSymbols(matrix, config);

        // Score Calculations
        Map<String, Double> sameSymbolScoreMap = calculateSameSymbolScores(symbolCounters, sameSymbolsWinMap, config, appliedCombinations);
        Map<String, Double> linearSymbolScoreMap = calculateLinearSymbolScores(matrix, linearSymbolsWinMap, bonusSymbolCounters, appliedCombinations);

        if (!sameSymbolScoreMap.isEmpty() || !linearSymbolScoreMap.isEmpty()) {
            totalReward = calculateTotalReward(sameSymbolScoreMap, linearSymbolScoreMap, bettingAmount);
            totalReward = applyBonusesToScore(config.getSymbolMap(), bonusSymbolCounters, totalReward);
        }

        appliedBonusSymbol = findAppliedBonusSymbol(bonusSymbolCounters, config);

        return createGameResult(matrix, totalReward, appliedCombinations, appliedBonusSymbol);
    }

    private Map<Integer, Double> extractSameSymbolsWinMap(GameConfig config) {
        return config.getWinCombinations().values().stream()
                .filter(winComb -> "same_symbols".equals(winComb.getWhen()))
                .collect(HashMap::new, (map, wc) -> map.put(wc.getCount(), wc.getRewardMultiplier()), HashMap::putAll);
    }

    private Map<String, Map<Double, List<List<List<String>>>>> extractLinearSymbolsWinMap(GameConfig config) {
        Map<String, Map<Double, List<List<List<String>>>>> linearSymbolsWinMap = new HashMap<>();
        config.getWinCombinations().values().stream()
                .filter(winComb -> "linear_symbols".equals(winComb.getWhen()))
                .forEach(winComb -> linearSymbolsWinMap
                        .computeIfAbsent(winComb.getGroup(), k -> new HashMap<>())
                        .computeIfAbsent(winComb.getRewardMultiplier(), k -> new ArrayList<>())
                        .add(winComb.getCoveredAreas()));
        return linearSymbolsWinMap;
    }

    private Map<String, Integer> countSymbols(String[][] matrix) {
        Map<String, Integer> symbolCounters = new HashMap<>();
        Arrays.stream(matrix).flatMap(Arrays::stream)
                .forEach(symbol -> symbolCounters.merge(symbol, 1, Integer::sum));
        return symbolCounters;
    }

    private Map<String, Integer> countBonusSymbols(String[][] matrix, GameConfig config) {
        Map<String, Integer> bonusSymbolCounters = new HashMap<>();
        Arrays.stream(matrix).flatMap(Arrays::stream)
                .forEach(symbol -> {
                    Symbol symbolDetails = config.getSymbolMap().get(symbol);
                    if (symbolDetails != null && symbolDetails.isBonus()) {
                        bonusSymbolCounters.merge(symbol, 1, Integer::sum);
                    }
                });
        return bonusSymbolCounters;
    }

    private Map<String, Double> calculateSameSymbolScores(Map<String, Integer> symbolCounters,
                                                          Map<Integer, Double> sameSymbolsWinMap,
                                                          GameConfig config,
                                                          Map<String, List<String>> appliedCombinations) {
        Map<String, Double> sameSymbolScoreMap = new HashMap<>();
        List<Integer> countWins = new ArrayList<>(sameSymbolsWinMap.keySet());
        Collections.sort(countWins);

        symbolCounters.forEach((symbol, count) -> {
            int validCount = countWins.stream()
                    .filter(c -> c <= count)
                    .max(Integer::compare)
                    .orElse(0);

            if (validCount > 0) {
                double multiplier = sameSymbolsWinMap.get(validCount) * config.getSymbolMap().get(symbol).getRewardMultiplier();
                sameSymbolScoreMap.put(symbol, multiplier);
                appliedCombinations.computeIfAbsent(symbol, k -> new ArrayList<>())
                        .add(String.format("same_symbol_%s_times", validCount));
            }
        });

        return sameSymbolScoreMap;
    }

    private Map<String, Double> calculateLinearSymbolScores(String[][] matrix,
                                                            Map<String, Map<Double, List<List<List<String>>>>> linearSymbolsWinMap,
                                                            Map<String, Integer> bonusSymbolCounters,
                                                            Map<String, List<String>> appliedCombinations) {
        Map<String, Double> linearSymbolScoreMap = new HashMap<>();
        linearSymbolsWinMap.forEach((group, groupWithScore) -> {
            groupWithScore.forEach((multiplier, patternList) -> {
                patternList.forEach(patternGroup -> {
                    patternGroup.forEach(pattern -> {
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
                            linearSymbolScoreMap.merge(tempSymbol, multiplier, (v1, v2) -> v1 * v2);
                            appliedCombinations.computeIfAbsent(tempSymbol, k -> new ArrayList<>())
                                    .add(LinearSymbols.getWinCombinationNameBasedOnGroupName(group));
                        }
                    });
                });
            });
        });

        return linearSymbolScoreMap;
    }

    private double calculateTotalReward(Map<String, Double> sameSymbolScoreMap, Map<String, Double> linearSymbolScoreMap, double bettingAmount) {
        return sameSymbolScoreMap.entrySet().stream()
                .mapToDouble(e -> e.getValue() * linearSymbolScoreMap.getOrDefault(e.getKey(), 1.0) * bettingAmount)
                .sum();
    }

    private double applyBonusesToScore(Map<String, Symbol> symbols, Map<String, Integer> bonusSymbolCounters, double score) {
        return bonusSymbolCounters.entrySet().stream()
                .reduce(score, (s, entry) -> {
                    Symbol symbol = symbols.get(entry.getKey());
                    if (symbol != null) {
                        for (int i = 1; i <= entry.getValue(); i++) {
                            s = symbol.applyBonus(s);
                        }
                    }
                    return s;
                }, Double::sum);
    }

    private Optional<String> findAppliedBonusSymbol(Map<String, Integer> bonusSymbolCounters, GameConfig config) {
        return bonusSymbolCounters.keySet().stream()
                .filter(key -> Optional.ofNullable(config.getSymbolMap().get(key)).map(Symbol::isBonus).orElse(false))
                .findFirst();
    }

    private GameResult createGameResult(String[][] matrix, double totalReward, Map<String, List<String>> appliedCombinations, Optional<String> appliedBonusSymbol) {
        GameResult gameResult = new GameResult();
        gameResult.setMatrix(matrix);
        gameResult.setTotalReward(totalReward);
        gameResult.setAppliedCombinations(appliedCombinations);
        appliedBonusSymbol.ifPresent(gameResult::setAppliedBonusSymbol);
        return gameResult;
    }
}
