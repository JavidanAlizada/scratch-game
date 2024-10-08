package com.game;

import com.game.calculator.WinningCombinationCalculator;
import com.game.config.ConfigLoader;
import com.game.config.GameConfig;
import com.game.generator.MatrixGenerator;
import com.game.model.GameResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class ScratchGame {
    private static final String CONFIG = "--config";
    private static final String BETTING_AMOUNT = "--betting-amount";

    public static void printMatrix(String[][] matrix) {
        if (matrix == null || matrix.length == 0) {
            System.out.println("The matrix is empty.");
            return;
        }

        for (String[] row : matrix) {
            System.out.print("\t\t[");
            for (int j = 0; j < row.length; j++) {
                System.out.print(row[j]);
                if (j < row.length - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println("]");
        }
    }

    public static void main(String[] args) throws Exception {
        var argsMap = readArgs(args);

        var bettingAmount = argsMap.get(BETTING_AMOUNT);
        var configFilePath = argsMap.get(CONFIG);

        GameConfig gameConfig = ConfigLoader.loadConfig(configFilePath);
        String[][] matrix = MatrixGenerator.generate(gameConfig);

        WinningCombinationCalculator calculator = new WinningCombinationCalculator();

        GameResult gameResult = calculator.calculateWinningsCombination(matrix, Double.parseDouble(bettingAmount), gameConfig);

        print(gameResult);
    }

    static void print(GameResult gameResult) {
        System.out.println("{");
        System.out.println("\t" + withinDoubleQuote("matrix") + "[");
        printMatrix(gameResult.getMatrix());
        System.out.print("\t],\n");
        System.out.println("\t" + withinDoubleQuote("reward") + BigDecimal.valueOf(gameResult.getTotalReward()).setScale(2, RoundingMode.CEILING).toPlainString() + ",");
        System.out.println("\t" + withinDoubleQuote("applied_winning_combinations") + gameResult.getAppliedCombinations() + ",");
        System.out.println("\t" + withinDoubleQuote("applied_bonus_symbol") + gameResult.getAppliedBonusSymbol());
        System.out.println("}");
    }

    static String withinDoubleQuote(String value) {
        return String.format("\"%s\":", value);
    }

    static Map<String, String> readArgs(String[] args) {
        String configFilePath = null;
        int bettingAmount = 0;

        for (int i = 0; i < args.length; i++) {
            if (CONFIG.equals(args[i]) && i + 1 < args.length) {
                configFilePath = args[++i];
            } else if (BETTING_AMOUNT.equals(args[i]) && i + 1 < args.length) {
                try {
                    bettingAmount = Integer.parseInt(args[++i]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid betting amount: " + args[i]);
                    throw new IllegalArgumentException("Invalid betting amount: " + args[i]);
                }
            }
        }

        if (configFilePath == null || bettingAmount <= 0) {
            System.err.println("Usage: java -jar <your-jar-file> --config <config-file-path> --betting-amount <amount>");
            throw new IllegalArgumentException("Usage: java -jar <your-jar-file> --config <config-file-path> --betting-amount <amount>");
        }

        return Map.of(CONFIG, configFilePath, BETTING_AMOUNT, String.valueOf(bettingAmount));
    }
}