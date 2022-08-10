package com.ryan;

import java.awt.event.*;

public class ClickListener implements MouseListener, MouseMotionListener, KeyListener {

    MSGame game;
    GamePanel gamePanel;
    ControlPanel controlPanel;
    AIPlayer aiPlayer;

    boolean on_size_slider = false;

    int mousex = 0;
    int mousey = 0;

    public ClickListener(MSGame game) {
        this.game = game;
    }

    public void set_up_listener(GamePanel gamePanel, ControlPanel controlPanel, AIPlayer aiPlayer) {
        this.gamePanel = gamePanel;
        this.controlPanel = controlPanel;
        this.aiPlayer = aiPlayer;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        int[] coords = game.mouse_to_coord(x, y);

        if (e.getComponent() == gamePanel) {
            if (e.getButton() == 1) {
                click(coords);
            } else if (e.getButton() == 3) {
                rclick(coords);
            }
            gamePanel.repaint();
        } else if (e.getComponent() == controlPanel) {
            if (controlPanel.sizeSlider.point_on(x, y) && !game.game_started) {
                controlPanel.sizeSlider.set_position(x);
                game.update_dimensions(controlPanel.sizeSlider.get_value() * 3);
                gamePanel.repaint();
            } else if (controlPanel.undo_move_button.mouseOn(x, y)) {
                controlPanel.undo_move_button.setState(2, true);
            } else if (controlPanel.new_game_button.mouseOn(x, y)) {
                controlPanel.new_game_button.setState(2, true);
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        on_size_slider = false;
        controlPanel.sizeSlider.reset_colours();
        controlPanel.undo_move_button.setState(1, false);
        controlPanel.new_game_button.setState(1, true);
        if (e.getComponent() == controlPanel) {
            if (controlPanel.undo_move_button.mouseOn(mousex, mousey)) {
                game.undo_move();
                gamePanel.repaint();
            } else if (controlPanel.new_game_button.mouseOn(mousex, mousey)) {
                game.new_game();
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    // MOTION LISTENER

    @Override
    public void mouseDragged(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (e.getComponent() == controlPanel) {
            if (on_size_slider) {
                controlPanel.sizeSlider.set_position(x);
                controlPanel.repaint();
                game.update_dimensions(controlPanel.sizeSlider.get_value() * 3);
                gamePanel.repaint();
            } else if (controlPanel.sizeSlider.point_on(x, y) && !game.game_started) {
                on_size_slider = true;
                controlPanel.sizeSlider.set_position(e.getX());
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mousex = e.getX();
        mousey = e.getY();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == 32) {  // space
            click(game.mouse_to_coord(mousex, mousey));
            gamePanel.repaint();
        } else if ((k == 88 || k == 70) && game.game_started) {  // x or f
            rclick(game.mouse_to_coord(mousex, mousey));
            gamePanel.repaint();
        } else if ((k == 157 || k == 67 || k == 69) && game.game_started) {  // c or cmnd or e
            clear(game.mouse_to_coord(mousex, mousey));
            gamePanel.repaint();
        }
    }

    private void click(int[] coords) {
        if (!game.game_started) {
            game.setUpGame(controlPanel.sizeSlider.get_value() * 3, coords[0], coords[1]);
            game.game_started = true;
            gamePanel.repaint();
            controlPanel.sizeSlider.set_disabled(true, true);
        }

        //if (!aiPlayer.timer.isRunning())
        //    aiPlayer.timer.start();

        game.update_game(coords);
        if (game.winner || game.dead)
            controlPanel.repaint();
    }

    private void rclick(int[] coords) {
        game.flag(coords);
        controlPanel.repaint();
    }

    private void clear(int[] coords) {
        game.update_all(coords);
        controlPanel.repaint();
    }
}
