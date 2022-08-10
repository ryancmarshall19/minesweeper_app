package com.ryan;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class GamePanel extends JPanel {
    MSGame game;

    int width;
    int height;

    private final Color dark_green = new Color(15, 110, 15);
    private final Color light_green = new Color(50, 170, 50);

    BufferedImage flag;
    BufferedImage mine;

    {
        try {
            flag = ImageIO.read(getClass().getResourceAsStream("Flag.png"));
            mine = ImageIO.read(getClass().getResourceAsStream("mine.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    BasicStroke stroke = new BasicStroke(2);

    private final Color[] colour_list = new Color[] {
            Color.WHITE,
            new Color(42, 159, 230),
            new Color(119, 248, 190),
            new Color(130, 6, 47),
            new Color(30, 100, 0),
            new Color(226, 251, 4),
            Color.MAGENTA,
            Color.RED,
            Color.BLACK
    };

    public GamePanel(MSGame game, int width, int height) {
        this.game = game;
        this.width = width;
        this.height = height;

        this.setPreferredSize(new Dimension(this.width, this.height));
    }

    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // draw all the squares
        double s = game.square_size;

        for (int i = 0; i < game.height; i++) {
            for (int j = 0; j < game.width; j++) {
                boolean state;
                if (game.game_started) {
                    state = game.board_states[i][j];
                } else {
                    state = false;
                }

                if (!state) {
                    if ((i + j)%2 == 0) {
                        g2d.setColor(dark_green);
                    } else {
                        g2d.setColor(light_green);
                    }
                    g2d.fill(new Rectangle2D.Double(j * s, i * s, s, s));

                    if (game.dead && game.game_started) {
                        if (game.board_mines[i][j] && !game.board_flags[i][j]) {
                            int x;
                            double px = 0;
                            int y;
                            double py = 0;
                            if (Math.random() < 0.5) {x = -1; px = 0.8*s;} else {x = 1;}
                            if (Math.random() < 0.5) {y = -1; py = 0.8*s;} else {y = 1;}

                            g2d.drawImage(mine, (int) (j * s + 0.1*s + px), (int) (i * s + 0.1*s + py), (int) (s*0.8*x), (int) (s*0.8*y), null);
                        } else if (game.board_flags[i][j] && !game.board_mines[i][j]) {
                            game.board_flags[i][j] = false;  // remove the flag
                            // draw an X
                            g2d.setColor(Color.RED);
                            Line2D.Double linepos = new Line2D.Double(j * s + s/5, (i + 1) * s - s/5, (j + 1) * s - s/5, i * s + s/5);
                            Line2D.Double lineneg = new Line2D.Double(j * s + s/5, i * s + s/5, (j + 1) * s - s/5, (i + 1) * s - s/5);
                            g2d.setStroke(new BasicStroke((int) (s/30D + 0.5)));
                            g2d.draw(linepos);
                            g2d.draw(lineneg);
                        }
                    }

                    if (game.game_started && game.board_flags[i][j]) {
                        g2d.drawImage(flag, (int) (j * s + 0.1*s), (int) (i * s), (int) (s*0.8), (int) s, null);
                    }

                } else {
                    int num = game.board_numbers[i][j];
                    if ((i + j) % 2 == 0)
                        g2d.setColor(Color.LIGHT_GRAY);
                    else
                        g2d.setColor(Color.WHITE);
                    g2d.fill(new Rectangle2D.Double(j * s, i * s, s, s));
                    g2d.setColor(colour_list[game.board_numbers[i][j]]);
                    if (num != 0) {
                        Font font = new Font("Monospaced", Font.BOLD, (int) (game.square_size * 0.8));
                        g2d.setFont(font);
                        g2d.drawString(String.valueOf(num), (int) (j * s + s/4D), (int) (i * s + font.getSize()));
                    }
                }
            }
        }
    }

}
