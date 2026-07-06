# Microsoft Developer Studio Generated NMAKE File, Based on dialogic.dsp
!IF "$(CFG)" == ""
CFG=dialogic - Win32 Debug
!MESSAGE No configuration specified. Defaulting to dialogic - Win32 Debug.
!ENDIF 

!IF "$(CFG)" != "dialogic - Win32 Release" && "$(CFG)" != "dialogic - Win32 Debug"
!MESSAGE Invalid configuration "$(CFG)" specified.
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "dialogic.mak" CFG="dialogic - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "dialogic - Win32 Release" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE "dialogic - Win32 Debug" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE 
!ERROR An invalid configuration is specified.
!ENDIF 

!IF "$(OS)" == "Windows_NT"
NULL=
!ELSE 
NULL=nul
!ENDIF 

CPP=cl.exe
MTL=midl.exe
RSC=rc.exe

!IF  "$(CFG)" == "dialogic - Win32 Release"

OUTDIR=.\Release
INTDIR=.\Release
# Begin Custom Macros
OutDir=.\Release
# End Custom Macros

ALL : "$(OUTDIR)\dialogic.dll"


CLEAN :
	-@erase "$(INTDIR)\ContextImpl.obj"
	-@erase "$(INTDIR)\dialogicChannelImpl.obj"
	-@erase "$(INTDIR)\dialogicChannelMakeCall.obj"
	-@erase "$(INTDIR)\dialogicChannelSCBUS.obj"
	-@erase "$(INTDIR)\dialogicDeviceCST.obj"
	-@erase "$(INTDIR)\dialogicDeviceDTI.obj"
	-@erase "$(INTDIR)\dialogicDeviceImpl.obj"
	-@erase "$(INTDIR)\dialogicDeviceMakeCall.obj"
	-@erase "$(INTDIR)\dialogicDeviceSignal.obj"
	-@erase "$(INTDIR)\dialogicDeviceVoice.obj"
	-@erase "$(INTDIR)\dialogicFaxChannel.obj"
	-@erase "$(INTDIR)\dialogicJavaDevice.obj"
	-@erase "$(INTDIR)\dialogicJavaFAX.obj"
	-@erase "$(INTDIR)\dialogicJavaSCBUS.obj"
	-@erase "$(INTDIR)\dialogicJavaVOX.obj"
	-@erase "$(INTDIR)\dialogicSignalChannelImpl.obj"
	-@erase "$(INTDIR)\dialogicVoiceChannelCallBack.obj"
	-@erase "$(INTDIR)\dialogicVoiceChannelCodec.obj"
	-@erase "$(INTDIR)\dialogicVoiceChannelImpl.obj"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(OUTDIR)\dialogic.dll"
	-@erase "$(OUTDIR)\dialogic.exp"
	-@erase "$(OUTDIR)\dialogic.lib"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP_PROJ=/nologo /MT /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "DIALOGIC_EXPORTS" /Fp"$(INTDIR)\dialogic.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 
MTL_PROJ=/nologo /D "NDEBUG" /mktyplib203 /win32 
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\dialogic.bsc" 
BSC32_SBRS= \
	
LINK32=link.exe
LINK32_FLAGS=kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib jvm.lib libsrlmt.lib libdxxmt.lib libfaxmt.lib libdtimt.lib sctools.lib /nologo /dll /incremental:no /pdb:"$(OUTDIR)\dialogic.pdb" /machine:I386 /nodefaultlib:"LIBC" /out:"$(OUTDIR)\dialogic.dll" /implib:"$(OUTDIR)\dialogic.lib" 
LINK32_OBJS= \
	"$(INTDIR)\ContextImpl.obj" \
	"$(INTDIR)\dialogicChannelImpl.obj" \
	"$(INTDIR)\dialogicChannelMakeCall.obj" \
	"$(INTDIR)\dialogicChannelSCBUS.obj" \
	"$(INTDIR)\dialogicDeviceCST.obj" \
	"$(INTDIR)\dialogicDeviceDTI.obj" \
	"$(INTDIR)\dialogicDeviceImpl.obj" \
	"$(INTDIR)\dialogicDeviceMakeCall.obj" \
	"$(INTDIR)\dialogicDeviceSignal.obj" \
	"$(INTDIR)\dialogicDeviceVoice.obj" \
	"$(INTDIR)\dialogicFaxChannel.obj" \
	"$(INTDIR)\dialogicJavaDevice.obj" \
	"$(INTDIR)\dialogicJavaFAX.obj" \
	"$(INTDIR)\dialogicJavaSCBUS.obj" \
	"$(INTDIR)\dialogicJavaVOX.obj" \
	"$(INTDIR)\dialogicSignalChannelImpl.obj" \
	"$(INTDIR)\dialogicVoiceChannelCallBack.obj" \
	"$(INTDIR)\dialogicVoiceChannelCodec.obj" \
	"$(INTDIR)\dialogicVoiceChannelImpl.obj"

