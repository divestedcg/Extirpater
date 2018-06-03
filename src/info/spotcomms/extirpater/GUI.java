/*
Copyright (c) 2014-2017 Divested Computing, Inc.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package info.spotcomms.extirpater;

import javax.crypto.Cipher;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Scanner;
import java.util.prefs.Preferences;

/**
 * Created using IntelliJ IDEA
 * User: Tad
 * Date: 8/12/2014
 * Time: 2:24 AM
 */
class GUI extends JFrame {

    boolean isAdmin = false;
    private boolean unlimitedStrength = false;
    String os;
    private final ArrayList<Drive> drives = new ArrayList<>();
    final JComboBox drpPasses = new JComboBox(new String[] {"Don't Erase Free Space", "1 Pass, 0", "1 Pass, Random", "2 Passes, 0/1", "3 Passes, 0/1/Random"});
    final Checkbox chkEmptyTrash = new Checkbox("Empty Trash", true);
    final JComboBox drpFillFileTable = new JComboBox(new String[] {"Don't Fill File Table", "Fill File Table, 20,000 Files", "Fill File Table, 200,000 Files", "Fill File Table, 2,000,000 Files"});

    final ArrayList<ByteArray> byteArrays = new ArrayList<>();

    public GUI() {
        try {
            isAdmin = isAdmin();
            unlimitedStrength = Cipher.getMaxAllowedKeyLength("AES") != 128;
            os = getOS();
            initDrives();
            int amtDrives = drives.size();
            setTitle("Extirpater");
            setLocation(50, 50);
            setSize(850, (amtDrives * 35) + 20);
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
            revalidate();
            repaint();
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
            e.printStackTrace();
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
                        String displayName = drivePath + " (" + fsv.getSystemDisplayName(drivePath).substring(0, fsv.getSystemDisplayName(drivePath).length() - 4) + ")";
                        drives.add(new Drive(this, drivePath, displayName, unlimitedStrength));
                    }
                }
            }
            if (os.equals("Mac")) {
                File[] volumes = new File("/Volumes").listFiles();
                for (File drivePath : volumes) {
                    if (drivePath.isDirectory()) {
                        String displayName = drivePath + " (" + (drivePath + "").substring(9, (drivePath + "").length()) + ")";
                        drives.add(new Drive(this, drivePath, displayName, unlimitedStrength));
                    }
                }
            }
            //Credits: Joe aka Dr_fantasmo
            if (os.equals("Linux")) {
                Process getDrives = new ProcessBuilder("mount", "-l").start();
                Scanner s = new Scanner(getDrives.getInputStream());
                ArrayList<String> drivesTemp = new ArrayList<>();
                while (s.hasNextLine()) {
                    String drive = s.nextLine();
                    if (drive.startsWith("/dev/sd") || drive.startsWith("/dev/vd") || drive.startsWith("/dev/mmcblk") || drive.startsWith("/dev/mapper/")) {
                        drivesTemp.add(drive);
                    }
                }
                s.close();
                Collections.sort(drivesTemp);
                for (String drive : drivesTemp) {
                    String[] driveS = drive.split(" ");
                    File drivePath = new File(driveS[2]);
                    String driveId = driveS[0].split("/dev/")[1];
                    String ssd = "";
                    if (driveId.startsWith("sd") || driveId.startsWith("vd")) {
                        driveId = driveId.substring(0, 3);
                        Scanner rotational = new Scanner(new File("/sys/block/" + driveId + "/queue/rotational"));
                        ssd = rotational.nextLine();
                        rotational.close();
                    }
                    String displayName = drivePath + "";
                    if (drive.contains("[") && drive.contains("]")) {
                        displayName += " (" + drive.substring(drive.indexOf("[") + 1, drive.lastIndexOf("]")) + ")";
                    }
                    if (ssd.equals("0")) {
                        displayName += " [SSD] ";
                    }
                    if (driveId.startsWith("mmcblk")) {
                        displayName += " [FLASH] ";
                    }
                    if (driveId.startsWith("vd")) {
                        displayName += " [VIRTUAL] ";
                    }
                    if (driveS[5].contains("compress")) {
                        displayName += " [COMPRESSED] ";
                    }
                    drives.add(new Drive(this, drivePath, displayName, unlimitedStrength));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
