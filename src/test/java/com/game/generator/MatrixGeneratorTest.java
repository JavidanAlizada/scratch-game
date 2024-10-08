package com.game.generator;

import com.game.config.GameConfig;
import com.game.model.BonusSymbolProbability;
import com.game.model.Probability;
import com.game.model.StandardSymbolProbability;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.game.errors.MatrixGeneratorErrors.INVALID_ROW_OR_COLUMNS;
import static com.game.errors.MatrixGeneratorErrors.INVALID_SYMBOL_POSITION;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MatrixGeneratorTest {
    private GameConfig config;
    private Probability probability;
    private BonusSymbolProbability bonusSymbolProbability;

    @BeforeEach
    void setUp() {
        config = mock(GameConfig.class);
        probability = mock(Probability.class);
        StandardSymbolProbability standardSymbolProbability = mock(StandardSymbolProbability.class);
        bonusSymbolProbability = mock(BonusSymbolProbability.class);

        when(config.getRows()).thenReturn(3);
        when(config.getColumns()).thenReturn(3);
        when(config.getProbabilities()).thenReturn(probability);
    }

    @Test
    void testGenerate_ValidConfig_MatrixGenerated() {
        Map<String, Integer> standardSymbols = new HashMap<>();
        standardSymbols.put("A", 5);
        standardSymbols.put("B", 3);

        Map<String, Integer> bonusSymbols = new HashMap<>();
        bonusSymbols.put("BONUS", 1);

        List<StandardSymbolProbability> standardSymbolProbabilities = List.of(
                createStandardSymbolProbability(0, 0, standardSymbols),
                createStandardSymbolProbability(0, 1, standardSymbols),
                createStandardSymbolProbability(0, 2, standardSymbols),
                createStandardSymbolProbability(1, 0, standardSymbols),
                createStandardSymbolProbability(1, 1, standardSymbols),
                createStandardSymbolProbability(1, 2, standardSymbols),
                createStandardSymbolProbability(2, 0, standardSymbols),
                createStandardSymbolProbability(2, 1, standardSymbols),
                createStandardSymbolProbability(2, 2, standardSymbols)
        );

        when(probability.getStandardSymbols()).thenReturn(standardSymbolProbabilities);
        when(probability.getBonusSymbols()).thenReturn(bonusSymbolProbability);
        when(bonusSymbolProbability.getSymbols()).thenReturn(bonusSymbols);

        String[][] matrix = MatrixGenerator.generate(config);

        assertNotNull(matrix);
        assertEquals(3, matrix.length);
        assertEquals(3, matrix[0].length);

        for (String[] strings : matrix) {
            for (String string : strings) {
                assertTrue(standardSymbols.containsKey(string) || bonusSymbols.containsKey(string));
            }
        }
    }

    @Test
    void testGenerate_InvalidRowConfig_ThrowsException() {
        when(config.getRows()).thenReturn(-1);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            MatrixGenerator.generate(config);
        });

        assertEquals(INVALID_ROW_OR_COLUMNS, exception.getMessage());
    }

    @Test
    void testGenerate_InvalidColumnConfig_ThrowsException() {
        when(config.getColumns()).thenReturn(0);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> MatrixGenerator.generate(config));

        assertEquals(INVALID_ROW_OR_COLUMNS, exception.getMessage());
    }

    @Test
    void testGenerate_InvalidSymbolPosition_ThrowsException() {
        List<StandardSymbolProbability> invalidProbabilities = List.of(
                createStandardSymbolProbability(-1, 0, Collections.singletonMap("A", 1))
        );
        when(probability.getStandardSymbols()).thenReturn(invalidProbabilities);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> MatrixGenerator.generate(config));

        assertEquals(INVALID_SYMBOL_POSITION, exception.getMessage());
    }

    private StandardSymbolProbability createStandardSymbolProbability(int row, int column, Map<String, Integer> symbols) {
        StandardSymbolProbability standardSymbolProbability = mock(StandardSymbolProbability.class);
        when(standardSymbolProbability.getRow()).thenReturn(row);
        when(standardSymbolProbability.getColumn()).thenReturn(column);
        when(standardSymbolProbability.getSymbols()).thenReturn(symbols);
        return standardSymbolProbability;
    }

}
