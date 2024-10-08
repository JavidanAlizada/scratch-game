package com.game.generator;

import com.game.config.GameConfig;
import com.game.model.BonusSymbolProbability;
import com.game.model.Probability;
import com.game.model.StandardSymbolProbability;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.game.errors.MatrixGeneratorErrors.*;

public final class MatrixGenerator {

    private MatrixGenerator() {
        throw new UnsupportedOperationException("This class cannot be instantiated.");
    }

    public static String[][] generate(GameConfig config) {
        validateConfig(config);
        String[][] matrix = new String[config.getRows()][config.getColumns()];

        Map<String, Integer> symbolCounters = new HashMap<>();
        Map<String, Integer> bonusSymbolCounters = new HashMap<>();

        boolean bonusSymbolAssigned = false;

        fillMatrixWithSymbols(config, matrix, symbolCounters, bonusSymbolCounters, bonusSymbolAssigned);

        return matrix;
    }

    private static void validateConfig(GameConfig config) {
        if (config.getRows() <= 0 || config.getColumns() <= 0) {
            throw new IllegalArgumentException(INVALID_ROW_OR_COLUMNS);
        }

        boolean invalidSymbolPosition = config.getProbabilities().getStandardSymbols().stream().anyMatch(probability -> isInvalidProbabilityRowsOrColumns(config, probability));

        if (invalidSymbolPosition) {
            throw new IllegalArgumentException(INVALID_SYMBOL_POSITION);
        }
    }

    private static boolean isInvalidProbabilityRowsOrColumns(GameConfig config, StandardSymbolProbability probability) {
        return probability.getRow() < 0 || probability.getRow() >= config.getRows() || probability.getColumn() < 0 || probability.getColumn() >= config.getColumns();
    }

    private static void fillMatrixWithSymbols(GameConfig config, String[][] matrix, Map<String, Integer> symbolCounters, Map<String, Integer> bonusSymbolCounters, boolean bonusSymbolAssigned) {
        Probability probabilities = config.getProbabilities();
        List<StandardSymbolProbability> standardSymbols = probabilities.getStandardSymbols();

        for (StandardSymbolProbability symbolProbability : standardSymbols) {
            String symbol = assignSymbol(symbolProbability, probabilities.getBonusSymbols(), symbolCounters, bonusSymbolCounters, bonusSymbolAssigned);
            matrix[symbolProbability.getRow()][symbolProbability.getColumn()] = symbol;
            if (symbol.equals(bonusSymbolCounters.keySet().stream().findFirst().orElse(null))) {
                bonusSymbolAssigned = true;
            }
        }
    }

    private static String assignSymbol(StandardSymbolProbability standardProbability, BonusSymbolProbability bonusProbability, Map<String, Integer> symbolCounters, Map<String, Integer> bonusSymbolCounters, boolean bonusSymbolAssigned) {
        if (!bonusSymbolAssigned && shouldAssignBonusSymbol()) {
            String bonusSymbol = generateBonusSymbol(bonusProbability);
            bonusSymbolCounters.merge(bonusSymbol, 1, Integer::sum);
            return bonusSymbol;
        } else {
            String normalSymbol = generateNormalSymbol(standardProbability);
            symbolCounters.merge(normalSymbol, 1, Integer::sum);
            return normalSymbol;
        }
    }


    private static boolean shouldAssignBonusSymbol() {
        return ThreadLocalRandom.current().nextInt(0, 10) < 1;
    }

    private static String generateBonusSymbol(BonusSymbolProbability bonusProbability) {
        Map<Integer, String> weightedSymbols = createWeightedSymbolMap(bonusProbability.getSymbols());
        return selectSymbolBasedOnProbability(weightedSymbols);
    }

    private static String generateNormalSymbol(StandardSymbolProbability standardProbability) {
        Map<Integer, String> weightedSymbols = createWeightedSymbolMap(standardProbability.getSymbols());
        return selectSymbolBasedOnProbability(weightedSymbols);
    }

    private static Map<Integer, String> createWeightedSymbolMap(Map<String, Integer> symbols) {
        return symbols.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    private static String selectSymbolBasedOnProbability(Map<Integer, String> probMap) {
        int totalWeight = probMap.keySet().stream().mapToInt(Integer::intValue).sum();
        int randomizedValue = ThreadLocalRandom.current().nextInt(1, totalWeight + 1);

        return probMap.entrySet().stream().filter(aboveRandomThreshold(randomizedValue)).findFirst().map(Map.Entry::getValue).orElseThrow(() -> new IllegalArgumentException(NO_SYMBOL_SELECTED));
    }


    private static Predicate<Map.Entry<Integer, String>> aboveRandomThreshold(int threshold) {
        final AtomicInteger cumulativeProbability = new AtomicInteger(0);
        return entry -> cumulativeProbability.addAndGet(entry.getKey()) >= threshold;
    }
}