"$(OUTDIR)\dialogic.dll" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ELSEIF  "$(CFG)" == "dialogic - Win32 Debug"

OUTDIR=.\Debug
INTDIR=.\Debug
# Begin Custom Macros
OutDir=.\Debug
# End Custom Macros

ALL : "$(OUTDIR)\dialogic.dll" "$(OUTDIR)\dialogic.bsc"


CLEAN :
	-@erase "$(INTDIR)\ContextImpl.obj"
	-@erase "$(INTDIR)\ContextImpl.sbr"
	-@erase "$(INTDIR)\dialogicChannelImpl.obj"
	-@erase "$(INTDIR)\dialogicChannelImpl.sbr"
	-@erase "$(INTDIR)\dialogicChannelMakeCall.obj"
	-@erase "$(INTDIR)\dialogicChannelMakeCall.sbr"
	-@erase "$(INTDIR)\dialogicChannelSCBUS.obj"
	-@erase "$(INTDIR)\dialogicChannelSCBUS.sbr"
	-@erase "$(INTDIR)\dialogicDeviceCST.obj"
	-@erase "$(INTDIR)\dialogicDeviceCST.sbr"
	-@erase "$(INTDIR)\dialogicDeviceDTI.obj"
	-@erase "$(INTDIR)\dialogicDeviceDTI.sbr"
	-@erase "$(INTDIR)\dialogicDeviceImpl.obj"
	-@erase "$(INTDIR)\dialogicDeviceImpl.sbr"
	-@erase "$(INTDIR)\dialogicDeviceMakeCall.obj"
	-@erase "$(INTDIR)\dialogicDeviceMakeCall.sbr"
	-@erase "$(INTDIR)\dialogicDeviceSignal.obj"
	-@erase "$(INTDIR)\dialogicDeviceSignal.sbr"
	-@erase "$(INTDIR)\dialogicDeviceVoice.obj"
	-@erase "$(INTDIR)\dialogicDeviceVoice.sbr"
	-@erase "$(INTDIR)\dialogicFaxChannel.obj"
	-@erase "$(INTDIR)\dialogicFaxChannel.sbr"
	-@erase "$(INTDIR)\dialogicJavaDevice.obj"
	-@erase "$(INTDIR)\dialogicJavaDevice.sbr"
	-@erase "$(INTDIR)\dialogicJavaFAX.obj"
	-@erase "$(INTDIR)\dialogicJavaFAX.sbr"
	-@erase "$(INTDIR)\dialogicJavaSCBUS.obj"
	-@erase "$(INTDIR)\dialogicJavaSCBUS.sbr"
	-@erase "$(INTDIR)\dialogicJavaVOX.obj"
	-@erase "$(INTDIR)\dialogicJavaVOX.sbr"
	-@erase "$(INTDIR)\dialogicSignalChannelImpl.obj"
	-@erase "$(INTDIR)\dialogicSignalChannelImpl.sbr"
	-@erase "$(INTDIR)\dialogicVoiceChannelCallBack.obj"
	-@erase "$(INTDIR)\dialogicVoiceChannelCallBack.sbr"
	-@erase "$(INTDIR)\dialogicVoiceChannelCodec.obj"
	-@erase "$(INTDIR)\dialogicVoiceChannelCodec.sbr"
	-@erase "$(INTDIR)\dialogicVoiceChannelImpl.obj"
	-@erase "$(INTDIR)\dialogicVoiceChannelImpl.sbr"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\vc60.pdb"
	-@erase "$(OUTDIR)\dialogic.bsc"
	-@erase "$(OUTDIR)\dialogic.dll"
	-@erase "$(OUTDIR)\dialogic.exp"
	-@erase "$(OUTDIR)\dialogic.ilk"
	-@erase "$(OUTDIR)\dialogic.lib"
	-@erase "$(OUTDIR)\dialogic.pdb"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP_PROJ=/nologo /MTd /W3 /Gm /GX /ZI /Od /D "WIN32" /D "_DEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "DIALOGIC_EXPORTS" /FR"$(INTDIR)\\" /Fp"$(INTDIR)\dialogic.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /GZ /c 
