package com.ryansims.sudoku;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This program is a Sudoku Solver. If you get stuck while playing Sudoku, you can
 * enter your puzzle into the solver and the solver will give you the answers.
 *
 * @author Ryan Sims
 * @version November 16th 2018
 **/
public class SudokuSolver {

    public static void main(String args[]) {
        for (Map.Entry<String, String> puzzleEntry : PUZZLES.entrySet()) {
            String title = puzzleEntry.getKey();
            String puzzleAsString = puzzleEntry.getValue();
            int[][] puzzle = decodePuzzle(puzzleAsString);
            solveAndReportSolution(title, puzzle);
        }
    }

    private static void solveAndReportSolution(String title, int[][] puzzle) {

        System.out.printf("Solving puzzle (\"%s\"):\n", title);
        System.out.println(puzzleToPrettyString(puzzle));

        if (!isPuzzleValid(puzzle)) {
            System.out.printf("Input puzzle \"%s\" is not valid.\n", title);
            return;
        }

        AtomicLong numCycles = new AtomicLong(0);

        long start = System.currentTimeMillis();
        int[][] solvedPuzzle = solve(puzzle, numCycles);
        long end = System.currentTimeMillis();
        long durationInMillis = end - start;

        if (isComplete(solvedPuzzle)) {
            System.out.printf("Solved \"%s\" using %s cycle(s) in %s ms:\n",
                    title, HUMAN_READABLE_FORMATTER.format(numCycles), HUMAN_READABLE_FORMATTER.format(durationInMillis));
            System.out.println(puzzleToPrettyString(solvedPuzzle));
        } else {
            System.out.printf("Determined that \"%s\" has no solution using %s cycle(s) in %s ms:\n",
                    title, HUMAN_READABLE_FORMATTER.format(numCycles), HUMAN_READABLE_FORMATTER.format(durationInMillis));
        }
    }

    private static int[][] solve(int[][] puzzle, AtomicLong numCycles) {
        numCycles.incrementAndGet();
        int[] firstEmptySquareCoordinates = findCoordinatesOfFirstEmptySquare(puzzle);
        if (firstEmptySquareCoordinates == null) {
            return puzzle; // Puzzle is solved
        }
        int firstEmptyX = firstEmptySquareCoordinates[0];
        int firstEmptyY = firstEmptySquareCoordinates[1];
        Set<Integer> legalRowValues = getLegalRowValues(puzzle, firstEmptyY);
        Set<Integer> legalColumnValues = getLegalColumnValues(puzzle, firstEmptyX);
        Set<Integer> legalBoxValues = getLegalBoxValues(puzzle, firstEmptyX, firstEmptyY);

        List<Integer> legalValues = new ArrayList<>();
        for (Integer value : legalRowValues) {
            if (legalColumnValues.contains(value) && legalBoxValues.contains(value)) {
                legalValues.add(value);
            }
        }

        for (int legalValue : legalValues) {
            puzzle[firstEmptyY][firstEmptyX] = legalValue;
            int[][] solvedPuzzle = solve(puzzle, numCycles);
            if (isComplete(solvedPuzzle)) {
                return solvedPuzzle;
            }
        }
        // No remaining legal values and puzzle is not complete. Set our empty square back to zero since we are
        // modifying our data structure in place and need to return the original value back to the caller.
        puzzle[firstEmptyY][firstEmptyX] = UNSET_VALUE;

        return puzzle;
    }

    private static int[] findCoordinatesOfFirstEmptySquare(int[][] puzzle) {
        for (int y = 0; y < puzzle.length; y++) {
            int[] row = puzzle[y];
            for (int x = 0; x < row.length; x++) {
                int value = row[x];
                if (value == UNSET_VALUE) {
                    return new int[]{x, y};
                }
            }
        }
        return null;
    }

