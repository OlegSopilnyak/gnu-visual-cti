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
package org.visualcti.server.connector.streams;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import org.visualcti.server.Parameter;
import org.visualcti.server.connector.GateKeeper;
import org.visualcti.server.connector.Link;
import org.visualcti.server.connector.LinkFactory;
import org.visualcti.server.connector.Transport;
import org.visualcti.server.security.Filter;
import org.visualcti.server.unitAction;
import org.visualcti.server.unitCommand;
import org.visualcti.server.unitError;
import org.visualcti.server.unitEvent;
import org.visualcti.server.unitResponse;
import org.visualcti.util.Queue;

/**
stream based Transport
processor for made the dialog with client,
using input/output stream
*/
abstract class Processor 
                implements  GateKeeper,
                            Transport,
                            Runnable
{
/**
Printer for send strings to client
*/
private PrintWriter output = null;
/**
Reader for input commands from client
*/
private BufferedReader input=null;
/**
The prompt for client's command
*/
protected String prompt = "";
/**
The login name
*/
private String login;
/**
The reference to owner of transport
*/
private Link owner=null;
/**
Links factory of this gate keeper
*/
private volatile LinkFactory factory=null;
/**
Filter for server flows
*/
private volatile Filter filter = null;
    /**
    <accessor>
    get access to user's filter
    */
    public final Filter getUserFilter(){return this.filter;}
	/**
	   <accessor>
	   to get owner of transport
	 */
	public Link getLink(){return this.owner;}
	
	/**
	   <mutator>
	   to assign owner of transport
	 */
	public void setLink(Link link){this.owner=link;}
	/**
	   <action>
	   to close Link's transport
	   @roseuid 3BC2AFDD026D
	 */
	public void close()
	{
	    this.process_exit_Command("immediate");
	}
/**
<queue>
Queue of active commands
*/
private final Queue commands = new Queue();
	/**
	   <transport>
	   to receive command from connected client
	   called outside of processor
	 */
	public unitCommand receive() throws IOException
	{
	    unitCommand command = (unitCommand)this.commands.pop();
	    if ( !this.ready() || command == null) throw new EOFException();
	    command.setLink(this.getLink().getName());
	    return command;
	}
	/**
	   <transport>
	   to send event to connected client
	   called outside of processor
	 */
	public void send(unitAction event) throws IOException
	{   
	    if ( !this.ready() ) throw new EOFException();
//this.output.println("\nsent:\n"+event.toString());
	    // transfer event to connect
	    switch( event.actionClass() )
	    {
	        case unitEvent.EVENT:
	        // via transport, sent event
	            this.sendEvent((unitEvent)event);
	            break;
	            
	        case unitEvent.ERROR:
	        // via transport, sent error
	            this.sendError((unitError)event);
	            break;
	            
	        case unitEvent.RESPONSE:
	        // via transport, sent response
	            unitResponse resp = (unitResponse)event;
	            int ID = resp.getCorrelationID();
	            if (
	                this.waitingCommand != null &&
	                this.waitingCommand.sequenceID() == ID
	                )
	            {
	                // transfer response to a waiting command
	                this.waitingCommand.setResponse(resp);
	            }
	            break;
	        default:
	        // via transport, sent unknown action 8(
	            this.output.println("\nsent unknown:\n"+event.toString());
	            return;
	    }
	}
	/** via transport, sended event */
	private final void sendEvent(unitEvent event)
	{
	    this.output.print("Unit("+event.getUnitPath()+") ");
	    switch(event.getID())
	    {
	        case unitEvent.START_ID:
	            this.output.print("Started..."); break;
	        case unitEvent.STOP_ID:
	            this.output.print("Stopped..."); break;
	        case unitEvent.STATE_ID:
	            this.output.print("says: "+event.getDescription());
	            break;
	    }
	    this.output.println();
	}
	/** via transport, sended error */
	private final void sendError(unitError error)
	{
	    this.output.print("!Error in ("+error.getUnitPath()+") malfunction: ");
	    this.output.print(error.getDescription());
	    if ( error.nested() != null){
	        this.output.println("\nStack trace:");
	        error.nested().printStackTrace(this.output);
	    }else this.output.println();
	}

    /**
    class for represent the command
    */
    public static class command
    {
        /** command's Name */
        public String Name;
        /** command's Option */
        public String Option;
        /** Constructor */
        public command(String in)
        {
            if (in == null) {
                this.Name = ""; this.Option = "";
            }else {
                in = in.trim();
                int space = in.indexOf(" ");
                if (space == -1) {
                    this.Name = in; this.Option = "";
                }else {
                    this.Name = in.substring(0, space);
                    this.Option = in.substring(space+1).trim();
                }
            }
            this.Name = this.Name.toLowerCase();
        }
    }
        /**
        to say prompt to client via this.ouput
        */
        private void prompt() throws IOException{
            if ( !this.stillWorked || output == null ) return;
            this.output.print(this.prompt);
            this.output.print(">");
            this.output.flush();
        }
    /**
    to request login 3 times
    */
    public Transport login() throws IOException
    {
        String password;
        for(int i=1; i <= 3;i++)
        {
            this.output.print("Login:");this.output.flush();
            this.login = this.input.readLine();
            if (this.login == null) throw new EOFException();
            this.output.print("Password:");this.output.flush();
            password = this.input.readLine();
            if (password == null) throw new EOFException();
            this.filter = this.factory.getUserFilter(login, password);
            if (this.filter != null) {
                this.startTranport(); return this;
            }
            this.output.println("\nYou wrong, try again...\n");
        }
        this.cleanAll();// free all external references
        return null;
    }
    /**
    To start Transport's thread
    */
    private void startTranport()
    {
        Thread wrapper = 
                new Thread
                    (
                    this.factory.getThreadGroup(),
                    this,
                    factory.getName()+"_Session"
                    );
        //wrapper.setPriority( Thread.MIN_PRIORITY );
        wrapper.start();
        while( !this.stillWorked ) Thread.yield();
    }
/**
to abort login process
*/
public void abortLogin(){}
    /**
    <cleaner>
    to clean all reference to external entities
    for disable the memory leaks
    */
    private final void cleanAll()
    {
        if (this.owner != null) this.owner.close();
        this.input = null;
        this.output = null;
        this.factory = null;
        this.owner = null;
        this.filter = null;
        this.cmdMap.clear();
    }
    /**
    Constructor of processor
    */
    public Processor
                (
                InputStream in,     // stream for accept commands
                OutputStream out,   // stream for sent results/ events
                LinkFactory factory // factory - owner of Processor
                )
    {
        this.input = new BufferedReader(new InputStreamReader(in));
        this.output = new PrintWriter(out, true);
        this.factory = factory;
        this.processCommandsMaping();
    }
        /** to make predefined commands map */
        private final void processCommandsMaping()
        {
            Method
            process = this.getProcess("exit");
            // to make exit synonyms (quit, q, x)
            if (process != null) {
                this.cmdMap.put("quit", process);
                this.cmdMap.put("q", process);
                this.cmdMap.put("x", process);
            }
            process = this.getProcess("help");
            // to make help synonyms (man, howto, ?, h)
            if (process != null) {
                this.cmdMap.put("man", process);
                this.cmdMap.put("howto", process);
                this.cmdMap.put("?", process);
                this.cmdMap.put("h", process);
            }
            process = this.getProcess("threads");
            // to make help synonyms (man, howto, ?, h)
            if (process != null) {
                this.cmdMap.put("!", process);
                this.cmdMap.put("t", process);
            }
        }
	/**
	<accessor>
	Check is transport ready to send receive
	*/
	public final boolean ready(){return this.stillWorked;}
    /**
    <flag>
    flag is Processor executed
    */
    private volatile boolean stillWorked = false;
    /**
    Main loop of processor
    */
    public final void run() {   
        try {
            this.output.println("Processor have started...");
            String line = null;// line - user's input (request to command)
            this.stillWorked = true;// still executed flag
            this.transportOpened();// to notify about transport starting
            while( this.stillWorked )
            {
                this.prompt();// sent prompt string to client
                if ((line = this.input.readLine()) == null) break;
                this.processCommand( new command(line) );// process user's command
            }
        }catch(Exception e){
            e.printStackTrace();
        }catch(ThreadDeath td){
            throw td;
        }finally {
            this.output.println("Processor have finished...");
            // unlink this processor from system
            this.stillWorked = false;
            // to free command semaphore
            this.run( null );
            // to notify transport owner
            this.transportClosed();
            // to clear all references
            this.cleanAll();
        }
    }
/**
this command, waiting of the response
*/
private volatile unitCommand waitingCommand = null;
    /**
    to run the command
    */
    private final void run(unitCommand command)
    {
        if (command != null) {
            if ( command.isNeedResponse() )
            {
                this.commands.push( this.waitingCommand = command );
                if ( !command.isDone() ) {
                    // to wait response, activated semaphore "waitingCommand"
                    try{synchronized(command){command.wait();}}catch(Exception e){}
                }
            } else  this.commands.push( command );
        } else  this.commands.push( command );
    }
    /**
    <notify>
    To notify on opening transport
    */
    protected void transportOpened(){}
    /**
    <notify>
    To notify on closing transport
    */
    protected void transportClosed(){}
/**
map for command -> method
*/
private final HashMap cmdMap = new HashMap();
/**
Array of all methods startsWith process_
*/
private static Method[] processes;
static {// to initialize processes array with "process_" prefix
    Method[]all = Processor.class.getDeclaredMethods();
    ArrayList right = new ArrayList();
    for(int i=0;i < all.length;i++) {Method proc = all[i];
        if (proc.getName().startsWith("process_")) right.add(proc);
    }
    Processor.processes = (Method[])right.toArray(new Method[0]);
}
        /**
        to process user command
        */
        private void processCommand(command cmd) throws Exception
        {
            if ("".equals(cmd.Name)) return;
            Method process = (Method)this.cmdMap.get(cmd.Name);
            if ( process != null ) {
                process.invoke( this, new Object[] {cmd.Option} );
                return;
            }else {
                process = this.getProcess( cmd.Name );
                if (process == null) {this.unknownCommand(cmd);return;}
                process.invoke( this, new Object[] {cmd.Option} );
            }
            this.cmdMap.put( cmd.Name, process );
        }
private static final Object SEMAPHORE = new Object();
        /**
        to get reference to Method for process the command
        */
        private Method getProcess(String command){
            // find in methods list method for command
            String methodName = "process_"+command+"_Command";
            for(int i=0;i < Processor.processes.length;i++) {
                Method proc = Processor.processes[i];
                if (proc.getName().equals(methodName)) return proc;// found
            }
            return null;// not found
        }
    /**
    list command handle
    */
    protected void process_list_Command(String options)
    {
        unitCommand command = new unitCommand(this.owner.getName(), unitCommand.GET_ID, "");
        command.set(new Parameter("target", "units") );
        command.setNeedResponse(true);
        this.output.println("Allowed units:");
        this.run(command);// launch and wait response
        if ( !command.isSuccessful() )
        {
            this.output.print("error: ");
            Parameter par = command.getParameter( "@error" );
            if ( par != null) this.output.println(par.getValue());
            else this.output.println(" Unknown error :(");
            return;
        }
        Parameter par = command.getParameter("units");
        StringTokenizer st = new StringTokenizer(par.getValue().toString(),";");
        ArrayList units = new ArrayList();
        while(st.hasMoreTokens()) units.add( st.nextToken() );
        Collections.sort( units );
        for(Iterator i = units.iterator();i.hasNext();)
        {
            String path = (String)i.next();
            this.output.println("Unit "+path+" "+this.unitInfo(path));
        }
    }
    /** get unit's information (type,state) */
    private String unitInfo(String unitPath)
    {
        unitCommand command = new unitCommand(unitPath, unitCommand.GET_ID, "");
        command.setNeedResponse(true);
        command.set(new Parameter("target", "meta").input() );
        /*
        command.set(new Parameter("meta.*", "meta").output() );
        command.set(new Parameter("meta.type", "meta").output() );
        command.set(new Parameter("meta.state", "meta").output() );
        */
        this.run(command);// launch and wait response
        if ( !command.isSuccessful() ) {
            String res ="error: ";
            Parameter par = command.getParameter( "@error" );
            if ( par != null) res += par.getValue();
            else res += " Unknown error :(";
            return res;
        } else {
            Parameter 
            par = command.getParameter( "meta.type" );
            String res = "("+par.getValue()+") ";
            par = command.getParameter( "meta.state" );
            return res + par.getValue();
        }
    }
    /**
    start command handle
    */
    protected void process_start_Command(String options)
    {
        unitCommand command = new unitCommand(options, unitCommand.START_ID, "");
        command.setNeedResponse(false); this.run(command);
    }
    /**
    stop command handle
    */
    protected void process_stop_Command(String options)
    {
        unitCommand command = new unitCommand(options, unitCommand.STOP_ID, "");
        command.setNeedResponse(false); this.run(command);
    }
    /**
    exit command handle
    */
    protected void process_exit_Command(String options)
    {
        this.stillWorked = false;
    }
    /**
    help exit command handle
    */
    protected void process_help_exit_Command(String options) throws Exception
    {
        this.output.println("This command will exit the processor.");
        this.output.println("  Current user will logout.");
        this.output.println("  For exit you can use also: quit | q | x");
    }
/** help message */
private final static String processorHelp =
"Processor support commands:\n"+
"\thelp - this text\n"+
"\thelp <command> - howto use the command\n"+
"\tthreads [all|main|<threads group>] - to show all active threads, with groups\n"+
"\tlist - get the list of allowed units\n"+
"\tstart <unit path> - to start the unit\n"+
"\tstop <unit path> - to stop the unit\n"+
"\texit - to exit processor\n"+
"-------------------------------";
    /**
    help command handle
    */
    protected void process_help_Command(String options) throws Exception
    {
        if ("".equals(options)) {
            this.output.println(processorHelp);
        }else {
            command cmd = new command(options);
            Method process = this.getProcess("help_"+cmd.Name);
            if (process != null) process.invoke(this,new String[]{cmd.Option});
            else{
                this.output.println("! Sorry can't help you with "+cmd.Name);
            }
        }
    }
    /**
    help help command handle
    */
    protected void process_help_help_Command(String options) throws Exception
    {
        this.output.println("The processor's help system.");
        this.output.println("  For help you can use also: man | howto | h | ?");
    }
    /**
    threads command handle
    */
    protected void process_threads_Command(String options) throws Exception
    {
        ThreadGroup group = this.findThreadGroup( options );
        if (group != null)          this.print(group,0);
        else this.output.println("[Group]> "+options+" not found...");
    }
    /**
    help help command handle
    */
    protected void process_help_threads_Command(String options) throws Exception
    {
        this.output.println("To show active threads.");
        this.output.println("  For threads you can use also: ! | t");
    }
    /** To print Threads group recursive */
    private final void print(ThreadGroup group,int margin){
        int count = group.activeGroupCount();
        this.printGroup(group, margin);
        if (count > 0) {
            ThreadGroup set[] = new ThreadGroup[count];
            count = group.enumerate(set, false);// recursive iteration
            for(int i=0;i < count;i++){this.print(set[i],margin+1);}
        }
    }
    /** To print group threads */
    private void printGroup(ThreadGroup group,int margin) {
        int count = group.activeCount();
        Thread list[] = new Thread[ count ];
        count = group.enumerate( list, false );
        this.margin(margin);
        this.output.println("[Group]> "+group.toString());
        for (int i=0;i < count;i++){
            try {
                this.margin(margin);
                this.output.println("\t"+(i+1)+")\t"+ list[i].toString() );
            }catch(Exception e){}
        }
    }
    /** to print margin for member of threads group */
    private final void margin(int margin) {
        for(int i=0;i < margin;i++)this.output.print("\t");
    }
    /**
    to find threads group by name. If not found returns null
    */
    private final ThreadGroup findThreadGroup(String name) {
        ThreadGroup group = Thread.currentThread().getThreadGroup();
        if ( "".equals(name) ) return group;// owner of this thread
        // get the root of all threads group
        while( group.getParent() != null ) group = group.getParent();
        if (group.getName().equals(name)) return group;// "system" group
        int count = group.activeGroupCount();
        ThreadGroup all[] = new ThreadGroup[count];
        count = group.enumerate(all, true);
        // to iterate groups list
        for(int i=0;i < count;i++){
            String grName = all[i].getName();
            if (grName.equals(name) ) return all[i];
            else
            if (grName.equals("main") && "all".equals(name)) return all[i];
        }
        return null;
    }
    /**
    <command_hanler>
    Unknow command answer
    */
    private void unknownCommand(command cmd) throws IOException
    {
        this.prompt();
        this.output.println("Unknown command ["+cmd.Name+"] !");
    }
}
