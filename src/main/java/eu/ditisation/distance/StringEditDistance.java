/*
 * Copyright (C) 2013 Universidad de Alicante
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package eu.ditisation.distance;

import eu.digitisation.ocr.ErrorMeasure;
import eu.digitisation.util.ArrayMath;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides basic implementations of some popular edit distance methods
 * operating on strings
 *
 * @version 2011.03.10
 */
public class StringEditDistance {

    /**
     * @return 3-wise minimum.
     */
    private static int min(int x, int y, int z) {
        return Math.min(x, Math.min(y, z));
    }

    /**
     * @param first the first string.
     * @param second the second string.
     * @return the indel distance between first and second.
     */
    public static int indel(String first, String second) {
        int i, j;
        int[][] A = new int[2][second.length() + 1];

        // Compute first row
        A[0][0] = 0;
        for (j = 1; j <= second.length(); ++j) {
            A[0][j] = A[0][j - 1] + 1;
        }

        // Compute other rows
        for (i = 1; i <= first.length(); ++i) {
            A[i % 2][0] = A[(i - 1) % 2][0] + 1;
            for (j = 1; j <= second.length(); ++j) {
                if (first.charAt(i - 1) == second.charAt(j - 1)) {
                    A[i % 2][j] = A[(i - 1) % 2][j - 1];
                } else {
                    A[i % 2][j] = Math.min(A[(i - 1) % 2][j] + 1,
                            A[i % 2][j - 1] + 1);
                }
            }
        }
        return A[first.length() % 2][second.length()];
    }

    /**
     * @param first the first string.
     * @param second the second string.
     * @return the Levenshtein distance between first and second.
     */
    public static int levenshtein(String first, String second) {
        int i, j;
        int[][] A;

        // intialize
        A = new int[2][second.length() + 1];

        // Compute first row
        A[0][0] = 0;
        for (j = 1; j <= second.length(); ++j) {
            A[0][j] = A[0][j - 1] + 1;
        }

        // Compute other rows
        for (i = 1; i <= first.length(); ++i) {
            A[i % 2][0] = A[(i - 1) % 2][0] + 1;
            for (j = 1; j <= second.length(); ++j) {
                if (first.charAt(i - 1) == second.charAt(j - 1)) {
                    A[i % 2][j] = A[(i - 1) % 2][j - 1];
                } else {
                    A[i % 2][j] = min(A[(i - 1) % 2][j] + 1,
                            A[i % 2][j - 1] + 1,
                            A[(i - 1) % 2][j - 1] + 1);
                }
            }
        }
        return A[first.length() % 2][second.length()];
    }

    /**
     * @param first the first string.
     * @param second the second string.
     * @return the minimal number of basic edit operations (insertions,
     * deletions, substitutions) required to transform first into second.
     */
    public static int[] operations(String first, String second) {
        int i, j;
        int[][][] A;

        // intialize
        A = new int[2][second.length() + 1][3];

        // Compute non-null elements in first row
        for (j = 1; j <= second.length(); ++j) {
            A[0][j][0] = A[0][j - 1][0] + 1;
        }

        // Compute other rows
        for (i = 1; i <= first.length(); ++i) {
            A[i % 2][0][2] = A[(i - 1) % 2][0][2] + 1;
            for (j = 1; j <= second.length(); ++j) {
                if (first.charAt(i - 1) == second.charAt(j - 1)) {
                    A[i % 2][j] = 
                            java.util.Arrays.copyOf(A[(i - 1) % 2][j - 1], 3);
                } else {
                    int ins = ArrayMath.sum(A[i % 2][j - 1]);
                    int del = ArrayMath.sum(A[(i - 1) % 2][j]);
                    int sub = ArrayMath.sum(A[(i - 1) % 2][j - 1]);
                    if (ins < Math.min(del, sub)) {
                        A[i % 2][j][0] = A[i % 2][j - 1][0] + 1;
                        A[i % 2][j][1] = A[i % 2][j - 1][1];
                        A[i % 2][j][2] = A[i % 2][j - 1][2];
                    } else if (del < sub) {
                        A[i % 2][j][0] = A[(i - 1) % 2][j][0];
                        A[i % 2][j][1] = A[(i - 1) % 2][j][1];
                        A[i % 2][j][2] = A[(i - 1) % 2][j][2] + 1;
                    } else {
                        A[i % 2][j][0] = A[(i - 1) % 2][j - 1][0];
                        A[i % 2][j][1] = A[(i - 1) % 2][j - 1][1] + 1;
                        A[i % 2][j][2] = A[(i - 1) % 2][j - 1][2];
                    }
                }
            }
        }
        return A[first.length() % 2][second.length()];
    }

    /**
     * Aligns two strings (one to one alignments).
     *
     * @param first the first string.
     * @param second the second string.
     * @return the mapping between positions.
     */
    public static int[] align(String first, String second) {
        int i, j;
        int[][] A;

        // intialize
        A = new int[first.length() + 1][second.length() + 1];

        // Compute first row
        A[0][0] = 0;
        for (j = 1; j <= second.length(); ++j) {
            A[0][j] = A[0][j - 1] + 1;
        }

        // Compute other rows
        for (i = 1; i <= first.length(); ++i) {
            A[i][0] = A[i - 1][0] + 1;
            for (j = 1; j <= second.length(); ++j) {
                if (first.charAt(i - 1) == second.charAt(j - 1)) {
                    A[i][j] = A[i - 1][j - 1];
                } else {
                    A[i][j] = min(A[i - 1][j] + 1, A[i][j - 1] + 1,
                            A[i - 1][j - 1] + 1);
                }
            }
        }
        int[] alignments = new int[first.length()];
        java.util.Arrays.fill(alignments, -1);
        i = first.length();
        j = second.length();
        while (i > 0 && j > 0) {
            if (A[i][j] == A[i - 1][j - 1]) {
                alignments[--i] = --j;
            } else if (A[i][j] == A[i - 1][j] + 1) {
                --i;
            } else if (A[i][j] == A[i][j - 1] + 1) {
                --j;
            } else if (A[i][j] == A[i - 1][j - 1] + 1) {
                --i;
                --j;
            } else { // remove after debugging
                Logger.getLogger(ErrorMeasure.class.getName())
                        .log(Level.SEVERE, null,
                                "Wrong code at StringEditDistance.alignments");
            }
        }

        return alignments;
    }
}