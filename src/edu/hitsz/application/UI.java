package edu.hitsz.application;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;

import static edu.hitsz.application.Main.object;

/**
 * 记录类。
 * 包含玩家姓名、排名、分数和时间
 *
 * @author HoganZ
 */

public class AircraftUI {
    private JPanel mainPanel;
    private JButton easy;
    private JButton normal;
    private JButton difficult;
    private JPanel topPanel;
    private JPanel buttonPanel;
    private JPanel bottomPanel;
    private JCheckBox soundEffectCheck;
    private JLabel gameName;
    private JComboBox music;
    private JTextField musicText;
    private final Game game = Game.getGame();

    public static void main(String[] args) {
        JFrame frame = new JFrame("Menu");
        frame.setContentPane(new AircraftUI().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public AircraftUI() {
        easy.addActionListener(e -> {
            try {
                ImageManager.BACKGROUND_IMAGE = ImageIO.read(new FileInputStream("src/images/bg.jpg"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            game.setMusicUsage(music.getSelectedIndex()==0);
            game.setDifficulty(0);
            synchronized(object) {
                object.notify();
            }
        });
        normal.addActionListener(e -> {
            try {
                ImageManager.BACKGROUND_IMAGE = ImageIO.read(new FileInputStream("src/images/bg3.jpg"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            game.setMusicUsage(music.getSelectedIndex()==0);
            game.setDifficulty(1);
            synchronized(object) {
                object.notify();
            }
        });
        difficult.addActionListener(e -> {
            try {
                ImageManager.BACKGROUND_IMAGE = ImageIO.read(new FileInputStream("src/images/bg5.jpg"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            game.setMusicUsage(music.getSelectedIndex()==0);
            game.setDifficulty(2);
            synchronized(object) {
                object.notify();
            }
        });
    }

    public JPanel getMainPanel(){
        return mainPanel;
    }

}
