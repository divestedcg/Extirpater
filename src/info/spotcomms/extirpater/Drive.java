/*
 * Copyright (c) 2015. Spot Communications
 */

package info.spotcomms.extirpater;

import org.uncommons.maths.random.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
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

    private long freeSpace;

    private static int kilobyte = 1000;
    private static int megabyte = 1000000;

    private boolean running = false;
    private boolean finished = false;

    private SeedGenerator seedGenerator = null;
    private Random random = null;
    private Random secureRandom = null;

    private DecimalFormat df = new DecimalFormat("#.##");

    public Drive(GUI gui, File drivePath, String driveName, boolean unlimitedStrength) {
        this.gui = gui;
        this.drivePath = drivePath;
        this.extirpaterPath = new File(this.drivePath + "/Extirpater");
        this.driveName = driveName;
        if (this.driveName.equals("")) {
            this.lblDriveName = new JLabel(this.drivePath + "", JLabel.CENTER);
        } else {
            this.lblDriveName = new JLabel(this.driveName, JLabel.CENTER);
        }
        this.freeSpace = (drivePath.getFreeSpace() / megabyte);
        this.btnControl = new JButton("Start");
        this.btnControl.addActionListener(this);
        this.lblStatus = new JLabel("Idle", JLabel.CENTER);
        try {
            if (gui.os.equals("Linux") || gui.os.equals("Mac")) {
                seedGenerator = new DevRandomSeedGenerator();
            } else {
                seedGenerator = new SecureRandomSeedGenerator();
            }
            if (unlimitedStrength) {
                random = new CMWC4096RNG(seedGenerator.generateSeed(16384));
                secureRandom = new AESCounterRNG(seedGenerator.generateSeed(32));
            } else {
                System.out.println("Unlimited Strength Encryption is not available on this runtime");
                random = new MersenneTwisterRNG(seedGenerator.generateSeed(16));
                secureRandom = new AESCounterRNG(seedGenerator.generateSeed(16));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                    @Override public void run() {
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
                                }
                            }
                            boolean emptyTrash = false;
                            if (!gui.isAdmin) {
                                emptyTrash = false;
                            } else {
                                emptyTrash = gui.chkEmptyTrash.getState();
                            }
                            Thread mainThread = start(emptyTrash, fillFileTable, amtFillFileTable, gui.drpPasses.getSelectedIndex());
                            mainThread.start();
                            while (running) {
                                //Do nothing
                                Thread.sleep(1000);
                            }
                            if (!finished) {
                                mainThread.stop();
                                deleteTempFiles();
                                lblStatus.setText("Stopped");
                            }
                            deleteDirectory(extirpaterPath.toPath());
                            btnControl.setText("Start");
                            btnControl.setEnabled(true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
    }

    private Thread start(final boolean emptyTrash, final boolean fillFileTable, final int amtFillFileTable, final int passes) {
        return new Thread(new Runnable() {
            @Override public void run() {
                try {
                    lblStatus.setText("Starting...");
                    Thread.sleep(2500);
                    if (emptyTrash) {
                        emptyTrash();
                    }
                    if (fillFileTable) {
                        fillFileTable(amtFillFileTable, 1);
                        deleteTempFiles();
                    }
                    switch (passes) {
                        case 0:
                            break;
                        case 1:
                            eraseFreeSpace((byte) 0x00, 1);
                            deleteTempFiles();
                            break;
                        case 2:
                            eraseFreeSpace((byte) 0x42, 1);
                            deleteTempFiles();
                            break;
                        case 3:
                            eraseFreeSpace((byte) 0x00, 1);
                            deleteTempFiles();
                            eraseFreeSpace((byte) 0xFF, 2);
                            deleteTempFiles();
                            break;
                        case 4:
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
                    e.printStackTrace();
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
            if (gui.os.equals("Mac")) {
                new ProcessBuilder("rm", "-rf", "~/.Trash").start();
            }
            if (gui.os.equals("Linux")) {
                new ProcessBuilder("rm", "-rf", "~/.local/share/Trash").start();
            }
            lblStatus.setText("Emptied Trash");
            Thread.sleep(2500);
        } catch (Exception e) {
            e.printStackTrace();
            lblStatus.setText("Failed to empty trash");
            try {
                Thread.sleep(2500);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    private void fillFileTable(int amtFiles, int pass) {
        try {
            lblStatus.setText("Filling File Table, Pass " + pass + " of 2");
            for (int x = 0; x < amtFiles; x++) {
                File f = new File(extirpaterPath + "/Extirpater_Temp-" + getRandomString(5));
                f.createNewFile();
                lblStatus.setText("Filling File Table, Pass " + pass + " of 2, File: " + x);
            }
            lblStatus.setText("Filled File Table");
            Thread.sleep(2500);
        } catch (Exception e) {
            e.printStackTrace();
            lblStatus.setText("Failed to Fill File Table");
            try {
                Thread.sleep(2500);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    private void eraseFreeSpace(byte value, int pass) {
        try {
            try {
                double progress = 0;
                lblStatus.setText("Erasing, Pass: " + pass + ", Value: " + value);
                File tempFile = new File(extirpaterPath + "/Extirpater_Temp-" + getRandomString(5));
                tempFile.createNewFile();
                FileOutputStream fos = new FileOutputStream(tempFile);
                if (value != 0x42) {
                    byte[] twentyfiveMegabyteArray = null;
                    byte[] oneMegabyteArray = null;
                    byte[] oneKilobyteArray = null;
                    for (ByteArray byteArray : gui.byteArrays) {
                        if (byteArray.getValue() == value) {
                            if (byteArray.getLength() == megabyte * 25) {
                                twentyfiveMegabyteArray = byteArray.getByteArray();
                            }
                            if (byteArray.getLength() == megabyte) {
                                oneMegabyteArray = byteArray.getByteArray();
                            }
                            if (byteArray.getLength() == kilobyte) {
                                oneKilobyteArray = byteArray.getByteArray();
                            }
                        }
                    }
                    while (drivePath.getFreeSpace() / megabyte >= 25) {
                        fos.write(twentyfiveMegabyteArray);
                        progress = 100.0 - ((((double) (drivePath.getFreeSpace() / megabyte)) / freeSpace) * 100.0);
                        lblStatus.setText("Erasing, Pass: " + pass + ", Value: " + value + ", Progress: " + df.format(progress) + "%");
                    }
                    while (drivePath.getFreeSpace() / megabyte >= 1) {
                        fos.write(oneMegabyteArray);
                    }
                    while (drivePath.getFreeSpace() / kilobyte >= 1) {
                        fos.write(oneKilobyteArray);
                    }
                    while (drivePath.getFreeSpace() >= 1) {
                        fos.write(value);
                    }
                } else {
                    lblStatus.setText("Erasing, Pass: " + pass + ", Value: Random");
                    while (drivePath.getFreeSpace() / megabyte >= 25) {
                        fos.write(getRandomByteArray(megabyte * 25));
                        progress = 100.0 - ((((double) (drivePath.getFreeSpace() / megabyte)) / freeSpace) * 100.0);
                        lblStatus.setText("Erasing, Pass: " + pass + ", Value: Random" + ", Progress: " + df.format(progress) + "%");
                    }
                    while (drivePath.getFreeSpace() / megabyte >= 1) {
                        fos.write(getRandomByteArray(megabyte));
                    }
                    while (drivePath.getFreeSpace() / kilobyte >= 1) {
                        fos.write(getRandomByteArray(kilobyte));
                    }
                    while (drivePath.getFreeSpace() >= 1) {
                        fos.write(getRandomByteArray(1));
                    }
                }
                fos.flush();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            lblStatus.setText("Erased Free Space");
            Thread.sleep(2500);
        } catch (Exception e) {
            e.printStackTrace();
            lblStatus.setText("Failed to Erase Free Space");
            try {
                Thread.sleep(2500);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    private void deleteTempFiles() {
        try {
            System.gc();
            lblStatus.setText("Cleaning Up");
            for (File f : extirpaterPath.listFiles()) {
                if (f.isFile()) {
                    if ((f + "").contains("Extirpater_Temp-")) {
                        f.delete();
                    }
                }
            }
            lblStatus.setText("Cleaned Up");
            Thread.sleep(2500);
        } catch (Exception e) {
            e.printStackTrace();
            lblStatus.setText("Failed to Clean Up");
            try {
                Thread.sleep(2500);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    private String getRandomString(int length) {
        String base = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        String temp = "";
        for (int i = 0; i < length; i++) {
            int rn = random.nextInt(base.length());
            temp = temp + base.substring(rn, rn + 1);
        }
        return temp;
    }

    private byte[] getRandomByteArray(int length) {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return bytes;
    }

    //Credit: http://fahdshariff.blogspot.ru/2011/08/java-7-deleting-directory-by-walking.html
    private void deleteDirectory(Path dir) {
        try {
            System.gc();
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (exc == null) {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    } else {
                        throw exc;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
