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
package org.visualcti.server.hardware.provider.soundcard;

import org.visualcti.server.hardware.*;
import org.visualcti.briquette.*;
import org.visualcti.media.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, <br></p>
 * <p>Copyright: Copyright (c) 2003 Prominic Technologies, Inc.  & Prominic Ukraine Co</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

final class soundDeviceUI extends JPanel
{
private final soundDevice device;
private final JLabel state = new JLabel();
private final callPanel callControl;
private final dtmfPanel dtmfControl;
private final JPanel voiceControl;
private final JPanel faxControl;
  public soundDeviceUI(soundDevice device)
  {
    super(new BorderLayout(),true);
    this.device=device;
    this.callControl = new callPanel();
    this.dtmfControl = new dtmfPanel();
    this.voiceControl= new voicePanel();
    this.faxControl  = new faxPanel();
    super.add(this.state,BorderLayout.NORTH);
    this.state.setBorder(new TitledBorder("State of device"));
    this.state.setFont(new Font("sansserif",Font.BOLD,14));
    JTabbedPane modes = new JTabbedPane(JTabbedPane.BOTTOM);
    modes.setFont(new Font("dialog",Font.BOLD,9));
    super.add(modes,BorderLayout.CENTER);
    modes.addTab("call",callControl);
    modes.addTab("dtmf",dtmfControl);
    modes.addTab("voice",voiceControl);
    modes.addTab("fax",faxControl);
  }
  public final void setState(String state)
  {
    this.state.setText(state);
  }
  public final void open()
  {
    synchronized( this.dtmfBuffer ) {this.dtmfBuffer.setText("");}
  }
  final void online()
  {
    this.callControl.setEnabled(true);
    this.dtmfControl.setEnabled(true);
    this.voiceControl.setEnabled(true);
    this.faxControl.setEnabled(true);
  }
  final void offline()
  {
    this.callControl.setEnabled(false);
    this.dtmfControl.setEnabled(false);
    this.voiceControl.setEnabled(false);
    this.faxControl.setEnabled(false);
  }
  final void waitCall()
  {
    this.originate.setEditable(false);
    this.caller.setEditable( true );
    this.calling.setEditable( false );
    this.calling.setText( this.originate.getText() );
  }
  final void makeCall(String target)
  {
    this.originate.setEditable(false);
    this.caller.setEditable( true );this.caller.setEnabled( true );
    this.calling.setEditable( false );
    this.caller.setText( this.originate.getText() );
    this.calling.setText( target );
    if ( this.callControl.makeCall.autoAnswer.isSelected() )
    {
      this.callControl.makeCall.answerCall();
    }
  }
private JTextField originate;
  final void setOriginate(String number){this.originate.setText(number);}
  final String getOriginate(){return this.originate.getText();}
private JTextField caller;
  final void setCaller(String number){this.caller.setText(number);}
  final String getCaller(){return this.caller.getText();}
private JTextField calling;
  final void setCalling(String number){this.calling.setText(number);}
  final String getCalling(){return this.calling.getText();}

private final class callPanel extends JPanel{
    JTextField state;
    JButton dropCall;
    waitCallPanel waitCall;
    makeCallPanel makeCall;
    callParamPanel callPar;
    private callPanel(){
      super(new BorderLayout(),true);
      originate = new JTextField(12);
      caller = new JTextField(12);
      calling = new JTextField(12);
      JPanel status = new JPanel(new BorderLayout(),false);
      waitCall = new waitCallPanel();
      makeCall = new makeCallPanel();
      callPar = new callParamPanel();
      super.add(status,BorderLayout.NORTH);
      this.state = new JTextField("OFFLINE");
      this.state.setBorder(null);
      this.state.setFont(new Font("sanserif",Font.BOLD,12));
      status.add(this.state,BorderLayout.CENTER);
      this.dropCall = new JButton("Drop");
      status.add(this.dropCall,BorderLayout.EAST);
      this.dropCall.addActionListener(new ActionListener(){
        public final void actionPerformed(ActionEvent e){device.dropCall();}
      });
      JPanel cc = new JPanel(false);
      super.add(cc,BorderLayout.CENTER);
      cc.setLayout(new BoxLayout(cc,BoxLayout.Y_AXIS));
      cc.add(Box.createVerticalStrut(10));
      cc.add( this.callPar );
      cc.add( this.waitCall );
      cc.add(this.makeCall);
    }
    private final class callParamPanel extends JPanel{
      callParamPanel(){super(false);
        super.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        this.addCallNumber("Origin", originate, this);
        this.addCallNumber("Caller", caller, this);
        this.addCallNumber("Calling", calling, this);
      }
      private final void addCallNumber(String title, JTextField field,JPanel cc){
        JPanel pane = new JPanel(new FlowLayout(), false);
        pane.add(new JLabel(title)); pane.add(field); cc.add(pane);
        cc.add(Box.createVerticalStrut(4));
      }
      public final void setEnabled(boolean enabled){
        originate.setEnabled( !enabled );
        caller.setEnabled( !enabled );
        calling.setEnabled( !enabled );
      }
    }
    private final class waitCallPanel extends JPanel{
      JButton ring = new JButton("Ring");
      waitCallPanel(){
        super(new FlowLayout(FlowLayout.CENTER),false);
        super.add(this.ring);
        super.setBorder(new TitledBorder("WaitCall"));
        this.ring.setFocusPainted(false);
        this.ring.addActionListener(new ActionListener(){
          public final void actionPerformed(ActionEvent e){device.callAlerted();}
        });
      }
      public final void setEnabled(boolean enabled){
        this.ring.setEnabled(!enabled);
      }
    }
    private final class makeCallPanel extends JPanel{
      final Object types[] = new Object[]{
          Reason.CA.VOICE,
          Reason.CA.FAX,
          Reason.CA.BUSY,
          Reason.CA.NO_ANSWER,
          Reason.CA.NO_DIAL_TONE
      };
      final JCheckBox autoAnswer = new JCheckBox("autoAnswer");
      final JCheckBox random = new JCheckBox("random",false);
      final JComboBox answerType = new JComboBox(types);
      final JButton answer = new JButton("Answer");
      makeCallPanel(){
        super(new BorderLayout(),false);
        JPanel aa = new JPanel(new FlowLayout(FlowLayout.CENTER),false);
        super.add(aa,BorderLayout.NORTH);
        aa.add(this.autoAnswer); aa.add(this.random);
        this.random.setEnabled(false);
        this.autoAnswer.addItemListener(new ItemListener(){
          public final void itemStateChanged(ItemEvent ev)
          {
            if ( autoAnswer.isSelected() ){
              random.setEnabled(true); random.setSelected(false);
              answer.setEnabled(false);answerType.setEnabled(false);
              answerCall();
            } else {
              random.setEnabled(false); random.setSelected(false);
              answer.setEnabled(true);answerType.setEnabled(true);
            }
          }
        });
        JPanel ca = new JPanel(new FlowLayout(FlowLayout.LEFT),false);
        super.add(ca,BorderLayout.CENTER);
        ca.add(this.answerType); ca.add(this.answer);
        this.answer.setFocusPainted(false);
        this.answer.setMargin(new Insets(1,1,1,1));
        this.answer.addActionListener(new ActionListener(){
          public final void actionPerformed(ActionEvent e){answerCall();}
        });
        super.setBorder(new TitledBorder("Make call"));;
      }
      private final java.util.Random randomizator = new java.util.Random();
      private final void answerCall(){
        if ( this.random.isSelected() ) {
          int index = this.randomizator.nextInt(types.length-1);
          this.answerType.setSelectedIndex(index);
        }
        device.answerCall( (String) answerType.getSelectedItem());
      }
      public final void setEnabled(boolean enabled){}
    }
    public final void setEnabled(boolean enabled){
      this.dropCall.setEnabled( enabled );
      String text = enabled ? "ONLINE":"OFFLINE";
      Color background =  enabled ? Color.green:Color.lightGray;
      this.state.setText(text);this.state.setBackground(background);
      this.callPar.setEnabled( enabled );
      this.waitCall.setEnabled( enabled );
      this.makeCall.setEnabled( enabled );
    }
}
  /**
   * The container of user's input
   */
JTextField dtmfBuffer;
  /**
   * Termination's flag
   */
private transient boolean terminated = false;
  /**
   * To get one symbol of user's Input
   * @param delay maxtime to wait input
   * @return one symbol or empty String if timeout
   */
  final String getSymbol(int delay){
    String buffer = this.dtmfBuffer.getText();
    String input = "";this.terminated = false;
    delay *= 10;
    do
    if ( buffer.length() > 0 ) {
      input = buffer.substring(0,1);
      synchronized(this.dtmfBuffer){
        this.dtmfBuffer.setText(buffer.substring(1));
      }
      break;
    }else
      try {
        if ( this.terminated ) break;
        Thread.sleep(100); delay--;
      }
      catch (Exception e) {}
    while( delay > 0 );
    return input;
  }
  /**
   * To terminate get symbol and clear the buffer
   */
  final void terminateGetSymbol(){
    this.terminated = true; this.dtmfBuffer.setText("");
  }
private final class dtmfPanel extends JPanel{
    private dtmfPanel(){
      super(new BorderLayout(),true);
      super.add(dtmfBuffer=new JTextField(),BorderLayout.NORTH);
      dtmfBuffer.setEditable(false);
      JPanel dialpad = new JPanel(new GridLayout(4,3),false);
      super.add(dialpad,BorderLayout.CENTER);
      this.addButton(dialpad,"1");
      this.addButton(dialpad,"2");
      this.addButton(dialpad,"3");
      this.addButton(dialpad,"4");
      this.addButton(dialpad,"5");
      this.addButton(dialpad,"6");
      this.addButton(dialpad,"7");
      this.addButton(dialpad,"8");
      this.addButton(dialpad,"9");
      this.addButton(dialpad,"*");
      this.addButton(dialpad,"0");
      this.addButton(dialpad,"#");
    }
    public final void setEnabled(boolean enabled){
      synchronized(dtmfBuffer){dtmfBuffer.setText("");}
      dtmfBuffer.setEnabled(enabled);
      for(Iterator i=this.buttons.iterator();i.hasNext();){
        ((JButton)i.next()).setEnabled(enabled);
      }
    }
    private final ArrayList buttons = new ArrayList(12);
    private final void addButton(JPanel dialpad,final String input){
      JButton button = new JButton(input);
      button.addActionListener(new ActionListener(){
        public final void actionPerformed(ActionEvent e){
          synchronized (dtmfBuffer) {
            String buf = dtmfBuffer.getText();
            dtmfBuffer.setText(buf + input);
            device.dtmfPressed( input );
          }
        }
      });
      button.setFocusPainted(false);
      dialpad.add(button);this.buttons.add(button);
    }
}
private final class voicePanel extends JPanel{
    public final void setEnabled(boolean enabled){
      this.formats.setEnabled( enabled );
    }
    private final JComboBox formats;
    private voicePanel(){
      super(new BorderLayout(),true);
      super.add(new playPanel(),BorderLayout.CENTER);
      formats = new JComboBox( device.canRecord() );
      formats.setEditable(false);
      formats.setSelectedItem(device.getRecordFormat());
      super.add(new recordPanel(),BorderLayout.SOUTH);
      formats.addActionListener(new ActionListener(){
        public final void actionPerformed(ActionEvent e){
          Audio selected = (Audio)formats.getSelectedItem();
          if (selected != null) device.setRecordFormat(selected);
        }
      });
    }
    private final class playPanel extends JPanel {
      private playPanel(){
        super(new BorderLayout(),false);
        super.setBorder(new TitledBorder("Playback"));
      }
    }
    private final class recordPanel extends JPanel{
      private recordPanel(){
        super(false);
        super.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
        super.setBorder(new TitledBorder("Record"));
        super.add(new JLabel("Format")); super.add( formats );
      }
    }
}
private final void startFax(){}
private final void stopFax(){}
private final class faxPanel extends JPanel{
    private faxPanel()
    {
      super(new BorderLayout(), true);
      super.add(new machinePanel(),BorderLayout.NORTH);
      super.add(new docPanel(),BorderLayout.CENTER);
      super.add(new controlPanel(),BorderLayout.SOUTH);
    }
    public final void setEnabled(boolean enabled){
      super.setEnabled(enabled);
    }
    private final class machinePanel extends JPanel {
      private machinePanel(){
        super(false);
        super.setBorder(new TitledBorder("Basis"));
        super.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        JPanel
        panel = new JPanel(false);
        panel.setLayout(new BoxLayout(panel,BoxLayout.X_AXIS));
        super.add(panel); panel.add(new JLabel("Transferred pages "));
        JTextField pagesCount =
            new JTextField(String.valueOf(device.fax.transferredPages),5);
        pagesCount.setHorizontalAlignment(JTextField.RIGHT);
        pagesCount.setDocument(new UI.validatingDocument(){
           protected final void validate(String str) throws Exception {
             int newValue = 0;
             if ( str.length() > 0 ) newValue = Integer.parseInt(str);
             device.fax.transferredPages = newValue;
           }
        });
        panel.add(pagesCount);
        panel = new JPanel(false);
        panel.setLayout(new BoxLayout(panel,BoxLayout.X_AXIS));
        super.add(panel);
        panel.add(new JLabel("RemoteID: "));
        JTextField remoteID = new JTextField(device.fax.remoteID,10);
        remoteID.setDocument(new UI.validatingDocument(){
           protected final void validate(String str) throws Exception {
             device.fax.remoteID = str;
           }
        });
        panel.add(remoteID);
        panel = new JPanel(false);
        panel.setLayout(new BoxLayout(panel,BoxLayout.X_AXIS));
        super.add(panel);
        panel.add(new JLabel("LocalID: "));
        JTextField localID = new JTextField(device.fax.localID,10);
        localID.setDocument(new UI.validatingDocument(){
           protected final void validate(String str) throws Exception {
             device.fax.localID = str;
           }
        });
        panel.add(localID);
        panel = new JPanel(new BorderLayout(),false); super.add(panel);
        panel.setBorder(new TitledBorder("Fax Header"));
        JTextField header = new JTextField(device.fax.faxHeader,10);
        header.setDocument(new UI.validatingDocument(){
           protected final void validate(String str) throws Exception {
             device.fax.faxHeader = str;
           }
        });
        panel.add(header,BorderLayout.CENTER);
      }
    }
    private final class docPanel extends JPanel{
      private docPanel(){super(new BorderLayout(),false);
        super.setBorder(new TitledBorder("Document"));
      }
    }
    private final class controlPanel extends JPanel{
      private controlPanel(){super(new FlowLayout(),false);
        super.setBorder(new TitledBorder("Transmission"));
        JButton start = new JButton("Start"); super.add(start);
        start.addActionListener(new ActionListener(){
          public final void actionPerformed(ActionEvent e){startFax();}
        });
        JButton stop = new JButton("Stop"); super.add(stop);
        stop.addActionListener(new ActionListener(){
          public final void actionPerformed(ActionEvent e){stopFax();}
        });
      }
    }
}
}
