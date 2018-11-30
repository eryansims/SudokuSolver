package com.ryansims.sudoku;

/**
 * This program is a Sudoku Solver. If you get stuck while playing Sudoku, you can
 * enter you’re puzzle into
 * the solver and the solver will give you the answers.
 *
 * @author Ryan Sims
 * @version November 16th 2018
 **/
public class Sudoku {

    private int[][] puzzle;

    private Sudoku(int[][] puzzle) {
        this.puzzle = puzzle;
    }

    public static void main(String args[]) {
        new Sudoku(new int[][]{
                {3, 0, 6, 5, 0, 8, 4, 0, 0},
                {5, 2, 0, 0, 0, 0, 0, 0, 0},
                {0, 8, 7, 0, 0, 0, 0, 3, 1},
                {0, 0, 3, 0, 1, 0, 0, 8, 0},
                {9, 0, 0, 8, 6, 3, 0, 0, 5},
                {0, 5, 0, 0, 9, 0, 6, 0, 0},
                {1, 3, 0, 0, 0, 0, 2, 5, 0},
                {0, 0, 0, 0, 0, 0, 0, 7, 4},
                {0, 0, 5, 2, 0, 6, 3, 0, 0}
        }).solve();
    }

    private void solve() {

    }
}