    private static String puzzleToPrettyString(int[][] puzzle) {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < puzzle.length; y++) {
            int[] row = puzzle[y];
            for (int x = 0; x < row.length; x++) {
                int value = row[x];
                if (value == UNSET_VALUE) {
                    sb.append(UNSET_VALUE_STRING);
                } else {
                    sb.append(value);
                }
                sb.append(" ");
                if (x == 2 || x == 5) {
                    sb.append("| ");
                }
            }
            sb.append("\n");
            if (y == 2 || y == 5) {
                sb.append(BOX_HORIZONTAL_DIVIDER + "\n");
            }
        }
        return sb.toString();
    }

    private static int[][] decodePuzzle(String puzzle) {
        List<String> strings = Arrays.asList(
                puzzle
                        .replaceAll("\\.", "0")
                        .replaceAll("[^0-9]", "")
                        .split(""));

        if (strings.size() != EXPECTED_NUMBER_OF_SQUARES) {
            throw new RuntimeException(
                    String.format("Puzzle should contain exactly %d values", EXPECTED_NUMBER_OF_SQUARES));
        }

        Iterator<String> valueIterator = strings.iterator();
        int[][] decodedPuzzle = new int[SUDOKU_DIMENSION_SIZE][SUDOKU_DIMENSION_SIZE];
        for (int y = 0; y < SUDOKU_DIMENSION_SIZE; y++) {
            for (int x = 0; x < SUDOKU_DIMENSION_SIZE; x++) {
                decodedPuzzle[y][x] = Integer.valueOf(valueIterator.next());
            }
        }
        return decodedPuzzle;
    }

    private static boolean isPuzzleValid(int[][] puzzle) {
        for (int y = 0; y < SUDOKU_DIMENSION_SIZE; y++) {
            for (int x = 0; x < SUDOKU_DIMENSION_SIZE; x++) {
                int[] containingRegion = getStartCoordinatesOfContainingRegion(x, y);
                int regionStartX = containingRegion[0];
                int regionStartY = containingRegion[1];
                if (!(isRowValid(puzzle[y]) &&
                        isColumnValid(puzzle, x) &&
                        isRegionValid(puzzle, regionStartX, regionStartY))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static Set<Integer> getAllowedValuesFromValueCounts(int[] valueCounts) {
        Set<Integer> allowedValues = new LinkedHashSet<>();
        for (int value = 1; value < valueCounts.length; value++) {
            int valueCount = valueCounts[value];
            if (valueCount == 0) {
                allowedValues.add(value);
            }
        }
        return allowedValues;
    }

    private static Set<Integer> getLegalRowValues(int[][] puzzle, int y) {
        int[] valueCounts = getValueCountsForRow(puzzle[y]);
        return getAllowedValuesFromValueCounts(valueCounts);
    }

    private static int[] getValueCountsForRow(int[] row) {
        int[] valueCounts = new int[VALUE_COUNT_ARRAY_SIZE];
        for (int value : row) {
            if (value != UNSET_VALUE) {
                valueCounts[value]++;
            }
        }
        return valueCounts;
    }

    private static Set<Integer> getLegalColumnValues(int[][] puzzle, int x) {
        int[] valueCounts = getValueCountsForColumn(puzzle, x);
        return getAllowedValuesFromValueCounts(valueCounts);
    }

    private static int[] getValueCountsForColumn(int[][] puzzle, int x) {
        int[] valueCounts = new int[VALUE_COUNT_ARRAY_SIZE];
        for (int[] row : puzzle) {
            int value = row[x];
            if (value != UNSET_VALUE) {
                valueCounts[value]++;
            }
        }
        return valueCounts;
    }

    private static Set<Integer> getLegalBoxValues(int[][] puzzle, int x, int y) {
        int[] valueCounts = getValueCountsForRegion(puzzle, x, y);
        return getAllowedValuesFromValueCounts(valueCounts);
    }

    private static int[] getValueCountsForRegion(int[][] puzzle, int regionStartX, int regionStartY) {
        int[] containingRegion = getStartCoordinatesOfContainingRegion(regionStartX, regionStartY);
        regionStartX = containingRegion[0];
        regionStartY = containingRegion[1];
        int[] valueCounts = new int[VALUE_COUNT_ARRAY_SIZE];
        for (int y = 0; y < REGION_SIZE; y++) {
            int[] row = puzzle[regionStartY + y];
            for (int x = 0; x < REGION_SIZE; x++) {
                int value = row[regionStartX + x];
                if (value != UNSET_VALUE) {
                    valueCounts[value]++;
                }
            }
        }
        return valueCounts;
    }

    private static boolean isRowValid(int[] row) {
        return isUnitValid(getValueCountsForRow(row));
    }

    private static boolean isUnitValid(int[] valueCounts) {
        for (int valueCount : valueCounts) {
            if (valueCount > 1) {
                return false;
            }
        }
        return true;
    }

    private static boolean isColumnValid(int[][] puzzle, int columnOffset) {
        return isUnitValid(getValueCountsForColumn(puzzle, columnOffset));
    }

    private static int[] getStartCoordinatesOfContainingRegion(int x, int y) {
        return new int[]{x - (x % REGION_SIZE), y - (y % REGION_SIZE)};
    }

    private static boolean isRegionValid(int[][] puzzle, int columnOffset, int rowOffset) {
        return isUnitValid(getValueCountsForRegion(puzzle, columnOffset, rowOffset));
    }

    private static boolean isComplete(int[][] puzzle) {
        return findCoordinatesOfFirstEmptySquare(puzzle) == null;
    }

    private static final DecimalFormat HUMAN_READABLE_FORMATTER = new DecimalFormat("#,###");
    private static final int EXPECTED_NUMBER_OF_SQUARES = 81;
    private static final int SUDOKU_DIMENSION_SIZE = 9;
    private static final int REGION_SIZE = 3;
    private static final int UNSET_VALUE = 0;
    private static final int VALUE_COUNT_ARRAY_SIZE = 10;
    private static final String UNSET_VALUE_STRING = ".";
    private static final String BOX_HORIZONTAL_DIVIDER = "------+-------+-------";

    private static final Map<String, String> PUZZLES = new HashMap<>();

    static {
        PUZZLES.put(
                // https://projecteuler.net/index.php?section=problems&id=96
                "Already solved", "" +
                        "4 8 3 | 9 2 1 | 6 5 7 " +
                        "9 6 7 | 3 4 5 | 8 2 1 " +
                        "2 5 1 | 8 7 6 | 4 9 3 " +
                        "------+-------+-------" +
                        "5 4 8 | 1 3 2 | 9 7 6 " +
                        "7 2 9 | 5 6 4 | 1 3 8 " +
                        "1 3 6 | 7 9 8 | 2 4 5 " +
                        "------+-------+-------" +
                        "3 7 2 | 6 8 9 | 5 1 4 " +
                        "8 1 4 | 2 5 3 | 7 6 9 " +
                        "6 9 5 | 4 1 7 | 3 8 2 ");
        PUZZLES.put(
                "One square empty", "" +
                        ". 8 3 | 9 2 1 | 6 5 7 " +
                        "9 6 7 | 3 4 5 | 8 2 1 " +
                        "2 5 1 | 8 7 6 | 4 9 3 " +
                        "------+-------+-------" +
                        "5 4 8 | 1 3 2 | 9 7 6 " +
                        "7 2 9 | 5 6 4 | 1 3 8 " +
                        "1 3 6 | 7 9 8 | 2 4 5 " +
                        "------+-------+-------" +
                        "3 7 2 | 6 8 9 | 5 1 4 " +
                        "8 1 4 | 2 5 3 | 7 6 9 " +
                        "6 9 5 | 4 1 7 | 3 8 2 ");

        PUZZLES.put(
                "Two squares empty", "" +
                        "4 . . | 9 2 1 | 6 5 7 " +
                        "9 6 7 | 3 4 5 | 8 2 1 " +
                        "2 5 1 | 8 7 6 | 4 9 3 " +
                        "------+-------+-------" +
                        "5 4 8 | 1 3 2 | 9 7 6 " +
                        "7 2 9 | 5 6 4 | 1 3 8 " +
                        "1 3 6 | 7 9 8 | 2 4 5 " +
                        "------+-------+-------" +
                        "3 7 2 | 6 8 9 | 5 1 4 " +
                        "8 1 4 | 2 5 3 | 7 6 9 " +
                        "6 9 5 | 4 1 7 | 3 8 2 ");

        PUZZLES.put(
                // http://sudopedia.enjoysudoku.com/Invalid_Test_Cases.html
                "Invalid (Box 5 contains duplicate values)", "" +
                        ". . 9 | . 7 . | . . 5 " +
                        ". . 2 | 1 . . | 9 . . " +
                        "1 . . | . 2 8 | . . . " +
                        "------+-------+-------" +
                        ". 7 . | . . 5 | . . 1 " +
                        ". . 8 | 5 1 . | . . . " +
                        ". 5 . | . . . | 3 . . " +
                        "------+-------+-------" +
                        ". . . | . . 3 | . . 6 " +
                        "8 . . | . . . | . . . " +
                        "2 1 . | . . . | . 8 7 ");

        PUZZLES.put(
                "No solution", "" +
                        ". . 9 | . 2 8 | 7 . . " +
                        "8 . 6 | . . 4 | . . 5 " +
                        ". . 3 | . . . | . . 4 " +
                        "------+-------+-------" +
                        "6 . . | . . . | . . . " +
                        ". 2 . | 7 1 3 | 4 5 . " +
                        ". . . | . . . | . . 2 " +
                        "------+-------+-------" +
                        "3 . . | . . . | 5 . . " +
                        "9 . . | 4 . . | 8 . 7 " +
                        ". . 1 | 2 5 . | 3 . . ");

        PUZZLES.put(
                "Unnamed 1", "" +
                        "3 . 6 | 5 . 8 | 4 . . " +
                        "5 2 . | . . . | . . . " +
                        ". 8 7 | . . . | . 3 1 " +
                        "------+-------+-------" +
                        ". . 3 | . 1 . | . 8 . " +
                        "9 . . | 8 6 3 | . . 5 " +
                        ". 5 . | . 9 . | 6 . . " +
                        "------+-------+-------" +
                        "1 3 . | . . . | 2 5 . " +
                        ". . . | . . . | . 7 4 " +
                        ". . 5 | 2 . 6 | 3 . . ");

        PUZZLES.put(
                "Unnamed 2", "" +
                        ". . . | . . 6 | . . . " +
                        ". 5 9 | . . . | . . 8 " +
                        "2 . . | . . 8 | . . . " +
                        "------+-------+-------" +
                        ". 4 5 | . . . | . . . " +
                        ". . 3 | . . . | . . . " +
                        ". . 6 | . . 3 | . 5 4 " +
                        "------+-------+-------" +
                        ". . . | 3 2 5 | . . 6 " +
                        ". . . | . . . | . . . " +
                        ". . . | . . . | . . . ");
    }
}
