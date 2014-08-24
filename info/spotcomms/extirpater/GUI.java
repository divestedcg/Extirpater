package info.spotcomms.extirpater;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.prefs.Preferences;

/**
 * Created using IntelliJ IDEA
 * User: Tad
 * Date: 8/12/2014
 * Time: 2:24 AM
 */
public class GUI extends JFrame implements ActionListener {

    private boolean isAdmin;
    private ArrayList<Drive> drives = new ArrayList<Drive>();
    private int amtDrives = 0;
    private final JComboBox drpPasses = new JComboBox(new String[] {"1 Pass, 0", "2 Passes, 0/1", "3 Passes, 0/1/R"});
    private int passes = 1;
    private final Checkbox chkEmptyRecycleBins = new Checkbox("Empty Recycle Bins");
    private final JButton btnStart = new JButton("Extirpate!");
    private boolean isRunning = false;
    private int amtDrivesSelected = 0;
    private final JLabel lblStatus = new JLabel("Waiting", JLabel.CENTER);

    public GUI() {
        isAdmin = isAdmin();
        initDrives();
        amtDrives = drives.size();
        setSize(230, 80 + (amtDrives * 30));
        setLocation(2, 2);
        setTitle("Extirpater");
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(amtDrives + 4, 1));
        add(panel);
        for (Drive drive : drives) {
            panel.add(drive.getCheckbox());
        }
        panel.add(drpPasses);
        drpPasses.addActionListener(this);
        panel.add(chkEmptyRecycleBins);
        chkEmptyRecycleBins.setEnabled(isAdmin);
        if (!isAdmin) {
            chkEmptyRecycleBins.setLabel("Empty Recycle Bins (admin only)");
        }
        panel.add(btnStart);
        btnStart.addActionListener(this);
        panel.add(lblStatus);
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    //Credits: http://stackoverflow.com/a/23538961
    private boolean isAdmin() {
        Preferences prefs = Preferences.systemRoot();
        try {
            prefs.put("extirpater", "swag"); //SecurityException on Windows
            prefs.remove("extirpater");
            prefs.flush(); //BackingStoreException on Linux
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //Credits: http://stackoverflow.com/a/15608620
    private void initDrives() {
        FileSystemView fsv = FileSystemView.getFileSystemView();
        for (File drivePath : File.listRoots()) {
            String driveType = fsv.getSystemTypeDescription(drivePath);
            if (driveType.equals("Local Disk") || driveType.equals("Removable Disk")) {
                String displayName = fsv.getSystemDisplayName(drivePath);
                drives.add(new Drive(this, drivePath, displayName.substring(0, displayName.length() - 4)));
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == drpPasses) {
            passes = drpPasses.getSelectedIndex() + 1;
        }
        if (e.getSource() == btnStart) {
            if (isRunning()) {
                isRunning = false;
                btnStart.setText("Halting...");
                btnStart.setEnabled(false);
            } else {
                isRunning = true;
                btnStart.setText("Halt!");
                amtDrivesSelected = 0;
                for (int x = 0; x < drives.size(); x++) {
                    Drive drive = drives.get(x);
                    drive.setDriveStatus(0);
                    drive.getCheckbox().setEnabled(false);
                    if (drive.getCheckbox().getState()) {
                        amtDrivesSelected++;
                        if (chkEmptyRecycleBins.getState()) {
                            drive.emptyDriveRecycleBin().start();
                        }
                        drive.wipeDriveFreeSpace(passes).start();
                    }
                }
                updateStatus().start();
            }
        }
    }

    private Thread updateStatus() {
        return new Thread(() -> {
            try {
                lblStatus.setText("Running");
                double percentage = 0.0;
                if (amtDrivesSelected == 0) {
                    percentage = 100.0;
                }
                while (percentage < 100.0) {
                    percentage = 0;
                    for (int x = 0; x < drives.size(); x++) {
                        percentage += drives.get(x).getDriveStatus();
                    }
                    percentage /= amtDrivesSelected;
                    percentage = Math.round(percentage * 100.0) / 100.0;
                    lblStatus.setText("Running, " + percentage + "%");
                    Thread.sleep(500);
                }
                lblStatus.setText("Waiting");
                isRunning = false;
                btnStart.setEnabled(true);
                btnStart.setText("Extirpate!");
                for (int x = 0; x < drives.size(); x++) {
                    drives.get(x).getCheckbox().setEnabled(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}
