#!/bin/bash

if [ $# -ne 2 ]
then
    echo "usage: $0 <video_path> <sdcard_path>"
    echo "  e.g. $0 ~/Downloads/cops.mp4 /Volumes/NOOKSD"
    exit 1
fi 

infile=$1
name=`basename "${infile%.*}"`
outroot=$2

if [ ! -f $infile ]
then
    echo "$infile does not exist"
    exit 1
fi

if [ ! -d $outroot ]
then
    echo "$outroot does not exist"
    exit 1
fi

outdir="$outroot/$name"
if [ -d $outdir ]
then
    echo "deleting $outdir"
    rm -rf $outdir
fi
mkdir $outdir


###############################################################
echo "extracting images"

# scale:
#  screen is 800x600, so scale down if the resolution is larger than that,
#  to not waste storage space with large images, and pad with black to maintain the aspect ratio.
#  (TODO: should we upscale also?  how does quality compare to runtime upscaling?)

# fps:
#  fps=24 will dump one frame per image.  ("frame" is arbitrary but 24 is standard for modern-ish movies.)
#  to reduce screen flickers and storage requirements, we dump fps=1
#  note that SD card is a FAT32 file system has a limit of number of files per directory (65534 according to the web, and lower for names longer than 8.3)

# keep this 8.3 to make more efficient use of FAT32 file system; also this must match code
filepat=img%05d

ffmpeg -i "$infile" \
    -vf scale=800:600:force_original_aspect_ratio=decrease,pad=800:600:-1:-1:color=black,fps=1 \
    "$outdir/${filepat}.png"

    #-frames:v 1 \ # limit frames


###############################################################
echo "converting images"

# optimize images for 4-bit grayscale display

# generate palette
convert -size 256x256 -colorspace gray -depth 4 gradient: palette.png

for img in $outdir/img*.png
do
    echo $img
    convert $img -remap palette.png $outdir/`basename $img .png`.jpg
    rm $img
done
