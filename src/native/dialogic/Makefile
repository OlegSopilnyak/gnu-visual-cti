#
#
# VisualCTI Dialogic  native code maker
#

# SCTOOLS
SCTOOLSDIR=/usr/dialogic/sctools
SCTOOLS=$(SCTOOLSDIR)/sctools.o
SCINCS=$(SCTOOLSDIR)/sctools.h

# the root's directory of Java, definition
#JAVA_ROOT=/usr/java/j2sdk1.4.0
#JAVA_ROOT=/usr/java/jdk1.3
JAVA_ROOT=/opt/jdk1.4

# Compiler definition and flags
CC=g++
#CC=/usr/bin/gcc


# flags for compile
CFLAGS= -O -I$(SCTOOLSDIR) -DLINUX \
	-I/usr/dialogic/inc \
	-I/usr/include \
	-I$(JAVA_ROOT)/include \
	-I$(JAVA_ROOT)/include/linux \
	-L/usr/dialogic/lib

# the set of obj-files for make the DLL
OBJ=	ContextImpl.o \
	dialogicChannelImpl.o \
	dialogicChannelMakeCall.o \
	dialogicChannelSCBUS.o \
	dialogicDeviceCST.o \
	dialogicDeviceDTI.o \
	dialogicDeviceImpl.o \
	dialogicDeviceMakeCall.o \
	dialogicDeviceSignal.o \
	dialogicFaxChannel.o \
	dialogicDeviceVoice.o \
	dialogicJavaDevice.o \
	dialogicJavaFAX.o \
	dialogicJavaSCBUS.o \
	dialogicJavaVOX.o \
	dialogicSignalChannelImpl.o \
	dialogicVoiceChannelCallBack.o \
	dialogicVoiceChannelCodec.o \
	dialogicVoiceChannelImpl.o

OBJP= $(OBJ)
OBJS= $(OBJ)

# depedences for compile
INCL= 135.h \
	Context.hpp \
	org_visualcti_server_hardware_provider_dialogic_FAX.h \
	org_visualcti_server_hardware_provider_dialogic_Hardware.h \
	org_visualcti_server_hardware_provider_dialogic_SCBUS.h \
	org_visualcti_server_hardware_provider_dialogic_VOX.h \
	dialogicChannel.hpp \
	dialogicChannelInfo.hpp \
	dialogicChannelSCBUS.hpp \
	dialogicDevice.hpp \
	dialogicEvent.hpp \
	dialogicFaxChannel.hpp \
	dialogicLib.h \
	dialogicSignalChannel.hpp \
	dialogicVoiceChannel.hpp \
	javaCommon.h

# libraries for make the DLL
LIBS= -lfax -ldxxx -ldti -lsrl -lLiS -ldl
#LIBS= -ldti -ldxxx -lfax -lsrl -ldl
#-lLiS

# libray's name without prefix & suffix
DIALOGIC = dialogic

all:     $(DIALOGIC)

dialogic:$(OBJP) $(SCTOOLS)
	echo "******** Creating the dirver's library **************"
	$(CC) $(CFLAGS) $(OBJP) $(SCTOOLS) $(LIBS) -shared -o lib$(DIALOGIC).so
#$(CC) $(CFLAGS) $(OBJP) $(SCTOOLS) $(LIBS) -shared -nostdlib -o lib$(DIALOGIC).so
#	/bin/rm -f $(OBJS)

# to colpy the DLL to up level
	-/bin/cp -f lib$(DIALOGIC).so ../lib$(DIALOGIC).so


ContextImpl.o: ContextImpl.cpp $(INCL)
	echo "Compiling ContextImpl.cpp..."
	$(CC) -c $(CFLAGS) ContextImpl.cpp

dialogicChannelImpl.o: dialogicChannelImpl.cpp $(INCL)
	echo "Compiling dialogicChannelImp.cpp..."
	$(CC) -c $(CFLAGS) dialogicChannelImpl.cpp

dialogicChannelMakeCall.o: dialogicChannelMakeCall.cpp $(INCL)
	echo "Compiling dialogicChannelMakeCall.cpp..."
	$(CC) -c $(CFLAGS)  dialogicChannelMakeCall.cpp

