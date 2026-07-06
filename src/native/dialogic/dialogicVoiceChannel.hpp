/*
##############################################################################
##
##  DO NOT REMOVE THIS LICENSE AND COPYRIGHT NOTICE FOR ANY REASON
##
##############################################################################

GNU VisualCTI - A Java multi-platform Computer Telephony Application Server
Copyright (C) 2002 by Oleg Sopilnyak.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

Contact oleg@visualcti.org or gennady@visualcti.org for more information.

Ukraine point of contact: Oleg Sopilnyak - oleg@visualcti.org
Home Phone:	380-62-3851086 (russian)

USA point of contact: Justin Kuntz - jkuntz@prominic.com
Prominic Technologies, Inc.
PO Box 3233
Champaign, IL 61826-3233
Fax number: 217-356-3356
##############################################################################

*/
#include "javaCommon.h"
/*
 * class for manage of voice Dialogic features
 */
class dialogicVoiceChannel
{
private:
	/*
	 * call-back function, called when Dialogic recording the voice
	 */
	static int writeData
						(
						int handle,		// The handle on the open channel
						char *pointer,	// The pointer of the data
						unsigned count	// Quantity byte in the data area
						);
	/*
	 * call-back function, called when Dialogic play the sound
	 */
	static int readData
						(
						int handle,		// The handle on the open channel
						char *pointer,	// The pointer of the data
						unsigned count	// Quantity byte in the data area
										// (which is planned to receive)
						);
	/*
	 * call-back function, called when Dialogic seek to voice data
	 * for example when need erase the terminated DTMF signal
	 */
	static long seekData
						(
						int handle,	// handle to opened device
						long offset,// offset to new position
						int whence	// rules
						);
	/*
	 * to translate codec string to codec ID
	 * for prepare codec ID for play/record calls
	 */
	static int getCodecID(const char *Codec);

public:
	/*
	 * To receive an environment of the Java-virtual machine
	 */
	static JNIEnv *getJNIEnv();
	/*
	 * to initialize voice resource features
	 */
	static void Initialize();
	/*
	 * to start playback, returns 0 if success, or DX_ERROR, when can't start
	 */
	static int startPlay(int handle, const char *mask, int time, const char *codec);
	/*
	 * to start record, returns 0 if success, or DX_ERROR, when can't start
	 */
	static int startRecord(int handle, const char *mask, int silence, int time, const char *codec);
	/*
	 * to get access to available codecs for opened channel
	 */
	static void getAvailableCodecs(int handle, char *codecsList);
};

#define PM_ULAW     0x10000    // uLaw codec
#define LINEAR      0x20000    // PCM codec
#define PM_SR11     0x40000    // PCM 11 codec

#define ADPCM_8KHZ	"OKI/8000"
#define ADPCM_6KHZ	"OKI/6000"
#define PCM8_ALAW	"ALAW/8000"
#define PCM6_ALAW	"ALAW/6000"
#define PCM8_ULAW	"ULAW/8000"
#define PCM6_ULAW	"ULAW/6000"
#define PCM_8KHZ	"LINEAR/8000"
#define PCM_6KHZ	"LINEAR/6000"
#define PCM_11KHZ	"LINEAR/11025"

#define ULAW_PREFIX "ULAW"
#define ALAW_PREFIX "ALAW"
#define PCM_PREFIX "LINEAR"
#define DIALOGIC_PREFIX "OKI"

