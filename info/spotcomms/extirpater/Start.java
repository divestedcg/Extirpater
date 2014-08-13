package info.spotcomms.extirpater;

import javax.swing.*;
import java.awt.*;

/**
 * Created using IntelliJ IDEA
 * User: Tad
 * Date: 8/12/2014
 * Time: 2:24 AM
 */
public class Start {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.jtattoo.plaf.hifi.HiFiLookAndFeel");
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        for (Window window : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
        GUI g = new GUI();
        int amtDrives = g.getDrives(false).size();
        g.setSize(230, 80 + (amtDrives * 30));
        g.setLocation(2, 2);
        g.setTitle("Extirpater");
        g.setVisible(true);
    }

}
