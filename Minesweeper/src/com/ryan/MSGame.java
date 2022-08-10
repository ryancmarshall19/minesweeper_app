package com.ryan;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class MSGame {

    ClickListener clickListener;
    MainFrame frame;

    boolean game_started = false;

    boolean[][] board_mines;
    int[][] board_numbers;
    boolean[][] board_states;
    boolean[][] board_flags;
    int mine_num;

    double square_size;
    int panel_height;

    int height;
    int width;

    boolean winner = false;
    boolean dead = false;
    private boolean high_score_possible = true;
    int num_of_grass;

    LinkedList<int[]> move_list;

    double high_score;


    public MSGame(ClickListener clickListener, int start_value) {

        if (clickListener == null) {
            this.clickListener = new ClickListener(this);
        } else {
            this.clickListener = clickListener;
            this.clickListener.game = this;
        }

        height = start_value * 4;
        width = (int) ((12D/9D) * height);

        frame = new MainFrame(this, this.clickListener, start_value);

        // start off the board just to print at first before the game is set up
        this.board_states = new boolean[height][width];
        this.board_mines = new boolean[height][width];
        this.board_flags = new boolean[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                board_states[i][j] = false;
                board_mines[i][j] = false;
            }
        }

        this.move_list = new LinkedList<>();

        high_score = 100000000;

    }

    public int num_number_squares() {
        int counter = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (board_states[i][j])
                    counter ++;
            }
        }
        return counter;
    }

    public void setUpGame(int h, int posRow, int posCol) {
        game_started = true;

        this.height = h;
        this.width = (height * 12)/9;

        this.board_mines = new boolean[height][width];  // index by [row][column]
        this.board_numbers = new int[height][width];  // [[0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0]]
        this.board_states = new boolean[height][width];  // false = grass, true = number
        this.board_flags = new boolean[height][width];

        double percentage = (-2000D / ((width * height) + 2900D)) + 0.8;
        if (percentage > 0.22)  // 0.22
            percentage = 0.22;
        mine_num = (int) (percentage * width * height + 0.5);  // + 0.5 to account for rounding. So, a mine num of 7.4 will end up being 7, but 7.6 will end up being 8.
        if (mine_num > width * height - 10) { mine_num = width * height - 10; }
        this.num_of_grass = width * height;

        int mn = mine_num;
        while (mn > 0) {
            int row = (int) (Math.random() * height);
            int col = (int) (Math.random() * width);
            if (!(Math.abs(posRow - row) <= 1 && Math.abs(posCol - col) <= 1) && !board_mines[row][col]) {
                if (Math.random() < 1D/(0.4*mines_around(row, col, board_mines) + 1)) {  // reduce the chance of putting a mine there the more mines are around it
                    board_mines[row][col] = true;
                    mn--;
                }
            }
        }

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                board_states[i][j] = false;

                if (board_mines[i][j]) {
                    board_numbers[i][j] = -1;
                } else {
                    board_numbers[i][j] = mines_around(i, j, board_mines);
                }
            }
        }

        try {
            high_score = Double.parseDouble(Files.readAllLines(Paths.get("hs.txt")).get(frame.controlPanel.sizeSlider.get_value() - 1));
        } catch (IOException e) {
            e.printStackTrace();
        }

        frame.controlPanel.start_timer();
    }

    public void update_game(int[] coords) {
        if (!dead && !winner) {
            int row = coords[0];
            int col = coords[1];

            board_flags[row][col] = false;

            // System.out.println("Updating ... " + Arrays.toString(coords));

            if (board_mines[row][col]) {
                // System.out.println("Dead.");
                dead = true;
            } else {
                // System.out.println("Successful click ...");
                // recurse to find all open spaces
                if (!board_states[row][col]) {  // only if you're not clicking on an already opened tile
                    board_states[row][col] = true;
                    num_of_grass--;
                    if (board_numbers[row][col] == 0) {
                        find_open_spaces(row, col);
                    }

                    // System.out.println("Adding coords: " + Arrays.toString(coords));
                }
                // System.out.println("Num of grass: " + num_of_grass);

                if (num_of_grass == mine_num) {
                    winner = true;
                    // write to the high score file
                    update_high_scores();
                }
            }
            move_list.addFirst(coords);
        }
    }

    private void update_high_scores() {
        try {
            double[] high_scores = new double[50];
            List<String> list = Files.readAllLines(Paths.get("hs.txt"));
            for (int i = 0; i < 50; i++) {
                high_scores[i] = Double.parseDouble(list.get(i));
            }

            // System.out.println(Files.readAllLines(Paths.get("hs.txt")));
            // System.out.println(Arrays.toString(high_scores));

            int n = frame.controlPanel.sizeSlider.get_value() - 1;
            if (frame.controlPanel.c_time <= high_scores[n] && high_score_possible) {  // got a high score

                high_scores[n] = frame.controlPanel.c_time;
                frame.controlPanel.high_score = true;

                high_score = high_scores[n];
            }

            FileWriter fw = new FileWriter("hs.txt");
            PrintWriter pw = new PrintWriter(fw);

            pw.print("");
            for (int i = 0; i < 50; i++) {
                pw.println(high_scores[i]);
            }

            pw.close();
            fw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update_all(int[] coords) {
        int row = coords[0];
        int col = coords[1];

        if (board_states[row][col] && flags_around(row, col) == board_numbers[row][col]) {
            // System.out.println("Successful clear...");
            if (row > 0 && !board_states[row - 1][col] && !board_flags[row - 1][col]) {
                // System.out.println("Left");
                update_game(new int[]{row - 1, col});
            }
            if (col > 0 && !board_states[row][col - 1] && !board_flags[row][col - 1]) {
                // System.out.println("Up");
                update_game(new int[]{row, col - 1});
            }
            if (row > 0 && col > 0 && !board_states[row - 1][col - 1] && !board_flags[row - 1][col - 1]) {
                // System.out.println("Up left");
                update_game(new int[]{row - 1, col - 1});
            }
            if (row < height - 1 && !board_states[row + 1][col] && !board_flags[row + 1][col]) {
                // System.out.println("Right");
                update_game(new int[]{row + 1, col});
            }
            if (col < width - 1 && !board_states[row][col + 1] && !board_flags[row][col + 1]) {
                // System.out.println("Down");
                update_game(new int[]{row, col + 1});
            }
            if (row < height - 1 && col < width - 1 && !board_states[row + 1][col + 1] && !board_flags[row + 1][col + 1]) {
                // System.out.println("Down right");
                update_game(new int[]{row + 1, col + 1});
            }
            if (row > 0 && col < width - 1 && !board_states[row - 1][col + 1] && !board_flags[row - 1][col + 1]) {
                // System.out.println("Down left");
                update_game(new int[]{row - 1, col + 1});
            }
            if (row < height - 1 && col > 0 && !board_states[row + 1][col - 1] && !board_flags[row + 1][col - 1]) {
                // System.out.println("Up right");
                update_game(new int[]{row + 1, col - 1});
            }
        }

    }

    public void flag(int[] coords) {
        if (!dead) {
            int row = coords[0];
            int col = coords[1];

            if (game_started && !board_states[row][col]) {
                board_flags[row][col] = !board_flags[row][col];
                move_list.addFirst(coords);
            }
        }
    }

    private void find_open_spaces(int row, int col) {
        // System.out.println("Checking row " + row + ", col " + col);
        boolean clear = board_numbers[row][col] == 0;

        if (row < height - 1) {
            if (!board_states[row + 1][col] && !board_mines[row + 1][col]) {
                if (clear && !board_states[row + 1][col]) {
                    board_states[row + 1][col] = true; num_of_grass --; board_flags[row + 1][col] = false;}
                if (board_numbers[row + 1][col] == 0) {
                    // System.out.println("Found 0 down");
                    find_open_spaces(row + 1, col);
                }
            }

            if (col > 0 && !board_states[row + 1][col - 1] && !board_mines[row + 1][col - 1]) {
                if (clear && !board_states[row + 1][col - 1]) {
                    board_states[row + 1][col - 1] = true; num_of_grass --; board_flags[row + 1][col - 1] = false;}
                if (board_numbers[row + 1][col - 1] == 0) {
                    // System.out.println("Found 0 down and left");
                    find_open_spaces(row + 1, col - 1);
                }
            }
            if (col < width - 1 && !board_states[row + 1][col + 1] && !board_mines[row + 1][col + 1]) {
                if (clear && !board_states[row + 1][col + 1]) {
                    board_states[row + 1][col + 1] = true; num_of_grass --; board_flags[row + 1][col + 1] = false;}
                if (board_numbers[row + 1][col + 1] == 0) {
                    // System.out.println("Found 0 down and right");
                    find_open_spaces(row + 1, col + 1);
                }
            }
        }
        if (row > 0) {
            if (!board_states[row - 1][col] && !board_mines[row - 1][col]) {
                if (clear && !board_states[row - 1][col]) {
                    board_states[row - 1][col] = true; num_of_grass --; board_flags[row - 1][col] = false;}
                if (board_numbers[row - 1][col] == 0) {
                    // System.out.println("Found 0 up");
                    find_open_spaces(row - 1, col);
                }
            }
            if (col > 0 && !board_states[row - 1][col - 1] && !board_mines[row - 1][col - 1]) {
                if (clear && !board_states[row - 1][col - 1]) {
                    board_states[row - 1][col - 1] = true; num_of_grass --; board_flags[row - 1][col - 1] = false;}
                if (board_numbers[row - 1][col - 1] == 0) {
                    // System.out.println("Found 0 up and left");
                    find_open_spaces(row - 1, col - 1);
                }
            }
            if (col < width - 1 && !board_states[row - 1][col + 1] && !board_mines[row - 1][col + 1]) {
                if (clear && !board_states[row - 1][col + 1]) {
                    board_states[row - 1][col + 1] = true; num_of_grass --; board_flags[row - 1][col + 1] = false;}
                if (board_numbers[row - 1][col + 1] == 0) {
                    // System.out.println("Found 0 up and right");
                    find_open_spaces(row - 1, col + 1);
                }
            }
        }
        if (col > 0 && !board_states[row][col - 1] && !board_mines[row][col - 1]) {
            if (clear && !board_states[row][col - 1]) {
                board_states[row][col - 1] = true; num_of_grass --; board_flags[row][col - 1] = false;}
            if (board_numbers[row][col - 1] == 0) {
                // System.out.println("Found 0 left");
                find_open_spaces(row, col - 1);
            }
        }
        if (col < width - 1 && !board_states[row][col + 1] && !board_mines[row][col + 1]) {
            if (clear && !board_states[row][col + 1]) {
                board_states[row][col + 1] = true; num_of_grass --; board_flags[row][col + 1] = false;}
            if (board_numbers[row][col + 1] == 0) {
                // System.out.println("Found 0 right");
                find_open_spaces(row, col + 1);
            }
        }
    }

    public int mines_around(int row, int col, boolean[][] mine_array) {
        int counter = 0;

        if (row < height - 1) {
            if (mine_array[row + 1][col])
                counter ++;
            if (col > 0 && mine_array[row + 1][col - 1])
                counter ++;
            if (col < width - 1 && mine_array[row + 1][col + 1])
                counter ++;
        }
        if (row > 0) {
            if (mine_array[row - 1][col])
                counter ++;
            if (col > 0 && mine_array[row - 1][col - 1])
                counter ++;
            if (col < width - 1 && mine_array[row - 1][col + 1])
                counter ++;
        }
        if (col > 0 && mine_array[row][col - 1])
            counter ++;
        if (col < width - 1 && mine_array[row][col + 1])
            counter ++;

        return counter;
    }

    private int flags_around(int row, int col) {
        int counter = 0;
        if (row < height - 1) {
            if (board_flags[row + 1][col])
                counter ++;
            if (col > 0 && board_flags[row + 1][col - 1])
                counter ++;
            if (col < width - 1 && board_flags[row + 1][col + 1])
                counter ++;
        }
        if (row > 0) {
            if (board_flags[row - 1][col])
                counter ++;
            if (col > 0 && board_flags[row - 1][col - 1])
                counter ++;
            if (col < width - 1 && board_flags[row - 1][col + 1])
                counter ++;
        }
        if (col > 0 && board_flags[row][col - 1])
            counter ++;
        if (col < width - 1 && board_flags[row][col + 1])
            counter ++;

        return counter;
    }

    public void update_dimensions(int size_slider_num) {
        square_size = ((double) panel_height)/((double) size_slider_num);
        height = size_slider_num;
        width = (int) (height * (12D/9D));
    }

    public int[] mouse_to_coord(int mx, int my) {
        return new int[] {(int) (my / square_size), (int) (mx/square_size)};
    }

    public void undo_move() {

        /* .out.print("Before");
        for (int[] e : move_list) {
            System.out.print(", " + Arrays.toString(e));
        }
        System.out.println("."); */

        if (move_list.size() > 1 && !winner) {
            int[] coords = move_list.get(0);
            move_list.removeFirst();
            if (board_states[coords[0]][coords[1]] || (board_mines[coords[0]][coords[1]] && dead)) {  // click
                unclick(coords);
            } else {  // flag
                board_flags[coords[0]][coords[1]] = !board_flags[coords[0]][coords[1]];
            }

            high_score_possible = false;
        }

        /* System.out.print("After");
        for (int[] e : move_list) {
            System.out.print(", " + Arrays.toString(e));
        }
        System.out.println("."); */
    }

    private void unclick(int[] coords) {
        dead = false;
        winner = false;
        if (board_states[coords[0]][coords[1]]){
            board_states[coords[0]][coords[1]] = false;
            num_of_grass ++;
        }
    }

    public int flag_num() {
        int i = 0;
        for (boolean[] row : board_flags) {
            for (boolean spot : row) {
                if (spot)
                    i ++;
            }
        }
        return i;
    }

    public void new_game() {
        frame.controlPanel.timer.stop();  // idk this might clean it up a bit for my laptop?

        new MSGame(clickListener, frame.controlPanel.sizeSlider.get_value());
        frame.dispose();
    }

    public void setHigh_score_possible(boolean b) {
        high_score_possible = b;
    }
}
