package com.ryansims.sudoku;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This program is a Sudoku Solver. If you get stuck while playing Sudoku, you can
 * enter your puzzle into the solver and the solver will give you the answers.
 *
 * @author Ryan Sims
 * @version November 16th 2018
 **/
@SuppressWarnings("SpellCheckingInspection")
public class Sudoku {

    private static final int[][] validRegionOffsets = new int[][]{
            {0, 0}, {3, 0}, {6, 0}, {0, 3}, {3, 3}, {6, 3}, {0, 6}, {3, 6}, {6, 6}};

    @SuppressWarnings("unused")
    private static final String impossible
            = ". . . |. . 5 |. 8 . \n" +
              ". . . |6 . 1 |. 4 3 \n" +
              ". . . |. . . |. . . \n" +
              "------+------+------\n" +
              ". 1 . |5 . . |. . . \n" +
              ". . . |1 . 6 |. . . \n" +
              "3 . . |. . . |. . 5 \n" +
              "------+------+------\n" +
              "5 3 . |. . . |. 6 1 \n" +
              ". . . |. . . |. . 4 \n" +
              ". . . |. . . |. . .\n";
    private static final String hardForNorvig
            = ".....6....59.....82....8....45........3........6..3.54...325..6..................";
    private static final String hardestEverForHumans
            = "..53.....\n" +
              "8......2.\n" +
              ".7..1.5..\n" +
              "4....53..\n" +
              ".1..7...6\n" +
              "..32...8.\n" +
              ".6.5....9\n" +
              "..4....3.\n" +
              ".....97..\n";

    // https://projecteuler.net/index.php?section=problems&id=96
    private static final String completeValidPuzzle
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

    private static final String nearlyNearlyCompleteValidPuzzle
            = "4 . . | 9 2 1 | 6 5 7 \n" +
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

    private static final String nearlyCompleteValidPuzzle
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

    private static final String ryansPuzzle
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

    private static final String noSolution
            = ". . 9 | . 2 8 | 7 . .\n" +
              "8 . 6 | . . 4 | . . 5\n" +
              ". . 3 | . . . | . . 4\n" +
              "------+-------+------\n" +
              "6 . . | . . . | . . .\n" +
              ". 2 . | 7 1 3 | 4 5 .\n" +
              ". . . | . . . | . . 2\n" +
              "------+-------+------\n" +
              "3 . . | . . . | 5 . .\n" +
              "9 . . | 4 . . | 8 . 7\n" +
              ". . 1 | 2 5 . | 3 . .\n";

    public static void main(String args[]) {
        processPuzzle(unsolvableDueToBox);
        System.out.println("--------------\n\n");
        processPuzzle(noSolution);
        System.out.println("--------------\n\n");
        processPuzzle(completeValidPuzzle);
        System.out.println("--------------\n\n");
        processPuzzle(nearlyCompleteValidPuzzle);
        System.out.println("--------------\n\n");
        processPuzzle(ryansPuzzle);
        System.out.println("--------------\n\n");
        processPuzzle(nearlyNearlyCompleteValidPuzzle);
        System.out.println("--------------\n\n");
        processPuzzle(hardForNorvig);
        System.out.println("--------------\n\n");
        processPuzzle(hardestEverForHumans);
        System.out.println("--------------\n\n");
//        processPuzzle(impossible);
//        System.out.println("--------------\n\n");
    }

    private static void processPuzzle(String puzzle) {
        int[][] decoded = decodePuzzle(puzzle);
        System.out.println("Input puzzle is:\n");
        System.out.println(puzzleToPrettyString(decoded));
        if (isValid(decoded)) {
            AtomicInteger cycles = new AtomicInteger(0);
            int[][] solved = solve(decoded, cycles);
            if (isComplete(solved)) {
                System.out.println("Solved puzzle in " + cycles + " cycle(s):\n");
                System.out.println(puzzleToPrettyString(solved));
            } else {
                System.out.println("I can't solve that");
            }
        } else {
            System.out.println("Input puzzle is not valid");
        }
    }

