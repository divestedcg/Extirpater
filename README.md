Extirpater
==========

Requirements
------------
- Java 8.0
- Windows 2000 and higher, including server versions
- 70MB of RAM, +1MB for each drive

Instructions
------------
1. Launch the program
2. Select the drives you want to erase the free space on
3. Select the amount of passes you want, the more passes the more through
4. Click "Extirpate!"
5. Wait a few hours, larger drives take longer
6. The status of the process will be shown on the bottom of the window

Notes
-----
- Halting whilst running, may take some time and in some instances it is quicker to close the program and start over

Reasoning for Creation
----------------------
There are many programs that have the ability to erase free space, but everyone I've found had the same issue. They weren't able to erase the free space of multiple drives at once. I personally liked to run a one pass erase on all my drives using Eraser at least once a week, however Eraser would take up to 15 hours per drive! So I looked around more, and stumbled upon cipher.exe, a program thats included with Windows. Although cipher.exe isn't meant to erase free space, it has the capability to do so, and you can open multiple instances of it to do multiple drives at once. So for the first week of using it, it was great, it went through my drives (4.5TB) overnight, the only issue being that all the Command Prompts took up a whole monitor (5 drives, 1 cmd each). So I created this program, a frontend for "cipher.exe /w", that is compact, and easy to use.

Credits
-------
- JTattoo
- Microsoft
- http://stackoverflow.com/a/23538961
- http://stackoverflow.com/a/15608620
