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
#include "dialogicVoiceChannel.hpp"
#include "Context.hpp"
#include <stdlib.h>
/*
 * To receive number - list codecs of the channel
 * The coded information about supported
 * record coders
 */
inline
int getCoders(int handle) 
{
	Context *context = Context::getContext(handle);// get context by handle
	if (context == NULL) return 0;// mistake (invalid handle)

	int codecs = context->features.ft_record;// get coders set value
	if( context->devinfo.ct_devfamily == CT_DFD41D ) codecs = codecs & 0xffef;
	return codecs;
}
/*
 * to add codec descriptor to full codecs list
 */
static
void addCodec ( char *list, char *codecInfo ) 
{
	strcat(list,codecInfo); strcat(list,"\n");
}
/*
 * to resolve supported codecs by codecIDs mask
 */
void processCodecs(char *list,int codes)
{
    if((codes & FT_ADPCM) != 0)// Dialogic OKI
    {
        if((codes & FT_DRT8KHZ) != 0) addCodec(list,ADPCM_8KHZ);// 8000 Hz
        if((codes & FT_DRT6KHZ) != 0) addCodec(list,ADPCM_6KHZ);// 6000 Hz
    }
    if((codes & FT_PCM) != 0)// PCM
    {
        if((codes & FT_ALAW) != 0)// ALAW PCM
        {
            if((codes & FT_DRT8KHZ) != 0) addCodec(list,PCM8_ALAW);// 8000 Hz
            if((codes & FT_DRT6KHZ) != 0) addCodec(list,PCM6_ALAW);// 6000 Hz
        }
        if((codes & FT_ULAW) != 0)// ULAW PCM
        {
            if((codes & FT_DRT8KHZ) != 0) addCodec(list,PCM8_ULAW);// 8000 Hz
            if((codes & FT_DRT6KHZ) != 0) addCodec(list,PCM6_ULAW);// 6000 Hz
        }
        if((codes & FT_LINEAR) != 0)// LINEAR PCM
        {
            if((codes & FT_DRT8KHZ) != 0) addCodec(list,PCM_8KHZ);// 8000 Hz
            if((codes & FT_DRT6KHZ) != 0) addCodec(list,PCM_6KHZ);// 6000 Hz
            if((codes & FT_DRT11KHZ) != 0) addCodec(list,PCM_11KHZ);// 11025 Hz
        }
    }
}
/*
 * to get access to available channel codecs
 */
void dialogicVoiceChannel::getAvailableCodecs
								(
								int handle,
								char *codecsList
								)
{
	codecsList[0] = 0;// truncute string
	int codecsID = ::getCoders( handle );
	if (codecsID != 0) ::processCodecs(codecsList,codecsID);
}
/*
 * to  make codecID from codec's string
 */
int dialogicVoiceChannel::getCodecID(const char *Codec)
{
char *delimiter = strchr( Codec, '/');// try find slash
int samplerate= -1;
int codec = DX_ERROR;
	// check codec string format
	if (delimiter == NULL) return DX_ERROR;// not valid codec's string
	*delimiter = 0; samplerate = atoi(delimiter+1);//get sample rate

	// resolve codec string
	if (strcmp(Codec, ULAW_PREFIX) == 0)
	{	// ULAW
		codec = MD_PCM|PM_ULAW;
		if (samplerate == 6000) codec |= PM_SR6;
		else if (samplerate == 8000) codec |= PM_SR8;
		else codec = DX_ERROR;
	} else
	if (strcmp(Codec, ALAW_PREFIX) == 0)
	{	// ALAW
		codec = MD_PCM|PM_ALAW;
		if (samplerate == 6000) codec |= PM_SR6;
		else if (samplerate == 8000) codec |= PM_SR8;
		else codec = DX_ERROR;
	} else
	if (strcmp(Codec, PCM_PREFIX) == 0)
	{	// LINEAR
		codec = MD_PCM|LINEAR;
		if (samplerate == 6000) codec |= PM_SR6;
		else if (samplerate == 8000) codec |= PM_SR8;
		else if (samplerate == 11025) codec |= PM_SR11;
		else codec = DX_ERROR;
	}else
	if (strcmp(Codec, DIALOGIC_PREFIX) == 0)
	{	// OKI
		if (samplerate == 6000) codec = PM_SR6;
		else if (samplerate == 8000) codec = PM_SR8;
		else codec = DX_ERROR;
	}
	return codec; // codec ID
}