    private static int[][] solve(int[][] puzzle, AtomicInteger cycles) {
        cycles.incrementAndGet();
        int[] firstEmptyOffset = findFirstEmptyOffset(puzzle);
        if (firstEmptyOffset == null) {
            return puzzle; // Puzzle is solved
        }
        int rowOffset = firstEmptyOffset[1];
        int columnOffset = firstEmptyOffset[0];
        //System.out.println("rowOffset: " + rowOffset + ", columnOffset: " + columnOffset);
        Set<Integer> rowValues = getValuesAllowedByRow(puzzle, rowOffset);
        Set<Integer> columnValues = getValuesAllowedByColumn(puzzle, columnOffset);
        Set<Integer> regionValues = getValuesAllowedByRegion(puzzle, columnOffset, rowOffset);

        List<Integer> allowedValues = new ArrayList<Integer>();
        for (Integer value : rowValues) {
            if (columnValues.contains(value) && regionValues.contains(value)) {
                allowedValues.add(value);
            }
        }
        for (int allowedValue : allowedValues) {
            //System.out.println("allowedValue: " + allowedValue);
            puzzle[rowOffset][columnOffset] = allowedValue;
            //System.out.println(puzzleToPrettyString(puzzle));
            int[][] solve = solve(puzzle, cycles);
            if (isComplete(solve)) {
                return solve;
            }
        }
        puzzle[rowOffset][columnOffset] = 0;
        return puzzle;
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

    private static Set<Integer> getAllowedValuesFromSeenArray(int[] seen) {
        Set<Integer> allowedValues = new LinkedHashSet<Integer>();
        for (int i = 1; i < seen.length; i++) {
            int value = seen[i];
            if (value > 0) {
                allowedValues.add(i);
            }
        }
        return allowedValues;
    }

    private static Set<Integer> getValuesAllowedByRow(int[][] puzzle, int rowOffset) {
        int[] seen = getValueCountsForRow(puzzle[rowOffset]);
        return getAllowedValuesFromSeenArray(seen);
    }

    private static int[] getValueCountsForRow(int[] row) {
        int[] seen = new int[10];
        for (int value : row) {
            if (value != 0) {
                seen[value]++;
            }
        }
        return seen;
    }

    private static Set<Integer> getValuesAllowedByColumn(int[][] puzzle, int columnOffset) {
        int[] seen = getValueCountsForColumn(puzzle, columnOffset);
        return getAllowedValuesFromSeenArray(seen);
    }

    private static int[] getValueCountsForColumn(int[][] puzzle, int columnOffset) {
        int[] seen = new int[10];
        for (int[] row : puzzle) {
            int value = row[columnOffset];
            if (value != 0) {
                seen[value]++;
            }
        }
        return seen;
    }

    private static Set<Integer> getValuesAllowedByRegion(int[][] puzzle, int columnOffset, int rowOffset) {
        int[] seen = getValueCountsForRegion(puzzle, columnOffset, rowOffset);
        return getAllowedValuesFromSeenArray(seen);
    }

    private static int[] getValueCountsForRegion(int[][] puzzle, int columnOffset, int rowOffset) {
        int[] containingRegion = getContainingRegion(columnOffset, rowOffset);
        columnOffset = containingRegion[0];
        rowOffset = containingRegion[1];
        int[] seen = new int[10];
        for (int i = 0; i < 3; i++) {
            int[] row = puzzle[rowOffset + i];
            for (int j = 0; j < 3; j++) {
                int value = row[columnOffset + j];
                if (value != 0) {
                    seen[value]++;
                }
            }
        }
        return seen;
    }

    private static boolean isRowValid(int[] row) {
        int[] valueCounts = getValueCountsForRow(row);
        return isUnitValid(valueCounts);
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
        return isUnitValid(getValueCountsForRegion(puzzle, columnOffset, rowOffset));
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
