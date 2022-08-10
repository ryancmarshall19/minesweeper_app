package com.ryan;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    MSGame game;
    GamePanel gamePanel;
    ControlPanel controlPanel;
    ClickListener clickListener;
    AIPlayer aiPlayer;

    int height;
    int game_panel_width;
    int control_panel_width;

    public MainFrame(MSGame game, ClickListener clickListener, int start_value) {

        this.game = game;

        height = Toolkit.getDefaultToolkit().getScreenSize().height - 200;
        if (height * (12D/9D) * 1.3 < Toolkit.getDefaultToolkit().getScreenSize().width) {
            game_panel_width = (int) (height * 12D/9D);
            control_panel_width = (int) (game_panel_width * 0.3);
        } else {
            int width = Toolkit.getDefaultToolkit().getScreenSize().width;
            height = (int) ((width/1.3D) * (9D/12D));
            game_panel_width = (int) (width/1.3D);
            control_panel_width = (int) (width - width/1.3D) - 20;
        }

        game.panel_height = height;

        this.gamePanel = new GamePanel(game, game_panel_width, height);
        this.controlPanel = new ControlPanel(game, gamePanel, control_panel_width, height, start_value);
        this.clickListener = clickListener;
        this.aiPlayer = new AIPlayer(game, gamePanel, controlPanel);
        this.clickListener.set_up_listener(gamePanel, controlPanel, aiPlayer);

        gamePanel.addMouseListener(this.clickListener);
        gamePanel.addMouseMotionListener(this.clickListener);
        controlPanel.addMouseListener(this.clickListener);
        controlPanel.addMouseMotionListener(this.clickListener);

        this.addKeyListener(this.clickListener);


        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Minesweeper");
        this.setResizable(false);

        JPanel ur_a_nerd = new JPanel();
        ur_a_nerd.setSize(new Dimension(game_panel_width + control_panel_width, height));
        ur_a_nerd.setBackground(Color.BLACK);
        // System.out.println(new Dimension(game_panel_width + control_panel_width, height));
        // System.out.println(Toolkit.getDefaultToolkit().getScreenSize());
        ur_a_nerd.add(gamePanel, BorderLayout.WEST);
        ur_a_nerd.add(controlPanel, BorderLayout.EAST);
        this.add(ur_a_nerd);
        this.setUndecorated(true);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);

    }
}
