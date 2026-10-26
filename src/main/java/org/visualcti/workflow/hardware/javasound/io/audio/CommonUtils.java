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
package org.visualcti.workflow.hardware.javasound.io.audio;

import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.visualcti.util.Tools;
import org.visualcti.workflow.hardware.javasound.SoundCardHandle;

/**
 * Provider Facade Part:Class-Utility: The telephony service provider facade 'common used functions'
 * (for the JavaSound implementation)
 *
 * @see PlaybackUtils
 * @see CaptureUtils
 */
abstract class CommonUtils {
    // the size of buffer that is using for media-transmitting operations
    protected static final int BUFFER_SIZE = 2048;
    private static final String QUEUE_IS_FULL = "AfterParty queue is full!!!";
    private static final Map<SoundCardHandle, BlockingQueue<Runnable>> afterParty = new ConcurrentHashMap<>();

    /**
     * <action>
     * To wait for operation isn't completed
     *
     * @param <H>    sound-card device handle type
     * @param handle the handle of the opened resource (sound card device's handle)
     */
    public static <H extends SoundCardHandle> void waitForOperationComplete(final H handle) {
        while (handle.isOperationInProgress()) {
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                Tools.error("Operation completing is interrupted");
                e.printStackTrace(Tools.err);
                /* Clean up whatever needs to be handled before interrupting  */
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * <test-action>
     * To complete audio data capturing and wait for it
     *
     * @param <H>            sound-card device handle type
     * @param handle         the handle of the opened resource (sound card device's handle)
     * @param afterRecording what it has to do after operation complete
     * @apiNote (for tests purposes only)
     */
    public static <H extends SoundCardHandle> void completeOperation(final H handle, final Runnable afterRecording) {
        final BlockingQueue<Runnable> completedAction = afterParty.get(handle);
        if (completedAction != null && !completedAction.offer(afterRecording)) {
            Tools.error(QUEUE_IS_FULL);
            return;
        }
        // waiting for operation isn't complete
        waitForOperationComplete(handle);
    }

    protected static <H extends SoundCardHandle> BlockingQueue<Runnable> getAfterPartyQueue(H handle) throws IOException {
        final BlockingQueue<Runnable> afterPartQueue = afterParty.get(handle);
        if (afterPartQueue == null) {
            // something went wrong
            throw new IOException("Sound capturing is started in a wrong way.");
        }
        return afterPartQueue;
    }

    protected static void postOperation(BlockingQueue<Runnable> afterPartQueue) throws IOException {
        try {
            // executing after operation activity if any
            if (!afterPartQueue.isEmpty()) {
                afterPartQueue.take().run();
            }
        } catch (InterruptedException e) {
            /* Clean up whatever needs to be handled before interrupting  */
            Thread.currentThread().interrupt();
            throw new IOException("Runnable Queue taking is interrupted.", e);
        }
    }

    // to mark operation for handle as started
    protected static <H extends SoundCardHandle> void startOperation(H handle) {
        handle.inProgress(true);
        final BlockingQueue<Runnable> previous = afterParty.put(handle, new ArrayBlockingQueue<>(1, true));
        if (previous != null) {
            final Runnable afterComplete = () -> Tools.error("Start Operation, Lost Runnable");
            // freeing previous queue
            if (!previous.offer(afterComplete)) {
                Tools.error(QUEUE_IS_FULL);
            }
        }
    }

    // adjusting playing volume value from handle
    protected static <H extends SoundCardHandle> void adjustPlayingVolume(final H handle, final SourceDataLine line) {
        if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            // current value
            final float volume = handle.getSourceVolume();
            try {
                final FloatControl gainControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);

                // Минимальное и максимальное значение в децибелах
                final float min = gainControl.getMinimum(); // например, -80.0f (почти тишина)
                final float max = gainControl.getMaximum(); // например, 6.0f (максимум)

                // Устанавливаем желаемое значение в децибелах
                // Например, устанавливаем среднее или конкретное значение
                gainControl.setValue(volume < min ? min : Math.min(volume, max));
            } catch (IllegalArgumentException e) {
                Tools.error("Cannot set volume for master gain " + volume);
                e.printStackTrace(Tools.err);
            }
        }
    }

    // adjusting recording volume value from handle
    protected static <H extends SoundCardHandle> void adjustRecordingVolume(final H handle, final TargetDataLine line) {
        // TODO implement it further
    }

    // to mark operation for handle as completed
    protected static <H extends SoundCardHandle> void operationComplete(H handle) {
        // operation is completed
        handle.inProgress(false);
        // removing capturing operation's runnable
        final BlockingQueue<Runnable> previous = afterParty.remove(handle);
        if (previous != null) {
            final Runnable afterComplete = () -> Tools.error("Complete Operation, Lost Runnable");
            // freeing previous queue
            if (!previous.offer(afterComplete)) {
                Tools.error(QUEUE_IS_FULL);
            }
        }
    }

    protected CommonUtils() {
    }
}
