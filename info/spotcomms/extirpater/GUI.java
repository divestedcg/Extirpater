package info.spotcomms.extirpater;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.prefs.Preferences;

/**
 * Created using IntelliJ IDEA
 * User: Tad
 * Date: 8/12/2014
 * Time: 2:24 AM
 */
public class GUI extends JFrame implements ActionListener {

    private final ArrayList<Checkbox> chkDrives = new ArrayList<Checkbox>();
    private final JComboBox drpPasses = new JComboBox(new String[]{"1 Pass, 0", "2 Passes, 0/1", "3 Passes, 0/1/R"});
    private final Checkbox chkEmptyRecycleBins = new Checkbox("Empty Recycle Bins");
    private final JButton btnStart = new JButton("Extirpate!");
    private final JLabel lblStatus = new JLabel("Waiting", JLabel.CENTER);

    private boolean isRunning = false;
    private boolean isAdmin = false;
    private int amtDrives = 0;
    private int amtDrivesSelected = 0;
    private int passes = 1;
    private Double[] driveStatus = new Double[1337];

    public GUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        add(panel);
        isAdmin = isAdmin();
        amtDrives = getDrives(false).size();
        panel.setLayout(new GridLayout(amtDrives + 4, 1));
        for(int x = 0; x < amtDrives; x++) {
            chkDrives.add(new Checkbox(getDrives(true).get(x)));
            panel.add(chkDrives.get(x));
        }
        panel.add(drpPasses); drpPasses.addActionListener(this);
        panel.add(chkEmptyRecycleBins); chkEmptyRecycleBins.setEnabled(isAdmin); if(!isAdmin) chkEmptyRecycleBins.setLabel(chkEmptyRecycleBins.getLabel() + " (admin only)");
        panel.add(btnStart); btnStart.addActionListener(this);
        panel.add(lblStatus);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == drpPasses) {
            passes = drpPasses.getSelectedIndex() + 1;
        }
        if (e.getSource() == btnStart) {
            if(!isRunning) {
                isRunning = true;
                btnStart.setText("Halt!");
                amtDrivesSelected = 0;
                driveStatus = new Double[1337];
                for (Checkbox c : chkDrives) {
                    if (c.getState()) {
                        amtDrivesSelected++;
                        if (chkEmptyRecycleBins.getState()) {
                            emptyRecycleBin(new File(c.getLabel().substring(0, 3))).start();
                        }
                        wipeDriveFreeSpace(new File(c.getLabel().substring(0, 3))).start();
                    }
                    c.setEnabled(false);
                }
                updateStatus().start();
            } else {
                isRunning = false;
                btnStart.setText("Halting...");
                btnStart.setEnabled(false);
            }
        }
    }

    public ArrayList<String> getDrives(boolean withNames) {
        ArrayList<String> drives = new ArrayList<String>();
        FileSystemView fsv = FileSystemView.getFileSystemView();
        for(File path:File.listRoots()) {
            if(fsv.getSystemTypeDescription(path).equals("Local Disk") || fsv.getSystemTypeDescription(path).equals("Removable Disk")) {
                if(withNames)
                    drives.add(path + " - " + fsv.getSystemDisplayName(path).substring(0, fsv.getSystemDisplayName(path).length() - 4));
                else
                    drives.add(path + "");
            }
        }
        return drives;
    }

    private boolean isAdmin(){
        Preferences prefs = Preferences.systemRoot();
        try{
            prefs.put("extirpater", "swag"); //SecurityException on Windows
            prefs.remove("extirpater");
            prefs.flush(); //BackingStoreException on Linux
            return true;
        }catch(Exception e){
            return false;
        }
    }

    private Thread emptyRecycleBin(final File drive) {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Runtime rt = Runtime.getRuntime();
                    if((drive + "").equals("C:\\"))
                        rt.exec("cmd.exe @cmd /C \"rmdir /S /Q " + drive + "$Recycle.Bin\"");
                    else
                        rt.exec("cmd.exe @cmd /C \"rmdir /S /Q " + drive + "$RECYCLE.BIN\"");
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Thread wipeDriveFreeSpace(final File drive) {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Process p = new ProcessBuilder("cipher", "/W:" + drive).start();
                    Scanner s = new Scanner(p.getInputStream());
                    int driveNumber = 1337;
                    for(int x = 0; x < amtDrives; x++) {
                        if((drive + "").equals(getDrives(false).get(x))) {
                            driveNumber = x;
                        }
                    }
                    int x = 0;
                    try {
                        while(s.hasNext()) {
                            if (!isRunning) {
                                s.close();
                                p.destroy();
                            } else if (s.next().contains("Writing")) {
                                x++;
                                if (x - 1 == passes){
                                    s.close();
                                    p.destroy();
                                }
                                driveStatus[driveNumber] = (x * 25.0);
                            }
                         }
                    } catch(Exception e) {
                    }
                    driveStatus[driveNumber] = 100.0;
                    Thread.sleep(500);
                    Runtime rt = Runtime.getRuntime();
                    rt.exec("cmd.exe @cmd /C \"rmdir /S /Q " + drive + "EFSTMPWP\"");
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Thread updateStatus() {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    lblStatus.setText("Running");
                    double percentage = 0.0;
                    while (percentage < 100.0) {
                        percentage = 0;
                        for(Double d : driveStatus) {
                            if(d != null)
                                percentage += d;
                        }
                        percentage /= amtDrivesSelected;
                        percentage = Math.round(percentage * 100.0) / 100.0;
                        if(amtDrivesSelected == 0)
                            percentage = 100.0;
                        lblStatus.setText("Running, " + percentage + "%");
                        Thread.sleep(500);
                    }
                    lblStatus.setText("Waiting");
                    isRunning = false;
                    btnStart.setEnabled(true);
                    btnStart.setText("Extirpate!");
                    for (Checkbox c : chkDrives) {
                        c.setEnabled(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
