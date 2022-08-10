package com.ryan;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;

public class ControlPanel extends JPanel implements ActionListener {

    MSGame game;
    GamePanel gamePanel;
    intSliderButton sizeSlider;
    basicButton undo_move_button;
    basicButton new_game_button;

    int width;
    int height;

    boolean timer_on = false;
    double c_time;
    Timer timer;

    boolean high_score = false;

    Rectangle2D.Double bgrect;

    private final Font textFont = new Font("Monospaced", Font.BOLD, 20);

    public ControlPanel(MSGame game, GamePanel gamePanel, int width, int height, int start_value) {
        this.game = game;
        this.gamePanel = gamePanel;
        this.setPreferredSize(new Dimension(width, height));
        this.width = width;
        this.height = height;

        String[] strarray = new String[50];

        for (int i = 0; i < 50; i++) {
            if (i == 0) {
                strarray[i] = "If you lose, I pity your braincell: it's lonely.";
            } else if (i < 3) {
                strarray[i] = "This level is for ages 3 - 5. Choking hazard.";
            } else if (i < 5) {
                strarray[i] = "This really isn't hard, but it's respectable.";
            } else if (i < 12) {
                strarray[i] = "This is more like it.";
            } else if (i < 25) {
                strarray[i] = "Are you sure? This is a lot.";
            } else if (i < 39) {
                strarray[i] = "Okay, seriously. You don't need that many.";
            } else if (i < 49) {
                strarray[i] = "I'll be blunt: you're not gonna complete this.";
            } else {
                strarray[i] = "... this is impossible.";
            }
        }

        this.sizeSlider = new intSliderButton(this, new int[] {2*width/10, 150}, new int[] {8*width/10, 150},
                null, 1, null, 50, "Difficulty", new Font("Monospaced", Font.BOLD, 15),
                strarray, new Font("Monospaced", Font.PLAIN, 10), start_value, Color.LIGHT_GRAY, new Color(50, 170, 50),
                new Color(50, 170, 50) /* new Color(15, 110, 15) */, new Font("Monospaced", Font.BOLD, 15),
                new Color(50, 170, 50) /* new Color(15, 110, 15) */);
        game.update_dimensions(sizeSlider.get_value() * 3);
        this.undo_move_button = new basicButton(this, new int[] {width/10, 220}, new int[] {8*width/10, 100},
                25, 2, new Color(50, 170, 50), new Color(20, 140, 40), Color.DARK_GRAY,
                Color.LIGHT_GRAY, Color.GRAY, "Undo Move", new Font("Monospaced", Font.BOLD, (int) ((8*width/10)*0.12)),
                new Color(15, 60, 10));
        this.new_game_button = new basicButton(this, new int[] {width/10, 350}, new int[] {8*width/10, 100},
            25, 2, new Color(50, 170, 50), new Color(20, 140, 40), Color.DARK_GRAY,
                Color.LIGHT_GRAY, Color.GRAY, "New Game", new Font("Monospaced", Font.BOLD, (int) ((8*width/10)*0.12)),
                new Color(15, 60, 10));

        bgrect = new Rectangle2D.Double(0, 0, width + 10, height + 10);
    }

    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHints(rh);

        // bg rectangle
        g2d.setColor(new Color(7, 40, 5));
        g2d.fill(bgrect);
        // title
        g2d.setColor(new Color(50, 170, 50));
        g2d.setFont(new Font("Monospaced", Font.BOLD, 25));
        String str = "Minesweeper";
        g2d.drawString(str, (float) (width/2D - 0.6*25*0.5*str.length()), 50F);
        // subtitle
        g2d.setFont(new Font("Monospaced", Font.BOLD, 14));
        str = "SPACE to click, F to flag.";
        g2d.drawString(str, (float) (width/2D - 0.57*14*0.5*str.length()), 80F);
        // mines left
        if (game.game_started) {
            g2d.setFont(textFont);
            str = "Mines left: " + (game.mine_num - game.flag_num());
            g2d.drawString(str, (float) (width / 2D - 0.55 * 20 * 0.5 * str.length()), height - 30);
        }

        sizeSlider.paint(g2d);

        undo_move_button.paint(g2d);

        new_game_button.paint(g2d);

