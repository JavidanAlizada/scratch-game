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

public final class MatrixGenerator {

    private MatrixGenerator() {
        throw new UnsupportedOperationException("This class cannot be instantiated.");
    }

    /**
     * Generates a 2D matrix representing the game's symbols based on the provided configuration.
     *
     * @param config The game configuration.
     * @return A 2D matrix filled with generated symbols.
     */
    public static String[][] generate(GameConfig config) {
        validateConfig(config);
        String[][] matrix = new String[config.getRows()][config.getColumns()];

        // Track how often each symbol appears
        Map<String, Integer> symbolCounters = new HashMap<>();
        Map<String, Integer> bonusSymbolCounters = new HashMap<>();

        boolean bonusSymbolAssigned = false;

        fillMatrixWithSymbols(config, matrix, symbolCounters, bonusSymbolCounters, bonusSymbolAssigned);

        return matrix;
    }

    /**
     * Validates the configuration for rows, columns, and symbol positions.
     *
     * @param config The game configuration.
     */
    private static void validateConfig(GameConfig config) {
        if (config.getRows() <= 0 || config.getColumns() <= 0) {
            throw new IllegalArgumentException("Rows and columns must be greater than zero.");
        }

        boolean invalidSymbolPosition = config.getProbabilities().getStandardSymbols()
                .stream()
                .anyMatch(probability -> isInvalidProbabilityRowsOrColumns(config, probability));

        if (invalidSymbolPosition) {
            throw new IllegalArgumentException("Invalid row or column specified in probabilities.");
        }
    }

    private static boolean isInvalidProbabilityRowsOrColumns(GameConfig config, StandardSymbolProbability probability) {
        return probability.getRow() < 0 || probability.getRow() >= config.getRows() ||
                probability.getColumn() < 0 || probability.getColumn() >= config.getColumns();
    }

    /**
     * Populates the matrix with symbols based on the game configuration and probabilities.
     *
     * @param config              The game configuration.
     * @param matrix              The matrix to be filled.
     * @param symbolCounters      A map to count occurrences of normal symbols.
     * @param bonusSymbolCounters A map to count occurrences of bonus symbols.
     * @param bonusSymbolAssigned  Track if a bonus symbol has been assigned.
     */
    private static void fillMatrixWithSymbols(GameConfig config,
                                              String[][] matrix,
                                              Map<String, Integer> symbolCounters,
                                              Map<String, Integer> bonusSymbolCounters,
                                              boolean bonusSymbolAssigned) {
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

    /**
     * Assigns a symbol to a given cell based on probabilities.
     *
     * @param standardProbability The standard symbol probability.
     * @param bonusProbability    The bonus symbol probability.
     * @param symbolCounters      A map to count occurrences of normal symbols.
     * @param bonusSymbolCounters A map to count occurrences of bonus symbols.
     * @param bonusSymbolAssigned  Track if a bonus symbol has been assigned.
     * @return The assigned symbol.
     */
    private static String assignSymbol(StandardSymbolProbability standardProbability,
                                       BonusSymbolProbability bonusProbability,
                                       Map<String, Integer> symbolCounters,
                                       Map<String, Integer> bonusSymbolCounters,
                                       boolean bonusSymbolAssigned) {
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

    /**
     * Determines if a bonus symbol should be assigned (~10% chance).
     *
     * @return True if a bonus symbol should be assigned, otherwise false.
     */
    private static boolean shouldAssignBonusSymbol() {
        return ThreadLocalRandom.current().nextInt(0, 10) < 1;
    }

    /**
     * Generates a bonus symbol based on weighted probabilities.
     *
     * @param bonusProbability The bonus symbol probability configuration.
     * @return The generated bonus symbol.
     */
    private static String generateBonusSymbol(BonusSymbolProbability bonusProbability) {
        Map<Integer, String> weightedSymbols = createWeightedSymbolMap(bonusProbability.getSymbols());
        return selectSymbolBasedOnProbability(weightedSymbols);
    }

    /**
     * Generates a normal symbol based on weighted probabilities.
     *
     * @param standardProbability The standard symbol probability configuration.
     * @return The generated normal symbol.
     */
    private static String generateNormalSymbol(StandardSymbolProbability standardProbability) {
        Map<Integer, String> weightedSymbols = createWeightedSymbolMap(standardProbability.getSymbols());
        return selectSymbolBasedOnProbability(weightedSymbols);
    }

    /**
     * Creates a map of probabilities and symbols.
     *
     * @param symbols The map of symbols with their respective probabilities.
     * @return A map where the key is the symbol probability and the value is the symbol.
     */
    private static Map<Integer, String> createWeightedSymbolMap(Map<String, Integer> symbols) {
        return symbols.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    /**
     * Selects a symbol based on weighted probabilities.
     *
     * @param probMap A map of symbol probabilities.
     * @return The selected symbol.
     */
    private static String selectSymbolBasedOnProbability(Map<Integer, String> probMap) {
        int totalWeight = probMap.keySet().stream().mapToInt(Integer::intValue).sum();
        int randomizedValue = ThreadLocalRandom.current().nextInt(1, totalWeight + 1);

        return probMap.entrySet().stream()
                .filter(aboveRandomThreshold(randomizedValue))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElseThrow(() -> new IllegalArgumentException("No symbol selected despite valid probabilities"));
    }

    /**
     * Creates a predicate to find the symbol matching the randomized threshold.
     *
     * @param threshold The threshold to match.
     * @return A predicate for stream filtering.
     */
    private static Predicate<Map.Entry<Integer, String>> aboveRandomThreshold(int threshold) {
        final AtomicInteger cumulativeProbability = new AtomicInteger(0);
        return entry -> cumulativeProbability.addAndGet(entry.getKey()) >= threshold;
    }
}
