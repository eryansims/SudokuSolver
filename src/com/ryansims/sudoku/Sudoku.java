package com.ryansims.sudoku;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * This program is a Sudoku Solver. If you get stuck while playing Sudoku, you can
 * enter your puzzle into the solver and the solver will give you the answers.
 *
 * @author Ryan Sims
 * @version November 16th 2018
 **/
public class Sudoku {

    private static final int[][] validRegionOffsets = new int[][]{
            {0, 0}, {3, 0}, {6, 0}, {0, 3}, {3, 3}, {6, 3}, {0, 6}, {3, 6}, {6, 6}};

    // https://projecteuler.net/index.php?section=problems&id=96
    public static final String completeValidPuzzle
            = "4 8 3 | 9 2 1 | 6 5 7 \n" +
              "9 6 7 | 3 4 5 | 8 2 1 \n" +
              "2 5 1 | 8 7 6 | 4 9 3 \n" +
              "------+-------+-------\n" +
              "5 4 8 | 1 3 2 | 9 7 6 \n" +
              "7 2 9 | 5 6 4 | 1 3 8 \n" +
              "1 3 6 | 7 9 8 | 2 4 5 \n" +
              "------+-------+-------\n" +
              "3 7 2 | 6 8 9 | 5 1 4 \n" +
              "8 1 4 | 2 5 3 | 7 6 9 \n" +
              "6 9 5 | 4 1 7 | 3 8 2 \n";

    public static final String nearlyCompleteValidPuzzle
            = ". 8 3 | 9 2 1 | 6 5 7 \n" +
              "9 6 7 | 3 4 5 | 8 2 1 \n" +
              "2 5 1 | 8 7 6 | 4 9 3 \n" +
              "------+-------+-------\n" +
              "5 4 8 | 1 3 2 | 9 7 6 \n" +
              "7 2 9 | 5 6 4 | 1 3 8 \n" +
              "1 3 6 | 7 9 8 | 2 4 5 \n" +
              "------+-------+-------\n" +
              "3 7 2 | 6 8 9 | 5 1 4 \n" +
              "8 1 4 | 2 5 3 | 7 6 9 \n" +
              "6 9 5 | 4 1 7 | 3 8 2 \n";


    // http://sudopedia.enjoysudoku.com/Invalid_Test_Cases.html
    private static final String unsolvableDueToBox
            = ". . 9 | . 7 . | . . 5 \n" +
              ". . 2 | 1 . . | 9 . . \n" +
              "1 . . | . 2 8 | . . . \n" +
              "------+-------+-------\n" +
              ". 7 . | . . 5 | . . 1 \n" +
              ". . 8 | 5 1 . | . . . \n" +
              ". 5 . | . . . | 3 . . \n" +
              "------+-------+-------\n" +
              ". . . | . . 3 | . . 6 \n" +
              "8 . . | . . . | . . . \n" +
              "2 1 . | . . . | . 8 7 \n";

    public static final String ryansPuzzle
            = "3 . 6 | 5 . 8 | 4 . . \n" +
              "5 2 . | . . . | . . . \n" +
              ". 8 7 | . . . | . 3 1 \n" +
              "------+-------+-------\n" +
              ". . 3 | . 1 . | . 8 . \n" +
              "9 . . | 8 6 3 | . . 5 \n" +
              ". 5 . | . 9 . | 6 . . \n" +
              "------+-------+-------\n" +
              "1 3 . | . . . | 2 5 . \n" +
              ". . . | . . . | . 7 4 \n" +
              ". . 5 | 2 . 6 | 3 . . \n";


    public static void main(String args[]) {
        int[][] decoded = decodePuzzle(nearlyCompleteValidPuzzle);
        System.out.println("Input puzzle is:\n");
        System.out.println(puzzleToPrettyString(decoded));
        System.out.println("Input puzzle isComplete " + isComplete(decoded));
        System.out.println("Input puzzle isValid " + isValid(decoded));
        try {
            int[][] solved = solve(decoded);
            System.out.println("Solved puzzle:\n");
            System.out.println(puzzleToPrettyString(solved));
        } catch (Exception e) {
            System.err.println("Puzzle is unsolvable");
        }
    }