        // winner
        if (game.winner) {
            timer.stop();
            g2d.setFont(new Font("Monospaced", Font.BOLD, 30));
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawString("You Win!", (float) (width/2F - 0.55 * 30 * 0.5 * 8), 520);
            g2d.setFont(new Font("Monospaced", Font.BOLD, 15));
            g2d.setColor(new Color(50, 170, 50));
            g2d.drawString("Click 'New Game' to play again.", (float) (width/2F - 0.6 * 15 * 0.5 * 31), 550);

            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setStroke(new BasicStroke(3));
            g2d.draw(new RoundRectangle2D.Double((width-2*(0.6 * 15 * 0.5 * 31) - 20)/2, 485, 2*(0.6 * 15 * 0.5 * 31) + 20, 570 - 490, 20, 20));

        } else if (game.dead) {
            timer.stop();
            g2d.setFont(new Font("Monospaced", Font.BOLD, 30));
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawString("You Died.", (float) (width/2F - 0.62 * 30 * 0.5 * 8), 520);
            g2d.setFont(new Font("Monospaced", Font.BOLD, 15));
            g2d.setColor(new Color(50, 170, 50));
            g2d.drawString("Click 'New Game' to play again.", (float) (width/2F - 0.6 * 15 * 0.5 * 31), 550);
            g2d.setFont(new Font("Monospaced", Font.BOLD, 12));
            str = "Or, press 'Undo Move' if you";
            g2d.drawString(str, (float) (width/2F - 0.53 * 13 * 0.5 * str.length()), 575);
            str = "misclicked (or if you're a cheater).";
            g2d.drawString(str, (float) (width/2F - 0.53 * 13 * 0.5 * str.length()), 587);

            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setStroke(new BasicStroke(3));
            g2d.draw(new RoundRectangle2D.Double((width-2*(0.6 * 15 * 0.5 * 31) - 20)/2, 485, 2*(0.6 * 15 * 0.5 * 31) + 20, 605 - 490, 20, 20));
        } else {
            if (timer != null && !timer.isRunning())
                timer.start();
        }

        // timer
        if (timer_on) {
            g2d.setColor(new Color(50, 170, 50));
            g2d.setFont(textFont);
            str = "Time used: " + Math.round(c_time * 10)/10 + "." + Math.round(c_time * 10) % 10 + "s";
            g2d.drawString(str, (float) (width / 2D - 0.55 * 20 * 0.5 * str.length()), height - 100);

            g2d.setFont(textFont);
            if (high_score) {
                g2d.setColor(new Color(207, 181, 59));
            } // else it's still green

            String x;
            if (game.high_score == 100000000) {
                x = "None";
            } else {
                x = Math.round(game.high_score * 10) / 10 + "." + Math.round(game.high_score * 10) % 10;
                x += "s";
            }

            str = "High Score: " + x;
            g2d.drawString(str, (float) (width / 2D - 0.55 * 20 * 0.5 * str.length()), height - 70);
        }

    }

    public void start_timer() {
        timer_on = true;
        /* LocalTime x = LocalTime.now();
        System.out.println(x);

        start_time = (60 * 60 * x.getHour() + 60 * x.getMinute() + x.getSecond());
        c_time = start_time; */
        c_time = 0;

        timer = new Timer(100, this);
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // update time
        /* LocalTime x = LocalTime.now();
        System.out.println(x);

        c_time = (60 * 60 * x.getHour() + 60 * x.getMinute() + x.getSecond()); */

        c_time += 0.1;

        repaint();
    }

    private void high_score(Graphics2D g2d) {

    }
}

class intSliderButton {

    ControlPanel controlPanel;

    private final int startX;
    private final int startY;
    private final int endX;
    private final int endY;

    private final String start_label;
    private final String end_label;
    private final String[] labelList;
    private final String title;
    private final Font title_font;

    private final int lower_bound;
    private final int upper_bound;
    private double current_position;
    private boolean disabled = false;

    private Color line_colour;
    private Color circle_colour;
    private final Color normal_circle_color;
    private final Color clicked_circle_colour;
    private final Font font;
    private final Font labelFont;
    private final Color text_colour;

    private final int circle_rad;
    private final double x_per_num;
    private final double y_per_num;

