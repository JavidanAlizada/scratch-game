package com.game.calculator;

import com.game.config.GameConfig;
import com.game.model.GameResult;
import com.game.model.Symbol;
import com.game.model.WinCombination;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class WinningCombinationCalculatorTest {

    private WinningCombinationCalculator calculator;
    private GameConfig gameConfig;

    @BeforeEach
    void setUp() {
        calculator = new WinningCombinationCalculator();
        gameConfig = new GameConfig();
        setUpGameConfig();
    }

    @Test
    void testCalculateWinningsCombination_WithWinningCombinations() {
        // Arrange
        String[][] matrix = {
                {"A", "B", "C"},
                {"A", "A", "A"},
                {"C", "A", "B"}
        };
        double bettingAmount = 10.0;

        GameResult result = calculator.calculateWinningsCombination(matrix, bettingAmount, gameConfig);

        assertNotNull(result);
        assertEquals(100.0, result.getTotalReward());
        assertTrue(result.getAppliedCombinations().containsKey("A"));
        assertEquals(1, result.getAppliedCombinations().get("A").size());
    }

    @Test
    void testCalculateWinningsCombination_NoWinningCombinations() {
        String[][] matrix = {
                {"B", "B", "F"},
                {"E", "F", "D"},
                {"A", "E", "A"}
        };
        double bettingAmount = 10.0;

        GameResult result = calculator.calculateWinningsCombination(matrix, bettingAmount, gameConfig);

        assertNotNull(result);
        assertTrue(result.getAppliedCombinations().isEmpty());
    }

    @Test
    void testCalculateWinningsCombination_WithBonusSymbol() {
        String[][] matrix = {
                {"B", "B", "C"},
                {"C", "BONUS", "B"},
                {"B", "C", "B"}
        };
        double bettingAmount = 10.0;

        GameResult result = calculator.calculateWinningsCombination(matrix, bettingAmount, gameConfig);

        assertNotNull(result);
        assertNotNull(result.getAppliedBonusSymbol());
        assertEquals("BONUS", result.getAppliedBonusSymbol());
    }

    private void setUpGameConfig() {
        Map<String, WinCombination> winCombinations = new HashMap<>();

        WinCombination winCombination = new WinCombination();
        winCombination.setCount(3);
        winCombination.setWhen("same_symbols");
        winCombination.setRewardMultiplier(10.0);
        winCombinations.put("A", winCombination);

        gameConfig.setWinCombinations(winCombinations);

        Map<String, Symbol> symbolMap = new HashMap<>();

        Symbol symbolA = new Symbol();
        symbolA.setType("standard");
        symbolA.setRewardMultiplier(1.0);
        symbolMap.put("A", symbolA);

        Symbol symbolB = new Symbol();
        symbolB.setType("standard");
        symbolB.setRewardMultiplier(1.2);
        symbolMap.put("B", symbolB);

        Symbol symbolC = new Symbol();
        symbolC.setType("standard");
        symbolC.setRewardMultiplier(1.3);
        symbolMap.put("C", symbolC);

        Symbol bonusSymbol = new Symbol();
        bonusSymbol.setType("bonus");
        bonusSymbol.setRewardMultiplier(2.0);
        bonusSymbol.setExtra(1000D);
        symbolMap.put("BONUS", bonusSymbol);

        gameConfig.setSymbolMap(symbolMap);
    }
}
