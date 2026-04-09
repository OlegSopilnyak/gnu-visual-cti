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
package org.visualcti.workflow;
import java.util.*;
import org.visualcti.briquette.*;
import org.visualcti.briquette.core.*;
import org.visualcti.server.task.*;
import org.visualcti.workflow.facade.*;
/**
 * <p>Title: Visual CTI Java Telephony Server</p>
 * <p>Description: VisualCTI WorkFlow, The IDE's runtime part</p>
 * <p>Copyright: Copyright Prominic Inc & Prominic Ukraine Co(c) 2002</p>
 * <p>Company: Prominic Ukraine Co</p>
 * @author Sopilnyak Oleg
 * @version 1.0
 */
final class ideRuntime {
/**
 * <attribute>
 * The owner of runtime
 */
private final IDE ide;
  /**
   * <constructor>
   * To make the IDE's runtime
   * @param ide the owner of runtime
   */
  ideRuntime(IDE ide)
  {
    this.ide=ide;
    this.pause=true;
    this.environment.setPart( "database", dbTools.getDatabase() );
    this.environment.setPart( "stdout", System.out );
    this.environment.setPart( "stderr", System.err );
    this.environment.setPart( "timer", new Timer(true) );
    new mainThread().start();
  }
/**
 * <attribute>
 * The equipment of the IDE
 */
private final Equipment equipment = new Equipment();
/**
 * <attribute>
 * The runtime's Environment of the Task
 */
private final Environment environment = new Environment();
  /**
   * <accessor>
   * To get access to IDE's equipment
   * @return the equipment
   */
  public final Equipment getEquipment() {return equipment;}
  /**
   * To get access to Task's runtime environment
   * @return the environment
   */
  public final Environment getEnvironment() {return environment;}
  /**
   * <action>
   * To visualize current briquette
   */
  public final void Find()
  {
    if ( this.briquette == null) return;
    visualChain chain = this.ide.getChain();
    visualOperation v = chain.getModel().getVisualOperation(this.briquette);
    chain.getModel().showOperation( v );
    chain.visualize( v );
  }
/**
 * <attribute>
 * The current briquette
 */
private Operation briquette = null;
/**
 * <prepare>
 * To init the parameters of Subroutine
 * @param caller the source of input parameters' values
 * @return true if success
 */
private final boolean initSubroutineCall(Subroutine caller){
  Subroutine call = (Subroutine)this.briquette;
  Entity entity = call.getEntity();
  if (entity == null || caller == null) {
    this.Stop(); return false;
  }else if( entity instanceof ChainEntity ){
    Chain chain = ( (ChainEntity) entity).getChain();
    if ( chain == null ) return false;
    call.initParameters( caller );
    this.briquette = chain.getMainOperation();
    if (this.briquette == null) {
      this.Stop(); return false;
    }
    // to visualize the graph of the Chain
    this.ide.openGraph( chain );
    this.setExecuted(this.briquette,true);
    this.chains.push(chain);
    this.subroutines.push(call);
    // to assign the pool with Pool's visualizer
    visualValues poolVisualizer = (visualValues)this.ide.getFacade().valuesPlace();
    poolVisualizer.assign( call.getPool() );
    return true;
  }
  return false;
}
/**
 * <prepare>
 * To finish the Subroutine's sequence
 * @return true if success
 */
private final boolean finishSubroutineCall(){
  // to remove the call from stack
  Subroutine call = (Subroutine)this.subroutines.pop();
  if ( this.subroutines.empty() ) {this.Stop(); return false;}
  // the caller in top of the stack
  Subroutine caller = (Subroutine)this.subroutines.peek();
  if ( call == null || caller == null ){
    this.Stop(); return false;
  }
// to remove unsused chain
  this.chains.pop();
// to visualize last graph
  this.ide.openGraph( (Chain)this.chains.peek());
  this.setExecuted(call, false);
  this.briquette = call.getLink(Operation.DEFAULT_LINK);
  // to return the parameters' values to caller
  call.returnParameters( caller );
  // to assign the pool with Pool's visualizer
  visualValues poolVisualizer = (visualValues)this.ide.getFacade().valuesPlace();
  poolVisualizer.assign( caller.getPool() );
  // success
  return true;
}
private transient boolean oneStep=false;
/**
 * <action>
 * To execute current briquette
 */
public final void Step()
{
  if ( this.pause && this.mainThreadPaused )
  {
    this.ide.setEnabled("Step",false);
    this.ide.setEnabled("Run",false);
    this.oneStep = true; this.pause = false;
    synchronized(this.context){this.context.notify();}
  }
}
/**
 * <flag>
 * Flag, is briquette executed
 */
private transient boolean executed=false;
/**
 * <action>
 * To execute current
 */
private final void stepInto()
{
  if ( this.briquette == null ) {this.Stop();return;}
  if ( this.isBreakPoint(this.briquette) )
  {
    visualBreakpoints visual =
        (visualBreakpoints)this.ide.getFacade().breakpointsPlace();
    visual.stopAtBreakpoint( this.briquette, false );

  }
  Subroutine caller = (Subroutine)this.subroutines.peek();
  if ( this.briquette instanceof Subroutine )
  {// to init the parameters of Subroutine-briquette
    if ( this.initSubroutineCall(caller) ) return;
  }
  this.ide.setEnabled("Stop", true);
  // to execute the briquette
  this.ide.getProgramm().printMessage("Executing:"+this.briquette.getAbout());
  try{Thread.sleep(50);}catch(Exception e){}
  this.executed = true;
  Operation next = this.briquette.doIt( (Subroutine)this.subroutines.peek() );
  this.executed = false;
  this.setExecuted(this.briquette, false);
  this.briquette = next;

  if ( this.briquette == null )
  {// to process the Subroutine's call result
    if ( !this.finishSubroutineCall() ) return;
  }
  // to adjust the visual components
  this.setExecuted( this.briquette, true );
  if ( !this.pause ){
    this.pause = this.isBreakPoint( this.briquette );
    visualBreakpoints visual =
        (visualBreakpoints)this.ide.getFacade().breakpointsPlace();
    visual.stopAtBreakpoint( this.briquette, true );
  }
  if ( this.briquette == null ) this.Stop();
}
/**
 * <visualization>
 * To mark/unmark the briquette as executed
 * @param briquette the target briquette
 * @param flag exectution's flag
 */
private final void setExecuted(Operation briquette,boolean flag){
  visualOperation visual = this.ide.getChain().getModel().getVisualOperation(briquette);
  if ( visual != null ) visual.setExecuted( flag );
}
/**
 * <accessor>
 * To check is briquette have a breakpoint flag
 * @param briquette the briquette to check
 * @return breakpoint's flag value
 */
private final boolean isBreakPoint(Operation briquette){
  visualOperation visual = this.ide.getChain().getModel().getVisualOperation(briquette);
  if ( visual != null ) return visual.isBreakPoint() && visual.isEnabledBreakPoint();
  return false;
}
/**
 * <attribute>
 * The stack of callers Subroutines
 */
private final Stack subroutines = new Stack();
/**
 * <attribute>
 * The stack of program's chains
 */
private final Stack chains = new Stack();
/**
 * <attribute>
 * The flag of program execution pause
 */
private transient boolean pause = false;
/**
 * <semaphore>
 * The semaphore for mainThread
 */
private final Object context = new Object();
/**
 * <action>
 * To reset the runtime
 */
final void Reset()
{
  this.pause = true; this.oneStep = false; this.executed=false;
  this.subroutines.clear(); this.chains.clear();
  Program prog = this.ide.getProgramm();
  prog.setMain( null );
  Chain chain = prog.getChain();
  // to connect to listen the pool's changes
  Subroutine sub = new Subroutine( prog );
  prog.setMain( sub );
  sub.initParameters( null );
  visualValues poolVisualizer = (visualValues)this.ide.getFacade().valuesPlace();
  poolVisualizer.assign( sub.getPool() );
  this.briquette = chain.getMainOperation();
  this.setExecuted(this.briquette,true);
  this.subroutines.push(sub);
  this.chains.push(chain);
  this.ide.setEnabled("Run", true);
  this.ide.setEnabled("Step", true);
  this.ide.setEnabled("Find",true);
  this.ide.getProgramm().printMessage("["+this.ide.getProgramm().getName()+"] ready.");
}
/**
 * <action>
 * To stop the program's execution
 */
 public final void Stop()
 {
   if ( this.executed )
   try{
     this.pause = true;
     this.briquette.stopExecute();
     // to wait a thread's pause
     while ( !this.mainThreadPaused )try{Thread.sleep(50);}catch(Exception e){}
     return;
   }catch(NullPointerException e){
    this.Reset();
    return;
   }
   if (this.isBreakPoint(this.briquette)) {
     visualBreakpoints visual =
         (visualBreakpoints)this.ide.getFacade().breakpointsPlace();
     visual.stopAtBreakpoint(this.briquette, false);
   }
   // to clear the reference to current briquette
   this.setExecuted(this.briquette, false);
   this.briquette = null;
   this.ide.setEnabled("Stop", false);
   this.ide.getProgramm().printMessage("["+this.ide.getProgramm().getName()+"] stopped.");
   this.Reset();
 }
 /**
  * <action>
  * To Run/Continue the excution
  */
 public final void Run()
 {
   this.pause = this.mainThreadPaused = false;
   this.ide.setEnabled("Run", false);
   this.ide.setEnabled("Step", false);
   this.ide.setEnabled("Find", false);
   this.ide.setEnabled("Pause", true);
   synchronized(this.context){this.context.notify();}
 }
 /**
  * <action>
  * To pause the chain's execution
  */
 public final void Pause()
 {
   this.pause = true;
   this.ide.setEnabled("Pause", false);
 }
private final void suspendSteps(){
 try {
   this.ide.setEnabled("Pause", false);
   this.ide.setEnabled("Run", true);
   this.ide.setEnabled("Step", true);
   this.ide.setEnabled("Find", true);
   this.mainThreadPaused = true;
   synchronized (this.context) {this.context.wait();}
 }catch (Exception e) {}
}
private final synchronized void oneStepCheck(){
  if ( !this.oneStep ) return;
  this.oneStep = false;
  this.pause = true;
  this.ide.setEnabled("Run", true);
  this.ide.setEnabled("Step", true);
  this.ide.setEnabled("Find", true);
}
 /**
  * <flag>
  * Flag, is main thread is paused
  */
 private transient boolean mainThreadPaused = false;
 /**
  * <executer>
  * The thread to execute the briquettes chain
  */
 private final class mainThread extends Thread{
   public final void run()
   {
     while( true )
     {
       if ( !pause ) {stepInto(); oneStepCheck();} else suspendSteps();
       this.yield();
     }
   }
 }
}
