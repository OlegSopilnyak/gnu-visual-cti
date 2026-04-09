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
package org.visualcti.briquette.message;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;
import org.jdom.Element;
import org.visualcti.briquette.Operation;
import org.visualcti.briquette.Subroutine;
import org.visualcti.briquette.Symbol;
import org.visualcti.briquette.core.Logic;
import org.visualcti.server.message.Message;
import org.visualcti.server.message.MessageException;
import org.visualcti.server.message.MessageFactory;
import org.visualcti.server.message.Messenger;
import org.visualcti.util.Property;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: Briquette: To receive the message</p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class Receive extends Basis
{
    /**
     * <constructor>
     * To build the briquette
     */
    public Receive()
    {
      super.setAbout("To receive the message");
      this.initRuntime();
    }
    /**
     * to initialize the parameters of the briquette
     */
    private final void initRuntime() {
      this.protocol = "Internal";
      this.server = Symbol.newConst("localhost");
      this.login = Symbol.newConst("user");
      this.password=Symbol.newConst("pass");
      try{this.selector.setXML( null );}catch(Exception e){}
    }
/**
 * <attribute>
 * The name of transport's protocol
 */
private String protocol;
  /**
   * <accessor>
   * To get access to the name of protocol
   * @return name of protocol
   */
  public final String getProtocol() {return protocol;}
  /**
   * <mutator>
   * To change the protocol's name
   * @param protocol the name of protocol
   */
  public final void setProtocol(String protocol) {this.protocol = protocol;}
/**
 * <attribute>
 * the server of messages
 */
private Symbol server;
  /**
   * <mutator>
   * To store new server's value
   * @param server new value
   */
  public final void setServer(Symbol server) {this.server = server;}
  /**
   * <accessor>
   * To get access to server's value
   * @return the server
   */
  public final Symbol getServer() {return this.server;}
/**
 * <attribute>
 * The login to server
 */
private Symbol login;
  /**
   * <mutator>
   * To store new login's value
   * @param login new value
   */
  public final void setLogin(Symbol login) {this.login = login;}
  /**
   * <accessor>
   * To get access to login's value
   * @return the login
   */
  public final Symbol getLogin() {return this.login;}
/**
 * <attribute>
 * The the password for login to server
 */
private Symbol password;
  /**
   * <mutator>
   * To store new password's value
   * @param password new value
   */
  public final void setPassword(Symbol password) {this.password = password;}
  /**
   * <accessor>
   * To get access to password's value
   * @return password
   */
  public final Symbol getPassword() {return this.password;}
/**
 * <attribute>
 * The logical sequence for selecting
 * */
private final Logic selector = new Logic();
  /**
   * <accessor>
   * To get access to selector
   * @return the selector
   */
  public final Logic getSelector() {return this.selector;}
    /**
     * <accessor>
     * To fill runtime information
     * To store representation of Opertaion to XML
     * @param xml container to store
     */
    protected final void storeRuntimeProperties(Element xml)
    {
      xml.addContent(new Property("protocol",this.protocol).getXML() );
      xml.addContent(new Property("server",this.server).getXML() );
      xml.addContent(new Property("login",this.login).getXML() );
      xml.addContent(new Property("password",this.password).getXML() );
      xml.addContent( this.selector.getXML() );
    }
    /**
     * <mutator>
     * To adjust runtime properties in Operation
     * To adjust Operation's runtime
     * @param xml stored information
     * @throws Exception if some wrong
     */
    protected final void restoreRuntimeProperties(Element xml) throws Exception
    {
      // to clear a runtime properties
      this.initRuntime();
      // to check of XML's integrity
      if (xml == null) return;
      // to make the properties's iterator
      Iterator i=xml.getChildren(Property.ELEMENT).iterator();
      ArrayList names = new ArrayList( 4 );
      // to iterate the properties of operation
      while( i.hasNext() )
      {
          Property property = new Property( (Element)i.next() );
          String name = property.getName();
          // check the property's name
          if (name == null) throw new Exception("Property without name!");
          if ( names.contains(name) )
            throw new Exception("Multiple definition of runtime properties!");
          // to solve the property by name
          if ( "protocol".equals(name) )    {
              this.protocol = property.getValue(this.protocol); names.add(name);
          } else
          if ( "server".equals(name) )    {
              this.server = property.getValue(this.server); names.add(name);
          } else
          if ( "login".equals(name) )    {
              this.login = property.getValue(this.login); names.add(name);
          } else
          if ( "password".equals(name) )    {
              this.password = property.getValue(this.password); names.add(name);
          }
      }
      // to restore the selector
      this.selector.setXML( xml.getChild(Logic.ELEMENT) );
    }
    /**
     * To prepare caller's pool before call
     * @param caller the owner of the pool
     */
    private final void preparePool(Subroutine caller){
      caller.set(Basis.system_msg_Available,"No");
      caller.set(Basis.system_msg_Attachment,null);
      caller.set(Basis.system_msg_AttachmentType,null);
      caller.set(Basis.system_msg_Destination,null);
      caller.set(Basis.system_msg_ReplyTo,null);
      caller.set(Basis.system_msg_Text,null);
      caller.set(Basis.system_msg_Text_CodePage,null);
      caller.set(Basis.system_msg_TimeStamp,null);
      caller.set(Basis.system_msg_Type,null);
    }
    /**
     * To store received message to system values
     * @param caller briquette's executor
     * @param message received message
     */
    private final void storeMessage(Subroutine caller,Message message){
      caller.set(Basis.system_msg_Available,"Yes");
      // get message's timestamp
      try{long time = message.getJMSTimestamp();
        java.util.Date date = new java.util.Date( time );
        caller.set( Basis.system_msg_TimeStamp, date );
      }catch(javax.jms.JMSException e){}
      javax.jms.Destination address;
      // get destination
      try{
        if ( (address = message.getJMSDestination()) != null)
          caller.set(Basis.system_msg_Destination,address.toString());
      }catch(javax.jms.JMSException e){}
      // get replyto
      try{
        if ( (address = message.getJMSReplyTo()) != null)
          caller.set(Basis.system_msg_ReplyTo,address.toString());
      }catch(javax.jms.JMSException e){}
      // get message's type
      try{caller.set(Basis.system_msg_Type,message.getJMSType());
      }catch(javax.jms.JMSException e){}
      // get message's text
      caller.set(Basis.system_msg_Text,message.getBodyText());
      // get message's attachment
      caller.set(Basis.system_msg_Attachment,message.getBodyAttachment());
    }
  /**
   * <action>
   * To do briquette's action
   * @param caller the owner of this briquette
   * @return reference to next briquette
   */
  public final Operation doIt(Subroutine caller)
  {
    try {
      this.preparePool( caller );
      org.visualcti.server.task.Environment env = caller.getProgramm().getEnv();
      // to get access to messenger's Service
      Messenger messenger = (Messenger)env.getPart("messenger",Messenger.class);
      // to prepare the call
      String factory = this.getFactoryName();
      String server = (String)caller.get( this.server );
      String login = (String)caller.get( this.login );
      String password = (String)caller.get( this.password );
      String selector = this.selector.getSQL(caller);
      // to prepare factory's properties
      Properties props = new Properties();
      props.put( Message.PROTOCOL, this.getProtocolName() );
      props.put( MessageFactory.SERVER, server );
      props.put( MessageFactory.LOGIN, login );
      props.put( MessageFactory.PASSWORD, password );
      // request to messenger
      Message message = messenger.recieve( factory, selector, props );
      // to store received message
      if ( message != null ) this.storeMessage( caller, message );
    }catch(NullPointerException e){
      return null;
    }catch(MessageException e){
      return null;
    }
    // go to next briquette
    return this.getLink(Operation.DEFAULT_LINK);
  }
  private final String getFactoryName(){
    return new StringTokenizer(this.protocol,"/").nextToken().toUpperCase();
  }
  private final String getProtocolName(){
    StringTokenizer st = new StringTokenizer(this.protocol,"/");
    st.nextToken();
    try{return st.nextToken();
    }catch(NoSuchElementException e){}
    return this.protocol;
  }
  /**
   * <action>
   * To cancel the doIt() execution
   */
  public final void stopExecute(){}
}
