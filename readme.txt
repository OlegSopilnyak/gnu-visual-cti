In this directory located main files of the system
scripts:
runVisualCTI.IDE.sh - to launch the IDE under Linux
runVisualCTI.control.sh - to launch the server's ControlPanel under Linux
runVisualCTI.server.sh - to launch the VisualCTI's server under Linux
rebulildVisualCTI.sh - to rebuild Java & C++ parts under Linux

runVisualCTI.server.bat - to launch the VisualCTI's server under Win32
rebuildVisualCTI.bat - to rebuild Java & C++ parts under Win32

configurations:
visualcti.policy - the Java policy file for the server
VisualCTI.server.xml - the configuration of the server
ServerSecurity.xml - VisualCTI server's security (users,group,permissions)
VisualCTI.control.xml - the configuration of server's ControlPanel
VisualCTI.workflow.xml - the configuration of VisualCTI IDE

licence:
visualcti.licence - this file contains the rules of VisualCTI's distributions.

dynamic libraries:
libdialogic.so - the library for support the Dialogic's hardware under Linux
dialogic.dll - the library for support the Dialogic's hardware under Win32

directories:
class:
directory contains the files to launch the parts of the system
java:
directory contains the Java's sources of the system
and scripts for make the Java part.
Please read java/readmy.txt
lib:
directory contains the set of JARs (Java libraries)
log:
directory, where the server store the logs of server's parts
native:
directory contains C++ parts of server
and scripts for build the dynamic libraries
Please read native/readmy.txt
persistent.messages:
In this directory server's messaging system will store undelivered messages
sample:
This directory contains the sample of IDE's programm
And the file for make the database in MySQL server
tasks:
directory contains the Tasks pools for server's channels
VM:
directory contains the prompts for make the CallCenter & VoiceMail system
