# Scratch Game

## Overview

This project implements a scratch game that generates a matrix filled with standard and bonus symbols based on specified probabilities. User can place bets, and winnings are calculated based on predefined winning combinations.

## Requirements

- JDK version 1.8 or higher
- Maven for dependency management
- JSON serialization/deserialization library (e.g., Jackson or Gson)

## Features

- Generates a matrix (e.g., 3x3) from symbols based on probabilities for each cell.
- Calculates winnings based on standard symbols and their combinations.
- Applies bonus symbols to increase final rewards.
- Flexible betting amounts for user.

## Symbol Types

### Standard Symbols
The game uses standard symbols to determine winnings based on winning combinations. The following table summarizes the reward multipliers for standard symbols:

| Symbol Name | Reward Multiplier |
|-------------|-------------------|
| A           | 50                |
| B           | 25                |
| C           | 10                |
| D           | 5                 |
| E           | 3                 |
| F           | 1.5               |

### Bonus Symbols
Bonus symbols affect the final reward if at least one winning combination matches. The actions for bonus symbols are summarized below:

| Symbol Name | Action                       |
|-------------|------------------------------|
| 10x         | Multiply final reward by 10   |
| 5x          | Multiply final reward by 5    |
| +1000       | Add 1000 to the final reward  |
| +500        | Add 500 to the final reward    |

## Setup

1. Clone the repository:
   ```
   git clone <repository-url>
   ```
2. Navigate to the project directory:
   ```
   cd <project-directory>
   ```
3. Build the project using Maven
   ```
   mvn clean install
   ```

## Testing

To run tests, execute the following command:
   ```
   mvn test
   ```
## Command-Line Interface (CLI) Usage   
To run the game with specific configurations and betting amounts, use the following command:
   ```
   java -jar <your-jar-file> --config config.json --betting-amount 100
   ```

With real example: (assume that, you have config.json in Documents folder)
   ```
   java -jar target/scratch-game.jar --config "C:\Users\${YOUR_USER}\Documents\config.json" --betting-amount 100

   ```

### Parameters  
**config** : *Configuration file (JSON format) containing game settings, combinations, symbols and probabilities.*

**betting-amount** : *The amount of money to bet for the game.*

## Usage
- The user can place a bet by specifying the betting amount through the CLI.
- The game will generate a matrix of symbols based on the configured probabilities.
- The winnings will be calculated based on the generated matrix and applied combinations.