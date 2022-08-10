package com.ryan;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import java.util.*;

public class AIPlayer implements ActionListener {
    MSGame game;
    GamePanel gamePanel;
    ControlPanel controlPanel;


    ArrayList<Integer> mine_coords;
    ArrayList<Integer> not_mine_coords;
    Set<Integer> known_coords;
    Set<Integer> finished_coords;

    boolean ordered = true;

    Timer timer;


    AIPlayer(MSGame game, GamePanel gamePanel, ControlPanel controlPanel) {
        this.game = game;
        this.gamePanel = gamePanel;
        this.controlPanel = controlPanel;
        mine_coords = new ArrayList<>();
        not_mine_coords = new ArrayList<>();
        known_coords = new HashSet<>();
        finished_coords = new HashSet<>();
        timer = new Timer(50, this);
        //game.setHigh_score_possible(false);
    }

    public void play() {

        timer.stop();

        // create a list of coordinates that border number areas
        Set<Integer> border_coords = new HashSet<>();  // coords are row * 1000 + col

        for (int i = 0; i < game.height; i ++) {
            for (int j = 0; j < game.width; j++) {  // board looks like : [ row1: [1, 2, 3, 4], row2: [1, 2, 3, 4], row3: [1, 2, 3, 4] ]
                int n = i * 1000 + j;
                if (game.board_states[i][j] && game.board_numbers[i][j] != 0 && !finished_coords.contains(n)) {
                    // border_coords.addAll(x_around(i * 1000 + j, 0));
                    border_coords.add(n);
                }
            }
        }

        // ArrayList<Integer> border_coords = new ArrayList<>(interm_border_coords);

        //System.out.println("Border coords: " + border_coords);

        if (border_coords.isEmpty()) { // e.g. the game hasn't started yet
            // start the game
            border_coords = new HashSet<>();  // TODO start the game
        }

        // until nothing is getting added each time, iterate through the border coords and apply the two basic rules
        Set<Integer> mine_list = new HashSet<>();
        Set<Integer> safe_list = new HashSet<>();
        int oldmlsize;

        // Set<Integer> to_remove = new HashSet<>();
        int to_remove = -1;

        do {
            oldmlsize = mine_list.size() + safe_list.size();
            //System.out.println("--- New While Loop ---");

            for (int coordinate : border_coords) {
                //System.out.println("-- Coordinate: " + coordinate + " --");

                ArrayList<Integer> grass = x_around(coordinate, 0);
                //System.out.println("Grass: " + grass);

                ArrayList<Integer> flags = x_around(coordinate, 1);  // flags around TODO

                //System.out.println("Flags: " + flags);

                // if grass around is the same as number, grass are mine
                if (grass.size() == game.board_numbers[coordinate / 1000][coordinate % 1000] && !finished_coords.contains(coordinate)) {
                    finished_coords.add(coordinate);
                    //System.out.println("Same grass num as num: Mine list added");
                    for (int i : grass) {
                        if (!known_coords.contains(i)) {
                            mine_list.add(i);
                            to_remove = coordinate;
                            // to_remove.add(coordinate);
                        }
                    }
                } else if (game.board_numbers[coordinate / 1000][coordinate % 1000] == flags.size() && !finished_coords.contains(coordinate)) {
                    finished_coords.add(coordinate);
                    // if flags around is the same as number, grass are safe
                    grass.removeAll(flags);
                    //System.out.println("Flags around matches num.");
                    for (int i : grass) {
                        if (!known_coords.contains(i)) {
                            //System.out.println("Adding " + i + " to safe list.");
                            safe_list.add(i);
                        }
                    }
                }
            }
            border_coords.remove(to_remove);

        } while (mine_list.size() + safe_list.size() != oldmlsize);

        //System.out.println("Mine list: " + mine_list);

        //System.out.println("Safe list: " + safe_list);

        // TODO if we still have no information, simulate every possibility
        /*
        long[] possibilities = new long[border_coords.size()];
        int n = Math.min(game.mine_num, border_coords.size());
        // iterate through all possible ways to arrange bombs through those coordinates
        for (int i = n; i > 0; i--) {
            System.out.println("Mine num loop: " + i + " mines.");
            int[] shifts = new int[i];
            System.out.println("Shifts: " + Arrays.toString(shifts));
            while (shifts[0] <= n - game.mine_num) {
                boolean[][] iteration = flag_possibilities(i, border_coords, shifts);

                if (!iteration.equals(new boolean[game.height][game.width])) {
                    System.out.print("Iteration: ");
                    for (boolean[] b : iteration)
                        System.out.print(Arrays.toString(b) + ", ");

                    int c = 0;
                    for (int[] coord : border_coords) {
                        if (iteration[coord[0]][coord[1]])
                            possibilities[c]++;
                        c++;
                    }
                }
            }
        }

        // check if any are 100 or 0
        int c = 0;
        for (long prob : possibilities) {
            if (prob == n)
                prob_coords.add(new int[] {border_coords.get(c)[0], border_coords.get(c)[1], 1});
            else if (prob == 0)
                prob_coords.add(new int[] {border_coords.get(c)[0], border_coords.get(c)[1], 0});
            c ++;
        }

        // if list is empty
        if (prob_coords.size() == 0) {
            long max = 0;
            c = 0;
            for (long p : possibilities) {
                if (p > max)
                    max = c;
                c ++;
            }
            prob_coords.add(new int[] {border_coords.get(c)[0], border_coords.get(c)[1], 1});
        }

         */

        mine_coords.addAll(mine_list);
        not_mine_coords.addAll(safe_list);

        known_coords.addAll(mine_list);
        known_coords.addAll(safe_list);
        //System.out.println("Known coords: " + known_coords);
        //System.out.println("Finished coords: " + finished_coords);

        // System.out.println("COMPLETED");

        timer.start();
    }

