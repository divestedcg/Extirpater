/*
 * Copyright (c) 2015. Spot Communications
 */

package info.spotcomms.extirpater;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import java.util.prefs.Preferences;

/**
 * Created using IntelliJ IDEA
 * User: Tad
 * Date: 8/12/2014
 * Time: 2:24 AM
 */
public class GUI extends JFrame {

    public boolean isAdmin;
    public String os;
    private ArrayList<Drive> drives = new ArrayList<Drive>();
    private int amtDrives = 0;
    public JComboBox drpPasses =
        new JComboBox(new String[] {"Don't Erase Free Space", "1 Pass, 0", "2 Passes, 0/1",
            "3 Passes, 0/1/Random"});
    public Checkbox chkEmptyTrash = new Checkbox("Empty Trash", true);
    public JComboBox drpFillFileTable = new JComboBox(
        new String[] {"Don't Fill File Table", "Fill File Table, 20,000 Files",
            "Fill File Table, 200,000 Files", "Fill File Table, 2,000,000 Files"});

    public ArrayList<ByteArray> byteArrays = new ArrayList<ByteArray>();

    public GUI() {
        try {
            isAdmin = isAdmin();
            os = getOS();
            initDrives();
            amtDrives = drives.size();
            setTitle("Extirpater");
            setLocation(50, 50);
            setSize(800, (amtDrives * 30) + 20);
            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(amtDrives + 1, 3));
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setVisible(true);
            add(panel);
            for (Drive drive : drives) {
                panel.add(drive.getLblDriveName());
                panel.add(drive.getBtnControl());
                panel.add(drive.getLblStatus());
            }
            panel.add(drpPasses);
            panel.add(chkEmptyTrash);
            chkEmptyTrash.setEnabled(isAdmin);
            if (!isAdmin) {
                chkEmptyTrash.setLabel("Empty Trash (admin only)");
            }
            panel.add(drpFillFileTable);
            new ByteArray(this, (byte) 0x00, 1000000 * 25);
            new ByteArray(this, (byte) 0x00, 1000000);
            new ByteArray(this, (byte) 0x00, 1000);
            new ByteArray(this, (byte) 0xFF, 1000000 * 25);
            new ByteArray(this, (byte) 0xFF, 1000000);
            new ByteArray(this, (byte) 0xFF, 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Credits: http://stackoverflow.com/a/23538961
    private boolean isAdmin() {
        try {
            Preferences prefs = Preferences.systemRoot();
            prefs.put("extirpater", "swag"); //SecurityException on Windows
            prefs.remove("extirpater");
            prefs.flush(); //BackingStoreException on Linux
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String getOS() {
        try {
            String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
            if (os.startsWith("win")) {
                return "Windows";
            }
            if (os.startsWith("mac")) {
                return "Mac";
            }
            if (os.contains("linux")) {
                return "Linux";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    private void initDrives() {
        try {
            //Credits: http://stackoverflow.com/a/15608620
            if (os.equals("Windows")) {
                FileSystemView fsv = FileSystemView.getFileSystemView();
                for (File drivePath : File.listRoots()) {
                    String driveType = fsv.getSystemTypeDescription(drivePath);
                    if (driveType.equals("Local Disk") || driveType.equals("Removable Disk")) {
                        String displayName = fsv.getSystemDisplayName(drivePath)
                            .substring(0, fsv.getSystemDisplayName(drivePath).length() - 4);
                        drives.add(new Drive(this, drivePath, displayName));
                    }
                }
            }
            if (os.equals("Mac")) {
                File[] volumes = new File("/Volumes").listFiles();
                for (File drivePath : volumes) {
                    if (drivePath.isDirectory()) {
                        String displayName =
                            (drivePath + "").substring(9, (drivePath + "").length());
                        drives.add(new Drive(this, drivePath, displayName));
                    }
                }
            }
            //Credits: Joe aka Dr_fantasmo
            if (os.equals("Linux")) {
                Process getDrives = new ProcessBuilder("mount", "-l").start();
                Scanner s = new Scanner(getDrives.getInputStream());
                ArrayList<String> drivesTemp = new ArrayList<String>();
                while (s.hasNextLine()) {
                    String drive = s.nextLine();
                    if (drive.startsWith("/dev/sd")) {
                        drivesTemp.add(drive);
                    }
                }
                s.close();
                for (String drive : drivesTemp) {
                    String[] driveS = drive.split(" ");
                    File drivePath = new File(driveS[2]);
                    String displayName = "";
                    if (drive.contains("[") && drive.contains("]")) {
                        displayName =
                            drive.substring(drive.indexOf("[") + 1, drive.lastIndexOf("]"));
                    }
                    drives.add(new Drive(this, drivePath, displayName));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