    intSliderButton(ControlPanel controlPanel, int [] starting_coord, int [] ending_coord, String label1, int lower_bound,
                    String label2, int upper_bound, String title, Font title_font, String[] labelList, Font labelFont, int starting_position,
            /* optionals: */ Color line_colour, Color dot_colour, Color clicked_dot_colour, Font font, Color text_colour) {
        this.controlPanel = controlPanel;

        this.startX = starting_coord[0];
        this.startY = starting_coord[1];
        this.endX = ending_coord[0];
        this.endY = ending_coord[1];
        this.start_label = Objects.requireNonNullElseGet(label1, () -> String.valueOf(lower_bound));
        this.end_label = Objects.requireNonNullElseGet(label2, () -> String.valueOf(upper_bound));
        this.title = title;
        if (labelList == null) {
            this.labelList = new String[upper_bound - lower_bound + 1];
            for (int i = 0; i < upper_bound - lower_bound + 1; i++) {
                this.labelList[i] = "";
            }
        } else {
            this.labelList = labelList;
        }

        this.lower_bound = lower_bound;
        this.upper_bound = upper_bound;
        this.current_position = Math.pow(starting_position - 1, 0.5) * 7.07107 + 0.5; // + 0.5 is so int rounding works properly

        if (line_colour == null) {
            this.line_colour = Color.BLACK;
        } else {
            this.line_colour = line_colour;
        }

        this.normal_circle_color = Objects.requireNonNullElse(dot_colour, Color.RED);
        this.clicked_circle_colour = Objects.requireNonNullElseGet(clicked_dot_colour, () -> new Color(150, 0, 0));
        this.circle_colour = normal_circle_color;

        this.font = Objects.requireNonNullElseGet(font, () -> new Font("BM Jua", Font.BOLD, 20));
        this.labelFont = Objects.requireNonNullElseGet(labelFont, () -> new Font("BM Jua", Font.BOLD, 15));
        this.title_font = Objects.requireNonNullElseGet(title_font, () -> new Font("BM Jua", Font.BOLD, 30));
        // what in the actual fuck does this do.

        this.text_colour = Objects.requireNonNullElseGet(text_colour, () -> circle_colour);

        this.circle_rad = (int) (this.font.getSize()/1.7 + 5);

        x_per_num = ((double) (endX - startX))/((double) (upper_bound - lower_bound));
        y_per_num = ((double) (endY - startY))/((double) (upper_bound - lower_bound));
    }

    public void paint(Graphics2D g2d) {
        // start label, end label, button label

        // line
        Line2D.Double line = new Line2D.Double(startX, startY, endX, endY);
        g2d.setColor(line_colour);
        g2d.draw(line);  // draw or fill?

        // circle
        int[] circle_coords = get_circle_coords();

        Ellipse2D.Double circle = new Ellipse2D.Double(circle_coords[0], circle_coords[1], circle_rad*2, circle_rad*2);
        g2d.setColor(circle_colour);
        g2d.fill(circle);

        // circle text
        g2d.setFont(font);
        g2d.setColor(Color.WHITE);
        String str = String.valueOf((int) Math.pow((current_position/7.07107), 2) + 1);
        g2d.drawString(str, (int) (circle_coords[0] + 15.3 - 5.6*str.length()), circle_coords[1] + 2 * circle_rad - 8);
        // labels
        g2d.setColor(text_colour);
        g2d.drawString(start_label, startX - 10*start_label.length() - 17, startY + 5);
        g2d.drawString(end_label, endX + 17, endY + 5);
        // labelList
        g2d.setFont(labelFont);
        str = labelList[get_value() - lower_bound];
        g2d.drawString(str, (int) ((startX + endX)/2 - 0.5*str.length()*0.39*font.getSize()), (int) ((startY + endY)/2 + 1.85*font.getSize()));
        // title
        g2d.setFont(title_font);
        g2d.drawString(title, (int) ((startX + endX)/2 - 0.5*title.length()*0.6*title_font.getSize()), (int) ((startY + endY)/2 - 1.11*title_font.getSize()));

    }

    public int get_value() {
        return (int) Math.pow((current_position/7.07107), 2) + 1;
    }

    public void set_position(int coordinate) {
        if (coordinate != -1){
            current_position = (coordinate - startX) / x_per_num + lower_bound;
            if (current_position < lower_bound) {
                current_position = lower_bound;
            } else if (current_position > upper_bound) {
                current_position = upper_bound;
            }
        }
        set_circle_colour(clicked_circle_colour);
        controlPanel.repaint();
    }

    public void reset_colours() {
        if (!disabled) {
            circle_colour = normal_circle_color;
            controlPanel.repaint();
        }
    }

