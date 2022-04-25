package edu.hitsz.application.menu;

import edu.hitsz.application.Game;
import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Status;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;


/**
 * @author HoganZ
 */
public class AircraftUI{
    private JButton easyButton;
    private JPanel mainPanel;

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private JComboBox<String> music;
    private JButton simple;
    private JButton difficult;
    private JTextField musicText;
    private JPanel topPanel;
    private JPanel bottomPanel;

    private Game game;

    public void setGame(Game game) {
        this.game = game;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Menu");
        frame.setContentPane(new AircraftUI().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public AircraftUI() {
        easyButton.addActionListener(e -> {
            System.out.println("选择了简单模式");
            try {
                ImageManager.BACKGROUND_IMAGE = ImageIO.read(new FileInputStream("src/images/bg.jpg"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            game.setDifficulty(0);
            Status.menuOver = true;
            game.setUseMusic(music.getSelectedIndex() == 0);
            synchronized (Status.class) {
                Status.class.notifyAll();
            }
        });
        simple.addActionListener(e -> {
            System.out.println("选择了普通模式");
            try {
                ImageManager.BACKGROUND_IMAGE = ImageIO.read(new FileInputStream("src/images/bg3.jpg"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            game.setDifficulty(1);
            Status.menuOver = true;
            game.setUseMusic(music.getSelectedIndex() == 0);
            synchronized (Status.class) {
                Status.class.notifyAll();
            }
        });
        difficult.addActionListener(e -> {
            System.out.println("选择了困难模式");
            try {
                ImageManager.BACKGROUND_IMAGE = ImageIO.read(new FileInputStream("src/images/bg5.jpg"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            game.setDifficulty(2);
            Status.menuOver = true;
            game.setUseMusic(music.getSelectedIndex() == 0);
            synchronized (Status.class) {
                Status.class.notifyAll();
            }
        });
    }
}