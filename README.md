Extirpater
==========

What is free space erasing?
---------------------------
- Typically when you delete a file it is not really deleted, it is merely removed from the file system's index.
- A free space eraser tool such as this one fills the remaining space of your drive with random noise files and then deletes them.
- This process makes deleted files for the most part irrecoverable.

What is file table filling?
---------------------------
- On some file systems, deleted file names can still be accessible in backup index databases.
- By creating many tens or hundreds of thousands of empty files with different random names you can push out the old files.
- This process makes deleted file names for the most part irrecoverable.

Important Things #1
-------------------
- Drives over their life span slowly fail, and the internal controller will mark those spaces to no longer be used.
- These parts of the drive are inaccessible to the operating system.
- ATA Secure Erase can in theory erase these, but it is unknown how effective such process is.
- Physical destruction is the only fool-proof method to ensure erasure in such a failure mode.
- Full disk encryption is also a good protective measure for case of drive failure, among other things.

Important Things #2
-------------------
- Solid State Drives are quirky.
- You cannot secure erase a single file using a traditional file eraser.
- You can however in theory, if you trust your drive firmware, send an ATA discard to the files location and it should hopefully be "zeroed".
- You can set your file system to automatically do this for deleted files by setting the `discard` mount flag in your /etc/fstab.

Requirements
------------
- Java 8.0 x64
- Windows, Mac, or a common Linux distro
- 256MB when using .exe, when using .jar about 100MB + 25MB per drive or 125MB if filling file table

Instructions
------------
0. (Optional) Run BleachBit on Linux/Windows and OnyX on Mac to delete unnecessary files
1. Launch the program
2. Choose whatever options you want on the bottom
3. Click "Start" on any drives you want
4. The status of drives are shown on the side of them

Known Issues
------------
- Sometimes the window will appear, however it will be empty or parts missing. Wait a few seconds and try resizing it
- On all systems, emptying the trash requires admin/root
- On Mac and Linux systems, root is required to run on the system drive
- On Mac systems, if a drive isn't mounted in "/Volumes" it will not be detected
- On Linux systems, drives will only be detected if the "mount" command is available
- On Linux systems, the drive name will not always appear
- On Linux systems, only trash at /home/$USER will be deleted
- On Linux systems, beyond 26 drives SSD detection will fail

Planned Updates
---------------
- Better GUI
- Reduce CPU usage, Goal: <2% CPU usage on my dual L5639's, currently ~3-30% mostly ~5%
- Reduce memory usage, Goal: <256MB on all systems
- In depth pass options
- Verification
- Bug fixes
- SSD/Flash/Virtual/Compressed detection on Mac/Windows

Reasons for Creation || Issues with Similar Programs
----------------------------------------------------
- Erasing multiple drives at once
- Speed
- Multiplatform support

Credits
-------
- Joe aka Dr_fantasmo
- Icon: Google/Android/AOSP, License: Apache 2.0, https://google.github.io/material-design-icons/
- Uncommons Maths, License: Apache 2.0, https://maths.uncommons.org
- (CC BY-SA 3.0) https://en.wikipedia.org/wiki/Data_erasure
- (CC BY-SA 3.0) https://stackoverflow.com/a/23538961
- (CC BY-SA 3.0) https://stackoverflow.com/a/15608620
- (GPLv3) https://fahdshariff.blogspot.com/2011/08/java-7-deleting-directory-by-walking.html

Donate
-------
- https://divested.dev/donate