    public void set_circle_colour(Color colour) {
        circle_colour = colour;
    }

    public void set_line_colour(Color colour) {
        line_colour = colour;
    }

    private int[] get_circle_coords() {
        int x = (int) (((current_position - lower_bound) * x_per_num) + startX);
        int y = (int) (((current_position - lower_bound) * y_per_num) + startY);

        return new int[] {x - circle_rad, y - circle_rad};
    }

    public boolean point_on(int x, int y) {
        return (x > startX - circle_rad && x < endX + circle_rad && y > startY - circle_rad && y < endY + circle_rad);
    }

    public void set_disabled(boolean b, boolean p) {
        disabled = b;
        set_circle_colour(clicked_circle_colour);
        if (p)
            controlPanel.repaint();
    }
}

class basicButton {
    ControlPanel controlPanel;

    private int state;  // 0 = disabled, 1 = normal, 2 = clicked

    private final int start_x;
    private final int start_y;
    private final int width;
    private final int height;

    private final Color bg_colour;
    private final Color clicked_bg_colour;
    private final Color disabled_bg_colour;
    private final Color border_colour;
    private final Color disabled_border_colour;

    private final String text;
    private final Font text_font;
    private final Color text_colour;

    // for paint
    RoundRectangle2D.Double background;
    BasicStroke stroke;

    basicButton(ControlPanel controlPanel, int[] start_coords, int[] dimensions, int curve_len, int border_width,
                Color bg_colour, Color clicked_bg_colour, Color disabled_bg_colour, Color border_colour, Color disabled_border_colour,
                String text, Font text_font, Color text_colour) {
        this.controlPanel = controlPanel;

        this.start_x = start_coords[0];
        this.start_y = start_coords[1];
        this.width = dimensions[0];
        this.height = dimensions[1];

        this.bg_colour = Objects.requireNonNullElse(bg_colour, new Color(99, 184, 240));
        int red = (int) (this.bg_colour.getRed() * 1.3);
        if (red > 255) { red = 255;}
        int green = (int) (this.bg_colour.getGreen()*1.3);
        if (green > 255) { green = 255; }
        int blue = (int) (this.bg_colour.getBlue()*1.3);
        if (blue > 255) { blue = 255; }
        this.clicked_bg_colour = Objects.requireNonNullElse(clicked_bg_colour,
                new Color(red, green, blue));
        this.disabled_bg_colour = Objects.requireNonNullElse(disabled_bg_colour,
                new Color((int) (this.bg_colour.getRed() * 0.6), (int) (this.bg_colour.getGreen()*0.6), (int) (this.bg_colour.getBlue()*0.6)));
        this.border_colour = Objects.requireNonNullElse(border_colour, Color.BLACK);
        this.disabled_border_colour = Objects.requireNonNullElse(disabled_border_colour, Color.GRAY);

        this.text = text;
        this.text_font = Objects.requireNonNullElse(text_font, new Font("BM Jua", Font.BOLD, 25));
        this.text_colour = Objects.requireNonNullElse(text_colour, Color.RED);

        this.state = 1;

        // for paint
        this.background = new RoundRectangle2D.Double(start_x, start_y, width, height, curve_len, curve_len);
        this.stroke = new BasicStroke(border_width);
    }

    public void paint(Graphics2D g2d) {
        // background
        if (state == 0) { // disabled
            g2d.setColor(disabled_bg_colour);
        } else if (state == 1) {  // normal
            g2d.setColor(bg_colour);
        } else {  // clicked
            g2d.setColor(clicked_bg_colour); }
        g2d.fill(background);
        // border
        if (state == 0)
            g2d.setColor(disabled_border_colour);
        else
            g2d.setColor(border_colour);
        g2d.setStroke(stroke);
        g2d.draw(background);
        // text
        g2d.setFont(text_font);
        g2d.setColor(text_colour);
        g2d.drawString(text, (int) ((2 * start_x + width)/2 - 0.5 * text.length() * 0.6 * text_font.getSize()),
                (int) ((2* start_y + height)/2 + 0.5 * 0.8 * text_font.getSize()));
    }

    public boolean mouseOn(int x, int y) {
        return (x > start_x && x < start_x + width && y > start_y && y < start_y + height && state != 0);
    }

    public void setState(int s, boolean p) {
        state = s;
        if (p)
            controlPanel.repaint();
    }

    public int getState() {
        return state;
    }
}