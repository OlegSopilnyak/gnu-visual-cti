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

Contact oleg.sopilnyak@gmail.com or gennady@visualcti.org for more information.

Ukraine point of contact: Oleg Sopilnyak - oleg.sopilnyak@gmail.com
Home Phone:	+380-63-8420220 (russian)

USA point of contact: Justin Kuntz - jkuntz@prominic.com
Prominic Technologies, Inc.
PO Box 3233
Champaign, IL 61826-3233
Fax number: 217-356-3356
##############################################################################

*/
package org.visualcti.core.channel.telephony.operation.adapter;

import org.visualcti.core.channel.telephony.operation.ToneId;

/**
 * Telephony Tone: The telephony's tone parameters wrapper
 *
 * @see ToneId
 */
public class TelephonyTone {
    private final ToneId id;
    private int toneId = 0;
    // parameters for primary frequency of the tone
    private final Frequency primary = new Frequency(0, 0);
    // parameters for secondary frequency of the tone
    private final Frequency secondary = new Frequency(0, 0);
    // parameters for the duration of the tone
    private final Duration duration = new Duration(0, 0, 0, 0);
    // signals repetition count for the tone's detection
    private int repetitionCount = 0;

    public TelephonyTone(ToneId id) {
        this(id, "");
    }

    public TelephonyTone(ToneId id, String description) {
        this.id = id;
        parseTone(description);
    }

    /**
     * <builder>
     * To build the tone from the definition as string (example "257,500,200,500,200,55,40,55,40,4")
     *
     * @param definition definition of the tone
     */
    protected void parseTone(final String definition) {
        String[] parts = definition.split(",");
        for (int partCode = 0; partCode < parts.length; partCode++) {
            final int partValue = parseInt(parts[partCode]);
            switch (partCode) {
                case 0:
                    this.toneId = partValue;
                    break;
                case 1:
                    this.primary.frequencyHz = partValue;
                    break;
                case 2:
                    this.primary.deviationHz = partValue;
                    break;
                case 3:
                    this.secondary.frequencyHz = partValue;
                    break;
                case 4:
                    this.secondary.deviationHz = partValue;
                    break;
                case 5:
                    this.duration.onTime = partValue;
                    break;
                case 6:
                    this.duration.onTimeDeviation = partValue;
                    break;
                case 7:
                    this.duration.offTime = partValue;
                    break;
                case 8:
                    this.duration.offTimeDeviation = partValue;
                    break;
                case 9:
                    this.repetitionCount = partValue;
                    break;
            }
        }
    }

    public ToneId getId() {
        return id;
    }

    public int getToneId() {
        return toneId;
    }

    public Frequency getPrimary() {
        return primary;
    }

    public Frequency getSecondary() {
        return secondary;
    }

    public Duration getDuration() {
        return duration;
    }

    public int getRepetitionCount() {
        return repetitionCount;
    }

    public void setRepetitionCount(int repetitionCount) {
        this.repetitionCount = repetitionCount;
    }

    @Override
    public String toString() {
        return toneId + "," + primary + "," + secondary + "," + duration + "," + repetitionCount;
    }

    /**
     * Tone's frequency
     *
     */
    public static class Frequency {
        private int frequencyHz;
        private int deviationHz;

        public Frequency(int frequencyHz, int deviationHz) {
            this.frequencyHz = frequencyHz;
            this.deviationHz = deviationHz;
        }

        public int getFrequencyHz() {
            return frequencyHz;
        }

        public void setFrequencyHz(int frequencyHz) {
            this.frequencyHz = frequencyHz;
        }

        public int getDeviationHz() {
            return deviationHz;
        }

        public void setDeviationHz(int deviationHz) {
            this.deviationHz = deviationHz;
        }

        @Override
        public String toString() {
            return frequencyHz + "," + deviationHz;
        }
    }

    /**
     * Tone's duration the values is in milliseconds
     *
     */
    public static class Duration {
        private int onTime;
        private int onTimeDeviation;
        private int offTime;
        private int offTimeDeviation;

        public Duration(int onTime, int onTimeDeviation, int offTime, int offTimeDeviation) {
            this.onTime = onTime;
            this.onTimeDeviation = onTimeDeviation;
            this.offTime = offTime;
            this.offTimeDeviation = offTimeDeviation;
        }

        public int getOnTime() {
            return onTime;
        }

        public void setOnTime(int onTime) {
            this.onTime = onTime;
        }

        public int getOnTimeDeviation() {
            return onTimeDeviation;
        }

        public void setOnTimeDeviation(int onTimeDeviation) {
            this.onTimeDeviation = onTimeDeviation;
        }

        public int getOffTime() {
            return offTime;
        }

        public void setOffTime(int offTime) {
            this.offTime = offTime;
        }

        public int getOffTimeDeviation() {
            return offTimeDeviation;
        }

        public void setOffTimeDeviation(int offTimeDeviation) {
            this.offTimeDeviation = offTimeDeviation;
        }

        @Override
        public String toString() {
            return onTime + "," + onTimeDeviation + "," + offTime + "," + offTimeDeviation;
        }
    }


    /// private methods
    private static int parseInt(final String part) {
        try {
            return Integer.parseInt(part.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