MTL_PROJ=/nologo /D "_DEBUG" /mktyplib203 /win32 
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\dialogic.bsc" 
BSC32_SBRS= \
	"$(INTDIR)\ContextImpl.sbr" \
	"$(INTDIR)\dialogicChannelImpl.sbr" \
	"$(INTDIR)\dialogicChannelMakeCall.sbr" \
	"$(INTDIR)\dialogicChannelSCBUS.sbr" \
	"$(INTDIR)\dialogicDeviceCST.sbr" \
	"$(INTDIR)\dialogicDeviceDTI.sbr" \
	"$(INTDIR)\dialogicDeviceImpl.sbr" \
	"$(INTDIR)\dialogicDeviceMakeCall.sbr" \
	"$(INTDIR)\dialogicDeviceSignal.sbr" \
	"$(INTDIR)\dialogicDeviceVoice.sbr" \
	"$(INTDIR)\dialogicFaxChannel.sbr" \
	"$(INTDIR)\dialogicJavaDevice.sbr" \
	"$(INTDIR)\dialogicJavaFAX.sbr" \
	"$(INTDIR)\dialogicJavaSCBUS.sbr" \
	"$(INTDIR)\dialogicJavaVOX.sbr" \
	"$(INTDIR)\dialogicSignalChannelImpl.sbr" \
	"$(INTDIR)\dialogicVoiceChannelCallBack.sbr" \
	"$(INTDIR)\dialogicVoiceChannelCodec.sbr" \
	"$(INTDIR)\dialogicVoiceChannelImpl.sbr"

"$(OUTDIR)\dialogic.bsc" : "$(OUTDIR)" $(BSC32_SBRS)
    $(BSC32) @<<
  $(BSC32_FLAGS) $(BSC32_SBRS)
<<

LINK32=link.exe
LINK32_FLAGS=jvm.lib libsrlmt.lib libdxxmt.lib libfaxmt.lib libdtimt.lib sctools.lib kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /dll /incremental:yes /pdb:"$(OUTDIR)\dialogic.pdb" /debug /machine:I386 /nodefaultlib:"LIBC" /out:"$(OUTDIR)\dialogic.dll" /implib:"$(OUTDIR)\dialogic.lib" /pdbtype:sept 
LINK32_OBJS= \
	"$(INTDIR)\ContextImpl.obj" \
	"$(INTDIR)\dialogicChannelImpl.obj" \
	"$(INTDIR)\dialogicChannelMakeCall.obj" \
	"$(INTDIR)\dialogicChannelSCBUS.obj" \
	"$(INTDIR)\dialogicDeviceCST.obj" \
	"$(INTDIR)\dialogicDeviceDTI.obj" \
	"$(INTDIR)\dialogicDeviceImpl.obj" \
	"$(INTDIR)\dialogicDeviceMakeCall.obj" \
	"$(INTDIR)\dialogicDeviceSignal.obj" \
	"$(INTDIR)\dialogicDeviceVoice.obj" \
	"$(INTDIR)\dialogicFaxChannel.obj" \
	"$(INTDIR)\dialogicJavaDevice.obj" \
	"$(INTDIR)\dialogicJavaFAX.obj" \
	"$(INTDIR)\dialogicJavaSCBUS.obj" \
	"$(INTDIR)\dialogicJavaVOX.obj" \
	"$(INTDIR)\dialogicSignalChannelImpl.obj" \
	"$(INTDIR)\dialogicVoiceChannelCallBack.obj" \
	"$(INTDIR)\dialogicVoiceChannelCodec.obj" \
	"$(INTDIR)\dialogicVoiceChannelImpl.obj"

"$(OUTDIR)\dialogic.dll" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ENDIF 