    private static int[][] solve(int[][] puzzle) {
        if (!isValid(puzzle)) {
            throw new RuntimeException();
        }
        int[] firstEmptyOffset = findFirstEmptyOffset(puzzle);
        if (firstEmptyOffset == null) {
            return puzzle; // Puzzle is solved
        }
        throw new RuntimeException();
    }

    private static int[] findFirstEmptyOffset(int[][] puzzle) {
        for (int i = 0; i < puzzle.length; i++) {
            int[] row = puzzle[i];
            for (int j = 0; j < row.length; j++) {
                int value = row[j];
                if (value == 0) {
                    return new int[]{j, i};
                }
            }
        }
        return null;
    }

    private static String puzzleToPrettyString(int[][] puzzle) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < puzzle.length; i++) {
            int[] row = puzzle[i];
            for (int j = 0; j < row.length; j++) {
                int value = row[j];
                if (value == 0) {
                    sb.append(".");
                } else {
                    sb.append(value);
                }
                sb.append(" ");
                if (j == 2 || j == 5) {
                    sb.append("| ");
                }
            }
            sb.append("\n");
            if (i == 2 || i == 5) {
                sb.append("------+-------+-------\n");
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

        if (strings.size() != 81) {
            throw new RuntimeException();
        }
        Iterator<String> valueIterator = strings.iterator();
        int[][] decoded = new int[9][9];
        for (int row = 0; row < 9; row++) {
            for (int column = 0; column < 9; column++) {
                decoded[row][column] = Integer.valueOf(valueIterator.next());
            }
        }
        return decoded;
    }

    private static boolean isValid(int[][] puzzle) {
        for (int rowOffset = 0; rowOffset < 9; rowOffset++) {
            for (int columnOffset = 0; columnOffset < 9; columnOffset++) {
                int[] containingRegion = getContainingRegion(columnOffset, rowOffset);
                if (!(isRowValid(puzzle[rowOffset]) &&
                      isColumnValid(puzzle, columnOffset) &&
                      isRegionValid(puzzle, containingRegion[0], containingRegion[1]))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isRowValid(int[] row) {
        boolean[] seen = new boolean[10];
        for (int value : row) {
            if (value == 0) {
                continue;
            }
            if (seen[value]) {
                System.out.println("isRowValid: But I've seen " + value);
                return false;
            }
            seen[value] = true;
        }
        return true;
    }

    private static boolean isColumnValid(int[][] puzzle, int columnOffset) {
        boolean[] seen = new boolean[10];
        for (int[] row : puzzle) {
            int value = row[columnOffset];
            if (value == 0) {
                continue;
            }
            if (seen[value]) {
                System.out.println("isColumnValid[" + columnOffset + "]: But I've seen " + value);
                return false;
            }
            seen[value] = true;
        }
        return true;
    }

    private static int[] getContainingRegion(int columnOffset, int rowOffset) {
        return new int[]{
                columnOffset - (columnOffset % 3),
                rowOffset - (rowOffset % 3)
        };
    }

    private static boolean isRegionValid(int[][] puzzle, int columnOffset, int rowOffset) {
        if (!areRegionOffsetsValid(columnOffset, rowOffset)) {
            throw new RuntimeException();
        }
        boolean[] seen = new boolean[10];
        for (int i = 0; i < 3; i++) {
            int[] row = puzzle[rowOffset + i];
            for (int j = 0; j < 3; j++) {
                int value = row[columnOffset + j];
                if (value == 0) {
                    continue;
                }
                if (seen[value]) {
                    System.out.println("isRegionValid[" + columnOffset + ", " + rowOffset + "]: But I've seen " + value);
                    return false;
                }
                seen[value] = true;
            }
        }
        return true;
    }

    private static boolean areRegionOffsetsValid(int columnOffset, int rowOffset) {
        for (int[] regionOffset : validRegionOffsets) {
            if (regionOffset[0] == columnOffset && regionOffset[1] == rowOffset) {
                return true;
            }
        }
        return false;
    }

    private static boolean isComplete(int[][] puzzle) {
        return findFirstEmptyOffset(puzzle) == null;
    }
}
