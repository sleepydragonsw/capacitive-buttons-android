#!/bin/bash -xe

###############################################################################
# create_brightness_buttons.sh
# By: Denver Coneybeare <denver@sleepydragon.org>
# May 30, 2015
#
# This script invokes a handful of ImageMagick commands to generate the PNG
# images for the main activity's brightness buttons.  This script can be run
# from the top-level directory of the project to write the files in-place.
###############################################################################

convert \
    -fill white \
    -stroke white \
    '(' \
        -size 1000x1000 \
        xc:transparent \
        -draw "polyline 500,0 1000,500 500,1000 0,500" \
        -draw "rectangle 146,146 854,854" \
    ')' \
    '(' \
        -size 1000x1000 \
        xc:transparent \
        -fill green \
        -draw "circle 500,500 300,300" \
    ')' \
    -compose dstout \
    -composite \
    app/src/main/res/drawable/btn_off.png