    private ArrayList<Integer> x_around(int row_col, int type) { // type: 0 = grass, 1 = flag
        int height = game.height;
        int width = game.width;
        int row = row_col / 1000;
        int col = row_col % 1000;

        // System.out.println("X around | Row: " + row + ", col: " + col + ", type: " + type);
        ArrayList<Integer> bc = new ArrayList<>();

        boolean[][] board;
        boolean target;

        if (type == 0) {
            board = game.board_states;
            target = false;
        } else {  // case 1
            board = game.board_flags;
            target = true;
        }

        if (row < height - 1) {
            if (board[row + 1][col] == target)
                bc.add((row + 1) * 1000 + col);
            if (col > 0 && board[row + 1][col - 1] == target)
                bc.add((row + 1) * 1000 + col - 1);
            if (col < width - 1 && board[row + 1][col + 1] == target)
                bc.add((row + 1) * 1000 + col + 1);
        }
        if (row > 0) {
            if (board[row - 1][col] == target)
                bc.add((row - 1) * 1000 + col);
            if (col > 0 && board[row - 1][col - 1] == target)
                bc.add((row - 1) * 1000 + col - 1);
            if (col < width - 1 && board[row - 1][col + 1] == target)
                bc.add((row - 1) * 1000 + col + 1);
        }
        if (col > 0 && board[row][col - 1] == target)
            bc.add(row * 1000 + col - 1);
        if (col < width - 1 && board[row][col + 1] == target)
            bc.add(row * 1000 + col + 1);

        return bc;
    }

