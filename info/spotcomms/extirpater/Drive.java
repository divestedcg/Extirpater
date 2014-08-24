package info.spotcomms.extirpater;

import java.awt.*;
import java.io.File;
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

    public Thread emptyDriveRecycleBin() {
        return new Thread(() -> {
            try {
                Runtime rt = Runtime.getRuntime();
                if(drivePath.equals(new File("C:\"")))
                    new ProcessBuilder("rmdir", "/S /Q " + drivePath + "$Recycle.Bin").start();
                else
                    new ProcessBuilder("rmdir", "/S /Q " + drivePath + "$RECYCLE.BIN").start();
            } catch(Exception e) {
                e.printStackTrace();
            }
        });
    }

    public Thread wipeDriveFreeSpace(final int passes) {
        return new Thread(() -> {
            try {
                Process p = new ProcessBuilder("cipher", "/W:" + drivePath).start();
                Scanner s = new Scanner(p.getInputStream());
                int passesCompleted = 0;
                try {
                    while (s.hasNext()) {
                        if (!parentInstance.isRunning()) {
                            s.close();
                            p.destroy();
                        } else if(s.next().contains("Writing")) {
                            passesCompleted++;
                            if ((passesCompleted - 1) == passes) {
                                s.close();
                                p.destroy();
                            }
                            driveStatus = (passesCompleted * 25.0);
                        }
                    }
                } catch(Exception e) {
                    //e.printStackTrace();
                    //Upon halting this will throw an error due to the scanner closing and the while loop still running
                }
                driveStatus = 100.0;
                Thread.sleep(500);
                new ProcessBuilder("cmd.exe", "@cmd /c \"rmdir /S /Q " + drivePath + "EFSTMPWP\"").start();
            } catch(Exception e) {
                e.printStackTrace();
            }
        });
    }

}
