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
package org.visualcti.briquette.message;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import org.jdom.Element;
import org.visualcti.briquette.Operation;
import org.visualcti.briquette.Subroutine;
import org.visualcti.briquette.Symbol;
import org.visualcti.server.message.Message;
import org.visualcti.server.message.MessageException;
import org.visualcti.server.message.MessageFactory;
import org.visualcti.server.message.Messenger;
import org.visualcti.util.Property;

/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: To compose and sent the message</p>
 * <p>Copyright: Copyright (c) Prominic Inc. & Prominic Ukraine Co. 2002</p>
 * <p>Company: Prominic Ukraine Co.</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */

public final class Sent extends Basis
{
  /**
   * <constructor>
   * To build the briquette
   */
  public Sent()
  {
    super.setAbout("To transmit the message");
    this.properties=new ArrayList();
    this.initRuntime();
  }
    /**
     * to initialize the parameters of the briquette
     */
    private final void initRuntime() {
      this.protocol = "Internal";
      this.server = Symbol.newConst("localhost");
      this.destination = Symbol.newConst("ANY");
      this.replyto = Symbol.newConst("NoReply");
      this.type = Symbol.newConst("warning");
      this.attachment = Symbol.newConst("");
      this.text = Symbol.newConst("Enter the text here...");
      this.needLogin=false;
      this.login = Symbol.newConst("user");
      this.password=Symbol.newConst("pass");
      this.properties.clear();
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
 * the destination of message
 */
private Symbol destination;
  /**
   * <accessor>
   * To get access to message's destination
   * @return the destination
   */
  public final Symbol getDestination() {return destination;}
  /**
   * <mutator>
   * To setup message's destination
   * @param destination new destination
   */
  public final void setDestination(Symbol destination) {this.destination = destination;}
/**
 * <attribute>
 * the source of messages */
private Symbol replyto;
  /**
   * <accessor>
   * To get access to message's replyto address
   * @return replyto address
   */
  public final Symbol getReplyto() {return replyto;}
  /**
   * <mutator>
   * To setup message's replyto address
   * @param replyto the address
   */
  public final void setReplyto(Symbol replyto) {this.replyto = replyto;}
/**
 * <attribute>
 * the type of the message
 */
private Symbol type;
  /**
   * <accessor>
   * To get access to message's type
   * @return the type
   */
  public final Symbol getType() {return type;}
  /**
   * <mutator>
   * To setup message's type
   * @param type new type
   */
  public final void setType(Symbol type) {this.type = type;}
/**
 * <attribute>
 * the binary part of the message
 */
private Symbol attachment;
  /**
   * <accessor>
   * To get access to message's attachment
   * @return attachment
   */
  public final Symbol getAttachment() {return this.attachment;}
  /**
   * <mutator>
   * To setup message's attachment
   * @param attachment new attachment
   */
  public final void setAttachment(Symbol attachment) {this.attachment = attachment;}
/**
 * <attribute>
 * the text part of the message
 */
private Symbol text;
  /**
   * <accessor>
   * To get access to message's text
   * @return text part of the message
   */
  public final Symbol getText() {return text;}
  /**
   * <mutator>
   * To setup message's text
   * @param text text part of the message
   */
  public final void setText(Symbol text) {this.text = text;}
/**
 * <attribute>
 * Flag is login to server is necessary
 */
private boolean needLogin;
  /**
   * <mutator>
   * To setup new login to server flag
   * @param needLogin the flag
   */
  public final void setNeedLogin(boolean needLogin) {this.needLogin = needLogin;}
  /**
   * <accessor>
   * To get access to flag
   * @return flag's value
   */
  public final boolean isNeedLogin() {return needLogin;}
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
 * the optional properties of the message
 */
private final ArrayList properties;
  /**
   * <accessor>
   * To get access to message's properties
   * @return the properties
   */
  public final java.util.ArrayList getProperties() {return properties;}
    /**
     * <accessor>
     * To fill runtime information
     * To store representation of Opertaion to XML
     * @param xml the container to store
     */
    protected final void storeRuntimeProperties(Element xml)
    {
      xml.addContent(new Property("protocol",this.protocol).getXML());
      xml.addContent(new Property("server",this.server).getXML());
      xml.addContent(new Property("destination",this.destination).getXML());
      xml.addContent(new Property("replyto",this.replyto).getXML());
      xml.addContent(new Property("type",this.type).getXML());
      if ( this.attachment != null && !this.attachment.isConst() ) {
        xml.addContent(new Property("attachment",this.attachment).getXML());
      }
      xml.addContent(new Property("text",this.text).getXML());
      xml.addContent(new Property("needLogin",this.needLogin).getXML());
      xml.addContent(new Property("login",this.login).getXML());
      xml.addContent(new Property("password",this.password).getXML());
      Element props=new Element("optional");
      for(Iterator i=this.properties.iterator();i.hasNext();)
      {
        Property message_property = (Property)i.next();
        if (message_property != null) props.addContent( message_property.getXML() );
      }
      xml.addContent( props );
    }
    /**
     * <mutator>
     * To adjust runtime properties in Operation
     * To adjust Operation's runtime
     * @param xml the container to restore
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
      ArrayList names = new ArrayList( 10 );
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
          if ( "destination".equals(name) )    {
              this.destination = property.getValue(this.destination); names.add(name);
          } else
          if ( "replyto".equals(name) )    {
              this.replyto = property.getValue(this.replyto); names.add(name);
          } else
          if ( "type".equals(name) )    {
              this.type = property.getValue(this.type); names.add(name);
          } else
          if ( "attachment".equals(name) )    {
              this.attachment = property.getValue(this.attachment); names.add(name);
          } else
          if ( "text".equals(name) )    {
              this.text = property.getValue(this.text); names.add(name);
          } else
          if ( "needLogin".equals(name) )    {
              this.needLogin = property.getValue(this.needLogin); names.add(name);
          } else
          if ( "login".equals(name) )    {
              this.login = property.getValue(this.login); names.add(name);
          } else
          if ( "password".equals(name) )    {
              this.password = property.getValue(this.password); names.add(name);
          }
      }
      // to restore the optional properties
      Element optXML = xml.getChild("optional");
      if ( optXML == null ) return;
      for(i=optXML.getChildren(Property.ELEMENT).iterator();i.hasNext();)
      {
          Property message_property = new Property((Element)i.next());
          if (message_property.getValue() != null) this.properties.add( message_property );
      }
    }

  private final Message makeMessage(Messenger messenger,Subroutine caller)
    throws MessageException
  {
    Message message = messenger.create( this.getFactoryName() );
    try{
      message.setStringProperty(Message.PROTOCOL, this.getProtocolName() );
    }catch(javax.jms.JMSException e){}
    String
    temp = (String)caller.get( this.destination );
    try {
      if (temp != null) message.setJMSDestination(new Message.Destination(temp));
    }catch(javax.jms.JMSException e){}
    temp = (String)caller.get( this.replyto );
    try {
      if (temp != null) message.setJMSReplyTo(new Message.Destination(temp));
    }catch(javax.jms.JMSException e){}
    temp = (String)caller.get( this.type );
    try {
      if (temp != null) message.setJMSType( temp );
    }catch(javax.jms.JMSException e){}
    temp = (String)caller.get( this.text );
    if (temp != null) message.setBodyText( temp );
    Object attachment = caller.get(this.attachment);
    if ( attachment != null && !this.attachment.isConst() ) message.setBodyAttachment(attachment);
    for(Iterator i=this.properties.iterator();i.hasNext();){
      Property message_property = (Property)i.next();
      if ( message_property == null )continue;
      if ( message_property.getValue() instanceof Symbol ) {
        Symbol symbol = message_property.getValue((Symbol)null);
        Object value = caller.get( symbol );
        if (value == null) continue;
        String name = "message."+message_property.getName();
        try{message.setObjectProperty( name, value );
        }catch(javax.jms.JMSException e){}
      }
    }
    return message;
  }
  /**
   * Main entry of briquette's execution
   * @param caller the caller of briquette
   * @return the reference to the next briquette
   */
  public final Operation doIt(Subroutine caller)
  {
    try{
      org.visualcti.server.task.Environment env = caller.getProgramm().getEnv();
      // to get access to messenger's Service
      Messenger messenger = (Messenger)env.getPart("messenger",Messenger.class);
      // to make the message
      Message message = this.makeMessage(messenger,caller);
      // to adjust the server
      try{
        String server = (String)caller.get(this.server);
        message.setStringProperty(MessageFactory.SERVER, server );
      }catch(javax.jms.JMSException e){}
      // adjust server's access
      if ( this.needLogin ) {
        try{
          String login = (String)caller.get(this.login);
          String password = (String)caller.get(this.password);
          message.setStringProperty(MessageFactory.LOGIN, login );
          message.setStringProperty(MessageFactory.PASSWORD, password );
        }catch(javax.jms.JMSException e){}
      }
      // to transmit the message
      messenger.send( this.getFactoryName(), message );
    }catch(NullPointerException e){
      return null;
    }catch(MessageException e){
      return null;
    }
    // go to next briquette
    return this.getLink(Operation.DEFAULT_LINK);
  }
  /**
   * <action>
   * To stop execution
   * Do nothing
   */
  public final void stopExecute(){}
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
}