dialogicChannelSCBUS.o: dialogicChannelSCBUS.cpp $(INCL)
	echo "Compiling dialogicChannelSCBUS.cpp..."
	$(CC) -c $(CFLAGS) dialogicChannelSCBUS.cpp

dialogicDeviceCST.o: dialogicDeviceCST.cpp $(INCL)
	echo "Compiling dialogicDeviceCST.cpp..."
	$(CC) -c $(CFLAGS) dialogicDeviceCST.cpp

dialogicDeviceDTI.o: dialogicDeviceDTI.cpp $(INCL)
	echo "Compiling dialogicDeviceDTI.cpp..."
	$(CC) -c $(CFLAGS) dialogicDeviceDTI.cpp

dialogicDeviceImpl.o: dialogicDeviceImpl.cpp $(INCL)
	echo "Compiling dialogicDeviceImpl.cpp..."
	$(CC) -c $(CFLAGS) dialogicDeviceImpl.cpp

dialogicDeviceMakeCall.o: dialogicDeviceMakeCall.cpp $(INCL)
	echo "Compiling dialogicDeviceMakeCall.cpp..."
	$(CC) -c $(CFLAGS) dialogicDeviceMakeCall.cpp

dialogicDeviceSignal.o: dialogicDeviceSignal.cpp $(INCL)
	echo "Compiling dialogicDeviceSignal.cpp..."
	$(CC) -c $(CFLAGS) dialogicDeviceSignal.cpp

dialogicFaxChannel.o: dialogicFaxChannel.cpp $(INCL)
	echo "Compiling dialogicFaxChannel.cpp..."
	$(CC) -c $(CFLAGS) dialogicFaxChannel.cpp

dialogicDeviceVoice.o: dialogicDeviceVoice.cpp $(INCL)
	echo "Compiling dialogicDeviceVoice.cpp..."
	$(CC) -c $(CFLAGS) dialogicDeviceVoice.cpp

dialogicJavaDevice.o: dialogicJavaDevice.cpp $(INCL)
	echo "Compiling dialogicJavaDevice.cpp..."
	$(CC) -c $(CFLAGS) dialogicJavaDevice.cpp

dialogicJavaFAX.o: dialogicJavaFAX.cpp $(INCL)
	echo "Compiling dialogicJavaFAX.cpp..."
	$(CC) -c $(CFLAGS) dialogicJavaFAX.cpp

dialogicJavaSCBUS.o: dialogicJavaSCBUS.cpp $(INCL)
	echo "Compiling dialogicJavaSCBUS.cpp..."
	$(CC) -c $(CFLAGS) dialogicJavaSCBUS.cpp

dialogicJavaVOX.o: dialogicJavaVOX.cpp $(INCL)
	echo "Compiling dialogicJavaVOX.cpp..."
	$(CC) -c $(CFLAGS) dialogicJavaVOX.cpp

dialogicSignalChannelImpl.o: dialogicSignalChannelImpl.cpp $(INCL)
	echo "Compiling dialogicSignalChannelImpl.cpp..."
	$(CC) -c $(CFLAGS) dialogicSignalChannelImpl.cpp

dialogicVoiceChannelCallBack.o: dialogicVoiceChannelCallBack.cpp $(INCL)
	echo "Compiling dialogicVoiceChannelCallBack.cpp..."
	$(CC) -c $(CFLAGS) dialogicVoiceChannelCallBack.cpp

dialogicVoiceChannelCodec.o: dialogicVoiceChannelCodec.cpp $(INCL)
	echo "Compiling dialogicVoiceChannelCodec.cpp..."
	$(CC) -c $(CFLAGS) dialogicVoiceChannelCodec.cpp

dialogicVoiceChannelImpl.o: dialogicVoiceChannelImpl.cpp $(INCL)
	echo "Compiling dialogicVoiceChannelImpl.cpp..."
	$(CC) -c $(CFLAGS) dialogicVoiceChannelImpl.cpp

# to clean the compiler/linker results
clean:
	/bin/rm -f $(OBJS)
	/bin/rm -f lib$(DIALOGIC).so
