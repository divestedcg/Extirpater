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
    private final Checkbox chkEmptyRecycleBins = new Checkbox("Empty Recycle Bins");
    private final JButton btnStart = new JButton("Extirpate!");
    private final JLabel lblStatus = new JLabel("Waiting", JLabel.CENTER);

    private boolean isAdmin = false;
    private int amtDrives = 0;
    private int amtDrivesSelected = 0;
    private Double[] driveStatus = new Double[1337];

    public GUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        add(panel);
        isAdmin = isAdmin();
        amtDrives = getDrives(false).size();
        panel.setLayout(new GridLayout(amtDrives + 3, 1));
        for(int x = 0; x < amtDrives; x++) {
            chkDrives.add(new Checkbox(getDrives(true).get(x)));
            panel.add(chkDrives.get(x));
        }
        panel.add(chkEmptyRecycleBins); chkEmptyRecycleBins.setEnabled(isAdmin); if(!isAdmin) chkEmptyRecycleBins.setLabel(chkEmptyRecycleBins.getLabel() + " (admin only)");
        panel.add(btnStart); btnStart.addActionListener(this);
        panel.add(lblStatus);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnStart) {
            for(Checkbox c : chkDrives) {
                if(c.getState()) {
                    amtDrivesSelected++;
                    if(chkEmptyRecycleBins.getState()) {
                        emptyRecycleBin(new File(c.getLabel().substring(0, 3))).start();
                        System.out.println("Emptied Recycle Bin of drive " + c.getLabel().substring(0, 3));
                    }
                    wipeDriveFreeSpace(new File(c.getLabel().substring(0, 3))).start();
                    System.out.println("Started wiping free space of drive " + c.getLabel().substring(0, 3));
                }
            }
            updateStatus().start();
            for(Checkbox c: chkDrives) {
                c.setEnabled(false);
            }
            btnStart.setEnabled(false);
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
                    Runtime rt = Runtime.getRuntime();
                    Process p = rt.exec("cmd.exe @cmd /C \"cipher /W:" + drive + " \"");
                    Scanner s = new Scanner(p.getInputStream());
                    int driveNumber = 1337;
                    for(int x = 0; x < amtDrives; x++) {
                        if((drive + "").equals(getDrives(false).get(x))) {
                            driveNumber = x;
                        }
                    }
                    int x = 0;
                    while(s.hasNext()) {
                        if(s.next().contains("Writing")) {
                            x++;
                            driveStatus[driveNumber] = (x*25.0);
                        }
                    }
                    driveStatus[driveNumber] = 100.0;
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
                        lblStatus.setText("Running, " + percentage + "%");
                        Thread.sleep(500);
                    }
                    lblStatus.setText("Finished");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
