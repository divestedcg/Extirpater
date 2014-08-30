package info.spotcomms.extirpater;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Created using IntelliJ IDEA
 * User: Tad
 * Date: 8/14/2014
 * Time: 4:24 AM
 */
public class Drive implements ActionListener {

    private GUI gui;
    private File drivePath;
    private File extirpaterPath;
    private String driveName;
    private JLabel lblDriveName;
    private JButton btnControl;
    private JLabel lblStatus;

    private static int bite = 1;
    private static int kilobyte = 1000;
    private static int megabyte = 1000000;

    private boolean running = false;
    private boolean finished = false;

    public Drive(GUI gui, File drivePath, String driveName) {
        this.gui = gui;
        this.drivePath = drivePath;
        this.extirpaterPath = new File(this.drivePath + "/Extirpater");
        this.driveName = driveName;
        if (this.driveName.equals(""))
            this.lblDriveName = new JLabel(this.drivePath + "", JLabel.CENTER);
        else
            this.lblDriveName = new JLabel(this.driveName, JLabel.CENTER);
        this.btnControl = new JButton("Start");
        this.btnControl.addActionListener(this);
        this.lblStatus = new JLabel("Idle", JLabel.CENTER);
    }

    public JLabel getLblDriveName() {
        return this.lblDriveName;
    }

    public JButton getBtnControl() {
        return this.btnControl;
    }

    public JLabel getLblStatus() {
        return this.lblStatus;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnControl) {
            if (running) {
                running = false;
                btnControl.setText("Stopping");
                btnControl.setEnabled(false);
            } else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            running = true;
                            finished = false;
                            btnControl.setText("Stop");
                            extirpaterPath.mkdir();
                            boolean fillFileTable = (gui.drpFillFileTable.getSelectedIndex() >= 1);
                            int amtFillFileTable = 0;
                            if (fillFileTable) {
                                switch (gui.drpFillFileTable.getSelectedIndex()) {
                                    case 0:
                                        amtFillFileTable = 0;
                                        break;
                                    case 1:
                                        amtFillFileTable = 10000;
                                        break;
                                    case 2:
                                        amtFillFileTable = 100000;
                                        break;
                                    case 3:
                                        amtFillFileTable = 1000000;
                                        break;
                                    case 4:
                                        amtFillFileTable = 1000000000;
                                        break;
                                }
                            }
                            boolean emptyTrash = false;
                            if (!gui.isAdmin)
                                emptyTrash = false;
                            else
                                emptyTrash = gui.chkEmptyTrash.getState();
                            Thread mainThread = start(emptyTrash, fillFileTable, amtFillFileTable, gui.drpPasses.getSelectedIndex() + 1);
                            mainThread.start();
                            while (running) {
                                //Do nothing
                                Thread.sleep(500);
                            }
                            if (!finished) {
                                mainThread.stop();
                                deleteTempFiles();
                                lblStatus.setText("Stopped");
                            }
                            deleteDirectory(extirpaterPath);
                            btnControl.setText("Start");
                            btnControl.setEnabled(true);
                        } catch (Exception e) {
                            //e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
    }

