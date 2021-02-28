# Very Slow Movie Player for Nook Simple Touch

This project was inspired by [this article](https://makezine.com/projects/the-very-slow-movie-player/) in Make Magazine. That design uses a Raspberry Pi and an e-ink display, but I happened to have an old Nook Simple Touch (NST) e-reader in my closet, so I decided to try to build one with that.

Shout out to the folks at [the XDA NST forums](https://forum.xda-developers.com/c/barnes-noble-nook-touch.1198/) for their help!

## Equipment:
 - A Nook Simple Touch; the going rate on eBay seems to be about $30
 - A MicroSD card
 - A picture frame large and deep enough to hold the NST

## Factory reset your NST (optional)

My NST was prone to random crashes, and doing a factory reset seemed to fix it.  If you want to do this, the best way is to use the n2T-Recovery_0.2.img (from the XDA forums, but also here in the img folder).  More detail can be found in those forums, but in brief:
 - Mount the SD card on your computer (e.g. using an SD card reader)
 - Write the img file to the SD card:

    sudo dd if=./img/n2T-Recovery_0.2.img of=/dev/disk<n> bs=1m

   where `<n>` is replaced with the number of your disk device.  (On OSX you can find that with `diskutil list`.)
 - Put the card in your NST and reboot

## Root your NST

This is done with NookManager, also available from the XDA forums but also here in the img folder.
- Mount the SD card on your computer
- Write the img file:
    sudo dd if=./img/NookManager.img of=/dev/disk<n> bs=1m
- **If your NST has been updated to firmware 1.2.2**, you will need to edit scripts/install_nookmods and scripts/install_old_installer on the SD card; change the line


    if [ "$SYSTEM" != "1.2.0" -a "$SYSTEM" != "1.2.1" ]; then

to 

    if [ "$SYSTEM" != "1.2.0" -a "$SYSTEM" != "1.2.1" -a "$SYSTEM" != "1.2.2"]; then


- Put the card in your NST and reboot

