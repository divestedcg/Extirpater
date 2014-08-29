package info.spotcomms.extirpater;

import java.awt.*;
import java.io.File;
import java.util.Random;
import java.util.Scanner;

/**
 * Created using IntelliJ IDEA
 * User: Tad
 * Date: 8/14/2014
 * Time: 4:24 AM
 */
public class Drive {

    private GUI parentInstance;
    private File drivePath;
    private String driveName;
    private double driveStatus;
    private Checkbox chkDrive;

    public Drive(GUI parentInstance, File drivePath, String driveName) {
        this.parentInstance = parentInstance;
        this.drivePath = drivePath;
        this.driveName = driveName;
        this.chkDrive = new Checkbox(drivePath + " - " + driveName);
    }

    public Checkbox getCheckbox() {
        return this.chkDrive;
    }

    public double getDriveStatus() {
        return this.driveStatus;
    }

    public void setDriveStatus(double driveStatus) {
        this.driveStatus = driveStatus;
    }

    public Thread wipeDriveFreeSpace(final boolean emptyTrash, final boolean fillUpFileTable, final int passes) {
        return new Thread(() -> {
            try {
                if(emptyTrash) {
                    Runtime rt = Runtime.getRuntime();
                    if (drivePath.equals(new File("C:\""))) {
                        new ProcessBuilder("rmdir", "/S /Q " + drivePath + "$Recycle.Bin").start();
                    } else {
                        new ProcessBuilder("rmdir", "/S /Q " + drivePath + "$RECYCLE.BIN").start();
                    }
                }
                if(fillUpFileTable) {
                    for (int x = 0; x < 100000; x++) {
                        File f = new File(drivePath + "Extirpater_Temp-" + getRandomString(10));
                        f.createNewFile();
                        f.delete();
                    }
                }
                Process p = new ProcessBuilder("cipher", "/W:" + drivePath).start();
                Scanner s = new Scanner(p.getInputStream());
                int passesCompleted = 0;
                try {
                    while (s.hasNext()) {
                        if (!parentInstance.isRunning()) {
                            s.close();
                            p.destroy();
                        } else if (s.next().contains("Writing")) {
                            passesCompleted++;
                            if ((passesCompleted - 1) == passes) {
                                s.close();
                                p.destroy();
                            }
                            driveStatus = (passesCompleted * 25.0);
                        }
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                    //Upon halting this will throw an error due to the scanner closing and the while loop still running
                }
                driveStatus = 100.0;
                Thread.sleep(500);
                new ProcessBuilder("cmd.exe", "@cmd /c \"rmdir /S /Q " + drivePath + "EFSTMPWP\"")
                    .start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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

}
