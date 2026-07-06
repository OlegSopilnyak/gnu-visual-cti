@echo off

rem to define the home of Dialogic's SDK
set DIALOGICDIR=E:\Program Files\Dialogic

rem to adjust the driver's includes & libs
set include=%include%;%DIALOGICDIR%\inc
set lib=%lib%;%DIALOGICDIR%\lib

nmake /C /D /S /f dialogic.mak CFG="dialogic - Win32 Release" ALL
if errorlevel 1 goto error
Copy .\Release\dialogic.dll ..\dialogic.dll > nul
nmake /C /D /S /f dialogic.mak CFG="dialogic - Win32 Release" CLEAN
Rmdir .\Release /s /q
:error
if errorlevel 1 	echo Can't make the dialogic.dll...
if not errorlevel 1 	echo Driver dialogic.dll has maked successful.