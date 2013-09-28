@REM Run swig to generate the C library
swig -java -package org.sleepydragon.capbutnbrightness.clib -outdir %~dp0..\src\org\sleepydragon\capbutnbrightness\clib -o %~dp0CLib.c %~dp0CLib.i 
