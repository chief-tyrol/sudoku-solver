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

package zone.gryphon.sudoku;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import zone.gryphon.sudoku.model.Grid;
import zone.gryphon.sudoku.model.Square;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author tyrol
 */
@Slf4j
@ToString
public class SudokuSolverApplication {

    public static void main(String... args) throws Exception {
        new SudokuSolverApplication().doMain(args);
    }

//    @Option(name = "--file", required = true)
    private String file;

    public void doMain(String... args) throws Exception {
        new CmdLineParser(this).parseArgument(args);

//        final String puzzle = "" +
//                "x,7,x,2,3,8,x,x,x\n" +
//                "x,x,x,7,4,x,8,x,9\n" +
//                "x,6,8,1,x,9,x,x,2\n" +
//                "x,3,5,4,x,x,x,x,8\n" +
//                "6,x,7,8,x,2,5,x,1\n" +
//                "8,x,x,x,x,5,7,6,x\n" +
//                "2,x,x,6,x,3,1,9,x\n" +
//                "7,x,9,x,2,1,x,x,x\n" +
//                "x,x,x,9,7,4,x,8,x";

        final String puzzle = "" +
                "x,7,x,2,3,x,x,x,x\n" +
                "x,x,x,7,4,x,x,x,9\n" +
                "x,6,x,1,x,9,x,x,2\n" +
                "x,3,5,4,x,x,x,x,x\n" +
                "6,x,7,x,x,2,5,x,1\n" +
                "8,x,x,x,x,5,7,6,x\n" +
                "2,x,x,6,x,3,1,9,x\n" +
                "7,x,9,x,2,1,x,x,x\n" +
                "x,x,x,9,7,4,x,x,x";

        byte[][] grid = new byte[9][9];

        String[] rows = puzzle.split("\n");
        for (int i = 0; i < rows.length; i++) {
            String[] columns = rows[i].split(",");
            for (int j = 0; j < columns.length; j++) {
                grid[i][j] = columns[j].charAt(0) == 'x' ? 0 : Byte.parseByte(columns[j]);
            }
        }

        log.info("this: {}", this);

        Grid grid1 = Grid.from(grid);

        log.info("grid: {}", grid1);

        log.info("Solution: {}", grid1.solve());
    }




}
