/*
 * Copyright 2019-2019 Gryphon Zone
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zone.gryphon.sudoku.model;

import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @author tyrol
 */
@Slf4j
@Value
public class Grid {

    private static final int LENGTH = 9;

    private static final Set<Byte> values = new HashSet<>(Arrays.asList((byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 6, (byte) 7, (byte) 8, (byte) 9));

    public static Grid from(@NonNull byte[][] raw) {

        if (raw.length != LENGTH) {
            throw new IllegalArgumentException(String.format("Must have %d rows", LENGTH));
        }

        Square[][] array = new Square[LENGTH][LENGTH];

        for (int i = 0; i < raw.length; i++) {
            if (raw[i] == null) {
                throw new IllegalArgumentException("Row " + i + " is null!");
            }

            if (raw[i].length != LENGTH) {
                throw new IllegalArgumentException(String.format("Row %d has %d columns instead of %d!", i, raw[i].length, LENGTH));
            }

            byte[] row = raw[i];

            for (int j = 0; j < row.length; j++) {
                array[i][j] = row[j] == 0 ? null : new Square(Collections.singleton(row[j]));
            }
        }

        return new Grid(array);
    }

    private final Square[][] array;

    private Grid(@NonNull Square[][] array) {
        this.array = array;

        if (array.length != LENGTH) {
            throw new IllegalArgumentException(String.format("Must have %d rows, got %d", LENGTH, array.length));
        }

        for (int i = 0; i < array.length; i++) {
            Square[] row = array[i];

            if (row == null) {
                throw new IllegalArgumentException("Row " + i + " is null!");
            }

            if (row.length != LENGTH) {
                throw new IllegalArgumentException(String.format("Row %d has %d columns instead of %d!", i, array[i].length, LENGTH));
            }
        }
    }

    public Grid solve() {

        // simple case
        if (isSolved()) {
            return this;
        }

        Grid previousIteration;
        Grid updated = calculatePossibilities();

        do {
            previousIteration = updated;
            updated = updated.calculatePossibilities();
        } while (!Objects.equals(previousIteration, updated));

        // updated now contains the best estimate we can get just by deductive reasoning
        // check if it's a valid solution, and return it if it is
        if (updated.isSolved()) {
            return updated;
        }

        // if it's not, iterate through each of the squares which can contain multiple possible values, and
        // check to see if we assume one of the values is right, if the puzzle is solvable.
        // repeat for all possible values in the squares which contain more than one possible value
        for (int i = 0; i < updated.array.length; i++) {
            Square[] row = updated.array[i];

            for (int j = 0; j < row.length; j++) {
                Square col = row[j];

                if (col.getPossibleValues().size() > 1) {
                    for (Byte value : col.getPossibleValues()) {

                        Square[][] copy = new Square[updated.array.length][row.length];

                        for (int x = 0; x < updated.array.length; x++) {
                            System.arraycopy(updated.array[x], 0, copy[x], 0, updated.array[x].length);
                        }

                        copy[i][j] = new Square(Collections.singleton(value));

                        Grid grid = new Grid(copy);

                        try {
                            return grid.solve();
                        } catch (Exception e) {
                            log.info("Tried invalid value: {}: {}", e.getClass().getSimpleName(), e.getMessage());
                        }

                    }
                }
            }
        }

        throw new IllegalStateException("Unable to find valid solution!");
    }

    private boolean isSolved() {
        for (Square[] squares : this.array) {
            for (Square square : squares) {
                if (square.getPossibleValues().size() != 1) {
                    return false;
                }
            }
        }
        return true;
    }

    private Grid calculatePossibilities() {
        Square[][] newArray = new Square[9][9];


        for (int i = 0; i < array.length; i++) {
            System.arraycopy(array[i], 0, newArray[i], 0, array[i].length);
        }


        for (int i = 0; i < newArray.length; i++) {
            for (int j = 0; j < newArray[i].length; j++) {

                if (newArray[i][j] == null || newArray[i][j].getPossibleValues().size() > 1) {
                    Set<Byte> possibilities = new HashSet<>(values);

                    possibilities.removeAll(getRepresentedNumbers(i, j));

                    if (possibilities.size() == 0) {
                        throw new IllegalStateException(String.format("No legal values to put in grid[%d][%d]", i, j));
                    }

                    newArray[i][j] = new Square(possibilities);
                }
            }
        }

        return new Grid(newArray);
    }

    /**
     * Get all the numbers which are already present for a given location in the grid.
     * i.e, return the collection of numbers which are present in the same row, column, or quadrant
     * as the given coordinate.
     * <p>
     * Note that only values which have been "locked in," i.e. are the only possible value for a square,
     * are included.
     * @param i The row
     * @param j The column
     * @return All numbers already represented
     */
    private Set<Byte> getRepresentedNumbers(int i, int j) {
        Set<Byte> output = new HashSet<>();

        for (int x = 0; x < LENGTH; x++) {
            Square value = array[x][j];

            if (value != null && value.getPossibleValues().size() == 1) {
                output.addAll(value.getPossibleValues());
            }
        }

        for (int x = 0; x < LENGTH; x++) {
            Square value = array[i][x];

            if (value != null && value.getPossibleValues().size() == 1) {
                output.addAll(value.getPossibleValues());
            }
        }

        int rowQuadrant = i / 3;
        int colQuadrant = j / 3;

        for (int x = rowQuadrant * 3; x < (rowQuadrant + 1) * 3; x++) {
            for (int y = colQuadrant * 3; y < (colQuadrant + 1) * 3; y++) {
                Square value = array[x][y];

                if (value != null && value.getPossibleValues().size() == 1) {
                    output.addAll(value.getPossibleValues());
                }
            }
        }

        return output;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append(":\n");

        for (Square[] row : array) {
            for (int j = 0; j < row.length; j++) {

                builder.append(row[j] == null ? null : row[j].getPossibleValues());

                if (j < row.length - 1) {
                    builder.append(", ");
                }
            }
            builder.append("\n");
        }

        return builder.toString();
    }
}