    private boolean[][] flag_possibilities(int total_flags, ArrayList<int[]> mine_cds, int[] shifts) { // the recursive function that does all the possibilities
        System.out.println("Flag possibilities: ");
        for (int[] cd : mine_cds) {
            System.out.print(Arrays.toString(cd) + ", ");
        }

        boolean[][] possible_board = new boolean[game.height][game.width];
        boolean successful = true;

        int[] coord;
        int cur_row;
        int cur_col;

        for (int i = 0; i < total_flags; i++) {
            coord = mine_cds.get(i + shifts[i]);
            cur_row = coord[0];
            cur_col = coord[1];

            possible_board[cur_row][cur_col] = true;
        }

        for (int i = 0; i < game.height; i++) {
            for (int j = 0; j < game.width; j++) {
                if (game.board_states[i][j] && game.mines_around(i, j, possible_board) != game.board_numbers[i][j]) {
                    successful = false;
                    System.out.println("Unsuccessful.");
                    break;
                }
            }
            if (!successful)
                break;
        }

        if (successful) {
            System.out.println("Successful.");
            return possible_board;
        }else
            return new boolean[game.height][game.width];

    }

    private long factorial(int n) {
        System.out.println("Factorializing " + n);
        long num = 1;
        for (int i = 0; i < n; i++) {
            num *= n - i;
            System.out.println(num);
        }
        System.out.println("Result: " + num);
        return num;
    }

    public void change_timer() {

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        // timer.setDelay(20000000);

        System.out.println("Action performing...");
        // if the list is empty, find more using play
        if (mine_coords.size() + not_mine_coords.size() == 0 || ordered) {
            System.out.println("Coords size 0");
            play();
        }

        // do something in the list
        if (mine_coords.size() + not_mine_coords.size() == 0) {
            System.out.println("Fail.");
            timer.stop();
        } /* /* */ else {  // nice
            System.out.println("Not mine coords: " + not_mine_coords);
            if (not_mine_coords.size() != 0) {
                if (ordered) {
                    int pos = 2140000999;
                    int ind = 0;
                    for (int i = 0; i < not_mine_coords.size(); i++) {
                        if (not_mine_coords.get(i)/1000 + not_mine_coords.get(i) % 1000 <= pos/1000 + pos % 1000) {
                            pos = not_mine_coords.get(i);
                            ind = i;
                            System.out.println("New min (mine). Pos = " + pos);
                        }
                    }
                    game.update_game(new int[] {pos / 1000, pos % 1000});
                    not_mine_coords.remove(ind);
                    known_coords.remove(pos);
                    System.out.println("Digging " + pos);
                } else {
                    int pos = not_mine_coords.get(0);
                    System.out.println("Digging " + pos);
                    game.update_game(new int[]{pos / 1000, pos % 1000});
                    not_mine_coords.remove(0);
                    known_coords.remove(pos);
                }
            } else {
                if (ordered) {
                    int pos = Integer.MAX_VALUE;
                    int ind = 0;
                    System.out.println("Mine coords: " + mine_coords);
                    for (int i = 0; i < mine_coords.size(); i++) {
                        if (mine_coords.get(i)/1000 + mine_coords.get(i) % 1000 <= pos/1000 + pos % 1000) {
                            pos = mine_coords.get(i);
                            ind = i;
                            System.out.println("New min (flag). Pos = " + pos);
                        }
                    }
                    game.flag(new int[] {pos / 1000, pos % 1000});
                    mine_coords.remove(ind);
                    System.out.println("Flagging " + pos);
                } else {
                    int pos = mine_coords.get(0);
                    System.out.println("Flagging " + pos);
                    game.flag(new int[]{pos / 1000, pos % 1000});
                    mine_coords.remove(0);
                }
            }
        }

        //*/


        // fast mode
        /*
        for (int i : not_mine_coords) {
            game.update_game(new int[]{i / 1000, i % 1000});
            known_coords.remove(i);
        }
        for (int i : mine_coords) {
            game.flag(new int[]{i / 1000, i % 1000});
        }
        not_mine_coords.clear();
        mine_coords.clear();

         */



        gamePanel.repaint();

        if (game.winner || game.dead) {
            for (int i : mine_coords) {
                game.flag(new int[]{i / 1000, i % 1000});
            }
            timer.stop();
        }
    }
}