.c{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cpp{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cxx{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.c{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cpp{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cxx{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<


!IF "$(CFG)" == "dialogic - Win32 Release" || "$(CFG)" == "dialogic - Win32 Debug"
SOURCE=.\ContextImpl.cpp

!IF  "$(CFG)" == "dialogic - Win32 Release"


"$(INTDIR)\ContextImpl.obj" : $(SOURCE) "$(INTDIR)"


!ELSEIF  "$(CFG)" == "dialogic - Win32 Debug"


"$(INTDIR)\ContextImpl.obj"	"$(INTDIR)\ContextImpl.sbr" : $(SOURCE) "$(INTDIR)"


!ENDIF 

SOURCE=.\dialogicChannelImpl.cpp

!IF  "$(CFG)" == "dialogic - Win32 Release"


"$(INTDIR)\dialogicChannelImpl.obj" : $(SOURCE) "$(INTDIR)"


!ELSEIF  "$(CFG)" == "dialogic - Win32 Debug"


"$(INTDIR)\dialogicChannelImpl.obj"	"$(INTDIR)\dialogicChannelImpl.sbr" : $(SOURCE) "$(INTDIR)"


!ENDIF 

SOURCE=.\dialogicChannelMakeCall.cpp

!IF  "$(CFG)" == "dialogic - Win32 Release"


"$(INTDIR)\dialogicChannelMakeCall.obj" : $(SOURCE) "$(INTDIR)"


!ELSEIF  "$(CFG)" == "dialogic - Win32 Debug"


"$(INTDIR)\dialogicChannelMakeCall.obj"	"$(INTDIR)\dialogicChannelMakeCall.sbr" : $(SOURCE) "$(INTDIR)"


!ENDIF 

SOURCE=.\dialogicChannelSCBUS.cpp

!IF  "$(CFG)" == "dialogic - Win32 Release"


"$(INTDIR)\dialogicChannelSCBUS.obj" : $(SOURCE) "$(INTDIR)"


!ELSEIF  "$(CFG)" == "dialogic - Win32 Debug"


"$(INTDIR)\dialogicChannelSCBUS.obj"	"$(INTDIR)\dialogicChannelSCBUS.sbr" : $(SOURCE) "$(INTDIR)"


!ENDIF 

SOURCE=.\dialogicDeviceCST.cpp

!IF  "$(CFG)" == "dialogic - Win32 Release"


"$(INTDIR)\dialogicDeviceCST.obj" : $(SOURCE) "$(INTDIR)"


!ELSEIF  "$(CFG)" == "dialogic - Win32 Debug"


"$(INTDIR)\dialogicDeviceCST.obj"	"$(INTDIR)\dialogicDeviceCST.sbr" : $(SOURCE) "$(INTDIR)"


!ENDIF 

SOURCE=.\dialogicDeviceDTI.cpp

!IF  "$(CFG)" == "dialogic - Win32 Release"


"$(INTDIR)\dialogicDeviceDTI.obj" : $(SOURCE) "$(INTDIR)"


!ELSEIF  "$(CFG)" == "dialogic - Win32 Debug"


"$(INTDIR)\dialogicDeviceDTI.obj"	"$(INTDIR)\dialogicDeviceDTI.sbr" : $(SOURCE) "$(INTDIR)"


!ENDIF 

SOURCE=.\dialogicDeviceImpl.cpp

!IF  "$(CFG)" == "dialogic - Win32 Release"


"$(INTDIR)\dialogicDeviceImpl.obj" : $(SOURCE) "$(INTDIR)"


!ELSEIF  "$(CFG)" == "dialogic - Win32 Debug"


"$(INTDIR)\dialogicDeviceImpl.obj"	"$(INTDIR)\dialogicDeviceImpl.sbr" : $(SOURCE) "$(INTDIR)"


!ENDIF 

SOURCE=.\dialogicDeviceMakeCall.cpp

!IF  "$(CFG)" == "dialogic - Win32 Release"


"$(INTDIR)\dialogicDeviceMakeCall.obj" : $(SOURCE) "$(INTDIR)"


!ELSEIF  "$(CFG)" == "dialogic - Win32 Debug"


"$(INTDIR)\dialogicDeviceMakeCall.obj"	"$(INTDIR)\dialogicDeviceMakeCall.sbr" : $(SOURCE) "$(INTDIR)"


!ENDIF 

SOURCE=.\dialogicDeviceSignal.cpp

!IF  "$(CFG)" == "dialogic - Win32 Release"


"$(INTDIR)\dialogicDeviceSignal.obj" : $(SOURCE) "$(INTDIR)"


!ELSEIF  "$(CFG)" == "dialogic - Win32 Debug"


"$(INTDIR)\dialogicDeviceSignal.obj"	"$(INTDIR)\dialogicDeviceSignal.sbr" : $(SOURCE) "$(INTDIR)"


!ENDIF 

SOURCE=.\dialogicDeviceVoice.cpp

!IF  "$(CFG)" == "dialogic - Win32 Release"


"$(INTDIR)\dialogicDeviceVoice.obj" : $(SOURCE) "$(INTDIR)"


!ELSEIF  "$(CFG)" == "dialogic - Win32 Debug"


"$(INTDIR)\dialogicDeviceVoice.obj"	"$(INTDIR)\dialogicDeviceVoice.sbr" : $(SOURCE) "$(INTDIR)"


!ENDIF 

SOURCE=.\dialogicFaxChannel.cpp

!IF  "$(CFG)" == "dialogic - Win32 Release"


"$(INTDIR)\dialogicFaxChannel.obj" : $(SOURCE) "$(INTDIR)"


!ELSEIF  "$(CFG)" == "dialogic - Win32 Debug"


"$(INTDIR)\dialogicFaxChannel.obj"	"$(INTDIR)\dialogicFaxChannel.sbr" : $(SOURCE) "$(INTDIR)"


!ENDIF 

SOURCE=.\dialogicJavaDevice.cpp

!IF  "$(CFG)" == "dialogic - Win32 Release"


"$(INTDIR)\dialogicJavaDevice.obj" : $(SOURCE) "$(INTDIR)"


!ELSEIF  "$(CFG)" == "dialogic - Win32 Debug"


"$(INTDIR)\dialogicJavaDevice.obj"	"$(INTDIR)\dialogicJavaDevice.sbr" : $(SOURCE) "$(INTDIR)"


!ENDIF 

SOURCE=.\dialogicJavaFAX.cpp

!IF  "$(CFG)" == "dialogic - Win32 Release"


"$(INTDIR)\dialogicJavaFAX.obj" : $(SOURCE) "$(INTDIR)"


!ELSEIF  "$(CFG)" == "dialogic - Win32 Debug"


"$(INTDIR)\dialogicJavaFAX.obj"	"$(INTDIR)\dialogicJavaFAX.sbr" : $(SOURCE) "$(INTDIR)"


!ENDIF 

SOURCE=.\dialogicJavaSCBUS.cpp

!IF  "$(CFG)" == "dialogic - Win32 Release"


"$(INTDIR)\dialogicJavaSCBUS.obj" : $(SOURCE) "$(INTDIR)"


!ELSEIF  "$(CFG)" == "dialogic - Win32 Debug"


"$(INTDIR)\dialogicJavaSCBUS.obj"	"$(INTDIR)\dialogicJavaSCBUS.sbr" : $(SOURCE) "$(INTDIR)"


!ENDIF 

SOURCE=.\dialogicJavaVOX.cpp

!IF  "$(CFG)" == "dialogic - Win32 Release"


"$(INTDIR)\dialogicJavaVOX.obj" : $(SOURCE) "$(INTDIR)"


!ELSEIF  "$(CFG)" == "dialogic - Win32 Debug"


"$(INTDIR)\dialogicJavaVOX.obj"	"$(INTDIR)\dialogicJavaVOX.sbr" : $(SOURCE) "$(INTDIR)"


!ENDIF 

SOURCE=.\dialogicSignalChannelImpl.cpp

!IF  "$(CFG)" == "dialogic - Win32 Release"


"$(INTDIR)\dialogicSignalChannelImpl.obj" : $(SOURCE) "$(INTDIR)"


!ELSEIF  "$(CFG)" == "dialogic - Win32 Debug"


"$(INTDIR)\dialogicSignalChannelImpl.obj"	"$(INTDIR)\dialogicSignalChannelImpl.sbr" : $(SOURCE) "$(INTDIR)"


!ENDIF 

SOURCE=.\dialogicVoiceChannelCallBack.cpp

!IF  "$(CFG)" == "dialogic - Win32 Release"


"$(INTDIR)\dialogicVoiceChannelCallBack.obj" : $(SOURCE) "$(INTDIR)"


!ELSEIF  "$(CFG)" == "dialogic - Win32 Debug"


"$(INTDIR)\dialogicVoiceChannelCallBack.obj"	"$(INTDIR)\dialogicVoiceChannelCallBack.sbr" : $(SOURCE) "$(INTDIR)"


!ENDIF 

SOURCE=.\dialogicVoiceChannelCodec.cpp

!IF  "$(CFG)" == "dialogic - Win32 Release"


"$(INTDIR)\dialogicVoiceChannelCodec.obj" : $(SOURCE) "$(INTDIR)"


!ELSEIF  "$(CFG)" == "dialogic - Win32 Debug"


"$(INTDIR)\dialogicVoiceChannelCodec.obj"	"$(INTDIR)\dialogicVoiceChannelCodec.sbr" : $(SOURCE) "$(INTDIR)"


!ENDIF 

SOURCE=.\dialogicVoiceChannelImpl.cpp

!IF  "$(CFG)" == "dialogic - Win32 Release"


"$(INTDIR)\dialogicVoiceChannelImpl.obj" : $(SOURCE) "$(INTDIR)"


!ELSEIF  "$(CFG)" == "dialogic - Win32 Debug"


"$(INTDIR)\dialogicVoiceChannelImpl.obj"	"$(INTDIR)\dialogicVoiceChannelImpl.sbr" : $(SOURCE) "$(INTDIR)"


!ENDIF 


!ENDIF 

