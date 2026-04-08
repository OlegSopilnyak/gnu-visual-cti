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
package org.visualcti.server.message.impl;

import java.util.*;
import java.io.*;

import org.jdom.*;
import org.visualcti.util.Tools;
import org.visualcti.server.*;
import org.visualcti.server.service.*;

import org.visualcti.server.message.Message;
import org.visualcti.server.message.MessageFactory;
import org.visualcti.server.message.MessageException;
import org.visualcti.server.message.Messenger;

/**
The class operates access to the messages
The access is possible from MessageFactory or
From the outside through handler
*/
public final
class MessengerImpl
        extends MessengerCluster
        implements Messenger
{
   /**
<accessor>
To get body unit's Icon (gif | jpeg)
   */
   public final byte[] getIcon(){return super.getIcon();}
   /**
<accessor>
To get Current state of unit
   */
   public final String getUnitState(){return super.getUnitState();}
   /**
<accessor>
To get Type of unit
   */
   public final String getType(){return super.getType()+" manager of messages factories";}
/**
Threads group for messenger
*/
private final ThreadGroup msgGroup = new ThreadGroup("Messenger");
   /**
   Constructor
   */
   public MessengerImpl() {super();}
   /**
allways return "Messenger"
   */
   public final String getName() {return "Messenger";}
   /**
<accessor>
current service's ThreadGroup
   */
   public final ThreadGroup getThreadGroup(){return this.msgGroup;}

   /**
Start messenger
   */
   public final void Start() throws IOException
   {
        if (this.services.size() == 0) {
            IOException e = new IOException("Sorry! No factories in messenger...");
            dispatchEvent( new unitError(this,e) ); throw e;
        }
        super.Start();// start the factories, as slave services
        this.dispatch( new unitEvent(this,unitEvent.START_ID) );
   }
   /**
Stop messenger
   */
   public final void Stop() throws IOException
   {
        super.Stop();// to stop all message factories
        this.dispatchEvent( new unitEvent(this,unitEvent.STOP_ID) );
   }
   /**
Append MessageFactory, as slave service,
Called when maked MessageFactory
   */
   public final void addSlaveService(Service service) {
        if (service instanceof MessageFactory) super.addSlaveService( service );
   }
   /**
remove MessageFactory from available list
   */
   public final void removeSlaveService(Service service) {
        if (service instanceof MessageFactory) super.removeSlaveService( service );
   }
    /**
    process configuration from XML element
    */
    protected final void processConfiguration(Element xml)
    {
        super.processConfiguration( xml );// process Cluster config
        Attribute dir = xml.getAttribute("persistent");
        this.needSaveXML = this.needSaveXML || dir==null;
        if (dir == null) xml.setAttribute(dir = new Attribute("persistent","./persistent.messages"));
        Attribute factoryRootDir = (Attribute)dir.clone();
        // to process "factory" entries
        for(Iterator i = xml.getChildren("factory").iterator();i.hasNext();)
        {
            Element e = (Element)i.next();
            Attribute Dir = e.getAttribute("persistent");
            Tools.out.print(".");Tools.out.flush();
            this.needSaveXML = this.needSaveXML || Dir==null;
            if (Dir == null) e.setAttribute(new Attribute("persistent","./persistent.messages"));
            Service factory = ServiceMaker.make( e );// to make factory, as service
            if (factory instanceof MessageFactory) {
                Tools.out.print(".");Tools.out.flush();
                this.addSlaveService(factory);
            }
        }
        if (this.needSaveXML) Config.save();
    }
   /**
<produce>
to create message of factory by factory name
   */
   public final Message create(String factoryName) throws MessageException
   {
        MessageFactory factory = this.getFactory(factoryName);
        if (factory == null) throw new MessageException("MessageFactory "+factoryName+" not installed...");
        return factory.createMessage();
   }
   /**
<produce>
to create message as reply to message for factory
   */
   public final Message createReplyToMessage(String factoryName, Message source) throws MessageException
   {
        MessageFactory factory = this.getFactory(factoryName);
        if (factory == null) throw new MessageException("MessageFactory "+factoryName+" not installed...");
        return factory.createReplyToMessage(source);
   }
   /**
<transfer>
to send the message via factory by factory name
   */
   public final void send(String factoryName, Message message) throws MessageException
   {
        MessageFactory factory = this.getFactory(factoryName);
        if (factory == null) throw new MessageException("MessageFactory "+factoryName+" not installed...");
        factory.send( message );
   }
   /**
<tarnsfer>
to receive message from local or remote messages list
it call this.reciveLocal, if not success, call this.reciveRemote
   */
   public final Message recieve
                    (
                    String factoryName,
                    String selector,
                    Properties properties
                    )
                    throws MessageException
   {
        MessageFactory factory = this.getFactory(factoryName);
        if (factory == null) throw new MessageException("MessageFactory "+factoryName+" not installed...");
        Message message = factory.receive(selector, properties);
        return message == null ? this.receiveRemote(factoryName, selector, properties):message;
   }
   /**
<accessor>
To get access to factory refarence by name
   */
   public final MessageFactory getFactory(String name){return (MessageFactory)this.getService(name);}

}