    public Thread start(final boolean emptyTrash, final boolean fillFileTable, final int amtFillFileTable, final int passes) {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    lblStatus.setText("Starting...");
                    Thread.sleep(5000);
                    if (emptyTrash) {
                        emptyTrash();
                    }
                    if (fillFileTable) {
                        fillFileTable(amtFillFileTable, 1);
                        deleteTempFiles();
                    }
                    switch (passes) {
                        case 1:
                            eraseFreeSpace((byte) 0x00, 1);
                            deleteTempFiles();
                            break;
                        case 2:
                            eraseFreeSpace((byte) 0x00, 1);
                            deleteTempFiles();
                            eraseFreeSpace((byte) 0xFF, 2);
                            deleteTempFiles();
                            break;
                        case 3:
                            eraseFreeSpace((byte) 0x00, 1);
                            deleteTempFiles();
                            eraseFreeSpace((byte) 0xFF, 2);
                            deleteTempFiles();
                            eraseFreeSpace((byte) 0x42, 3);
                            deleteTempFiles();
                            break;
                    }
                    if (fillFileTable) {
                        fillFileTable(amtFillFileTable, 2);
                        deleteTempFiles();
                    }
                    deleteTempFiles();
                    running = false;
                    finished = true;
                    lblStatus.setText("Finished!");
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        });
    }

    private void emptyTrash() {
        try {
            lblStatus.setText("Emptying Trash");
            if (gui.os.equals("Windows")) {
                if (drivePath.equals(new File("C:\""))) {
                    new ProcessBuilder("rmdir", "/S", "/Q", drivePath + "$Recycle.Bin").start();
                } else {
                    new ProcessBuilder("rmdir", "/S", "/Q", drivePath + "$RECYCLE.BIN").start();
                }
            }
            if (gui.os.equals("Mac OS")) {
                Runtime rt = Runtime.getRuntime();
                new ProcessBuilder("rm", "-rf", "~/.Trash/*").start();
            }
            if (gui.os.equals("Linux")) {
                new ProcessBuilder("rm", "-rf", "~/.Trash/*").start();
                new ProcessBuilder("rm", "-rf", "~/.local/share/Trash/files/*").start();
            }
            lblStatus.setText("Emptied Trash");
            Thread.sleep(5000);
        } catch (Exception e) {
            //e.printStackTrace();
            lblStatus.setText("Failed to empty trash");
            try {
                Thread.sleep(5000);
            } catch (Exception e1) {
            }
        }
    }

    private void fillFileTable(int amtFiles, int pass) {
        try {
            lblStatus.setText("Filling File Table, Pass " + pass + " of 2");
            for (int x = 0; x < amtFiles; x++) {
                File f = new File(extirpaterPath + "/Extirpater_Temp-" + getRandomString(229));
                f.createNewFile();
                if (x % 1000 == 0)
                    lblStatus.setText("Filling File Table, Pass " + pass + " of 2, File: " + x);
            }
            lblStatus.setText("Filled File Table");
            Thread.sleep(5000);
        } catch (Exception e) {
            //e.printStackTrace();
            lblStatus.setText("Failed to Fill File Table");
            try {
                Thread.sleep(5000);
            } catch (Exception e1) {
            }
        }
    }

    private void eraseFreeSpace(byte value, int pass) {
        try {
            if (value == 0x42)
                lblStatus.setText("Erasing Free Space, Pass: " + pass + ", Value: Random");
            else
                lblStatus.setText("Erasing Free Space, Pass: " + pass + ", Value: " + value);
            try {
                while ((drivePath.getFreeSpace() / megabyte) >= 100) {
                    File file = new File(extirpaterPath + "/Extirpater_Temp-" + getRandomString(229));
                    file.createNewFile();
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    if (value == 0x42)
                        fileOutputStream.write(getRandomByteArray(megabyte));
                    else
                        fileOutputStream.write(getByteArray(value, megabyte));
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            } catch (Exception e) {
                //Possible out of space exception
            }
            try {
                while ((drivePath.getFreeSpace() / kilobyte) >= 1) {
                    File file = new File(extirpaterPath + "/Extirpater_Temp-" + getRandomString(229));
                    file.createNewFile();
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    if (value == 0x42)
                        fileOutputStream.write(getRandomByteArray(kilobyte));
                    else
                        fileOutputStream.write(getByteArray(value, kilobyte));
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            } catch (Exception e) {
                //Possible out of space exception
            }
            try {
                while ((drivePath.getFreeSpace() / bite) >= 1) {
                    File file = new File(extirpaterPath + "/Extirpater_Temp-" + getRandomString(229));
                    file.createNewFile();
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    if (value == 0x42)
                        fileOutputStream.write(getRandomByteArray(bite));
                    else
                        fileOutputStream.write(getByteArray(value, bite));
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            } catch (Exception e) {
                //Possible out of space exception
            }
            lblStatus.setText("Erased Free Space");
            Thread.sleep(5000);
        } catch (Exception e) {
            //e.printStackTrace();
            lblStatus.setText("Failed to Erase Free Space");
            try {
                Thread.sleep(5000);
            } catch (Exception e1) {
            }
        }
    }

    public void deleteTempFiles() {
        try {
            lblStatus.setText("Cleaning Up");
            for (int x = 0; x < 1000; x++) {
                File[] allRootFiles = extirpaterPath.listFiles();
                for (File f : allRootFiles) {
                    if (f.isFile()) {
                        if ((f + "").contains("Extirpater_Temp-")) {
                            f.delete();
                        }
                    }
                }
            }
            lblStatus.setText("Cleaned Up");
            Thread.sleep(5000);
        } catch (Exception e) {
            //e.printStackTrace();
            lblStatus.setText("Failed to Clean Up");
            try {
                Thread.sleep(5000);
            } catch (Exception e1) {
            }
        }
    }

    private String getRandomString(int length) {
        String base = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        String temp = "";
        for (int i = 0; i < length; i++) {
            int rn = new Random(System.currentTimeMillis() - Math
                    .round((Math.random() * 1000 * Math.random()) / Math.PI)).nextInt(base.length());
            temp = temp + base.substring(rn, rn + 1);
        }
        return temp;
    }

    private byte[] getByteArray(int b, int length) {
        byte[] bytes = new byte[length];
        for (int x = 0; x < length; x++) {
            bytes[x] = (byte) b;
        }
        return bytes;
    }

    private byte[] getRandomByteArray(int length) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return bytes;
    }

    private void deleteDirectory(File dir) {
        try {
            File[] files = dir.listFiles();
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteDirectory(f);
                    f.delete();
                } else
                    f.delete();
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

}
