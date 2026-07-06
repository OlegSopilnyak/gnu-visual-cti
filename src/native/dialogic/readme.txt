This directory contains the source of dynamic library for Dialogic's hardware
and the scripts for make it.

Scripts:
makeDriver.bat - to build the library under Win32.
Important!
Need to adjust the path to Dialogic's SDK (DIALOGICDIR)
dialogic.mak - this is make-file from the Visual Studio,
for make the Dialogic's library under Win32

Makefile - this is Linux's make-file for make the library under Linux
Important!
Need to adjust:
 the path to Dialogic's SCTOOLS (SCTOOLSDIR)
 the path to Java's SDK (JAVA_ROOT)
 the path to C++ compiler (CC)