/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : ProcessEvent.java
 * VERSION   : $Revision: 1.4 $
 * DATE      : $Date: 2012/06/18 11:20:53 $
 * PURPOSE   : 
 * AUTHOR    : Bernd Schuster, MSK Gesellschaft fuer Automatisierung mbH, Schenefeld
 * REMARKS   : -
 * CHANGES   : CH#n <Kuerzel> <datum> <Beschreibung>
 *
 * This file is part of the jPac process automation controller.
 * jPac is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jPac is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the jPac If not, see <http://www.gnu.org/licenses/>.
 *
 * LOG       : $Log: ProcessEvent.java,v $
 * LOG       : Revision 1.4  2012/06/18 11:20:53  schuster
 * LOG       : introducing cyclic tasks
 * LOG       :
 * LOG       : Revision 1.3  2012/03/09 09:24:24  schuster
 * LOG       : JPac handling breakpoints
 * LOG       :
 * LOG       : Revision 1.2  2012/02/27 07:41:19  schuster
 * LOG       : some minor changes
 * LOG       :
 */

package org.jpac;

import java.util.ArrayList;

public abstract class ProcessEvent extends Fireable{

    private   boolean                  timedout;
    private   boolean                  timeoutActive;
    private   boolean                  emergencyStopOccured;
    private   String                   emergencyStopCause;
    private   boolean                  shutdownRequested;
    private   boolean                  monitoredEventOccured;
    private   long                     timeoutPeriod;
    private   long                     timeoutNanoTime;
    private   String                   statusString;
    private   ArrayList<Fireable>      monitoredEvents;

    public ProcessEvent(){
        super();
        initStates();
    }

    @Override
    protected void initStates(){
        super.initStates();
        timedout                = false;
        timeoutActive           = false;
        emergencyStopOccured    = false;
        emergencyStopCause      = null;
        shutdownRequested       = false;
        monitoredEventOccured   = false;
        monitoredEvents         = null;
    }

    @Override
    public boolean isFired() throws ProcessException {
        //fired state persist for the whole process cycle
        //even though the fire-condition may change
        if (!fired){
            //check monitored events first ...
            if (monitoredEvents != null){
                //check whole list of monitored events
                for(Fireable f : monitoredEvents){
                    if (f.isFired()){
                        //if at least one is fired
                        //prepare notification of the observing module
                        monitoredEventOccured = true;
                        fired                 = true;
                        if (Log.isDebugEnabled()) Log.debug(this + " fired caused by monitored event : " + f);
                    }
                }
            }
            //... then check own fire condition
            super.isFired();
        }
        return fired;
    }


    public synchronized ProcessEvent await(long nanoseconds) throws EventTimedoutException, EmergencyStopException, ShutdownRequestException, ProcessException, OutputInterlockException, InputInterlockException, MonitorException, InconsistencyException{
        awaitImpl(true, nanoseconds, true);
        if (isTimedout())
            throw new EventTimedoutException(this);
        return this;
    }

    public synchronized ProcessEvent await() throws EmergencyStopException, ShutdownRequestException, ProcessException, OutputInterlockException, InputInterlockException, MonitorException, InconsistencyException{
        //do not propagate EventTimedoutException because
        //timeout conditions are not handled here
        awaitImpl(false, 0, true);
        return this;
    }

    private ProcessEvent awaitImpl(boolean withTimeout, long nanoseconds, boolean noteStatus) throws ShutdownRequestException, EmergencyStopException, ProcessException, OutputInterlockException, InputInterlockException, MonitorException, InconsistencyException{
        if (!(Thread.currentThread() instanceof AbstractModule)){
            throw new InconsistencyException("ProcessEvents cannot be awaited outside the work() context of modules");
        }
        AbstractModule module = (AbstractModule)Thread.currentThread();
        //reinitialize state var's
        reset();
        //if notation of the modules status is requested, do it here
        if (noteStatus){
            module.getStatus().enter(getStatusString());
        }
        //check for interlock conditions produced by the observing module
        module.postCheckInterlocks();
        //get and reset list of events monitored by the module
        module.resetMonitoredEvents();
        monitoredEvents = module.getMonitoredEvents();
        //store the module awaiting me
        setObservingModule(module);
        module.setAwaitedEvent(this);
        //register myself as an active waiting event
        register();
        //prepare timeout related vars
        setTimeoutPeriod(nanoseconds);
        setTimeoutNanoTime(System.nanoTime() + nanoseconds);
        timeoutActive = withTimeout;
        //now lay observing module to sleep until this event occurs
        synchronized(this){
            if (module.isAwakenedByProcessEvent()){
                //tell the automation controller that one of the modules, awakened by an process event
                //has come to an end for this cycle
                module.setAwakenedByProcessEvent(false);
                module.storeSleepNanoTime();
                module.getJPac().indicateCheckBack(this);
            }
            //wait, until an ProcessEvent or a timeout occurs
            do{
                try {wait();} catch (InterruptedException ex) {}
              }
            while(!isTimedout() && !isFired() && !isEmergencyStopOccured() && !isShutdownRequested() && !isProcessExceptionThrown());
            module.storeWakeUpNanoTime();
            module.resetSleepNanoTime();// invalidate sleepNanoTime
        }
        //if notation of the status was requested on call, remove it here
        if (noteStatus){
            module.getStatus().leave();
        }
        //no module is awaiting me
        //setObservingModule(null);
        //handle exceptions in order of relevance
        if (isShutdownRequested())
            throw new ShutdownRequestException();
        if (isEmergencyStopOccured())
            throw new EmergencyStopException(JPac.getInstance().getEmergencyStopExceptionCausedBy());
        if (isMonitoredEventOccured())
            throw new MonitorException(monitoredEvents);
        if (isProcessExceptionThrown()){
            if (getProcessException() instanceof InEveryCycleDoException){
                throw new InEveryCycleDoException(getProcessException().getCause());
            }
            else{
                throw new ProcessException(getProcessException());
            }
        }
        module.setAwaitedEvent(null);
        //check for incoming interlock conditions to be handled by the observing module
        module.preCheckInterlocks();
        return this;
    }
    
    public ConjunctiveEvent and(ProcessEvent anotherProcessEvent){
        //create a new conjunctive event and add myself as the first event
        ConjunctiveEvent conjEvent = new ConjunctiveEvent(this);
        //now add the other event
        conjEvent.and(anotherProcessEvent);
        return conjEvent;
    }

    public DisjunctiveEvent or(ProcessEvent anotherProcessEvent){
        //create a new disjunctive event and add myself as the first event
        DisjunctiveEvent disjEvent = new DisjunctiveEvent(this);
        //now add the other event
        disjEvent.or(anotherProcessEvent);
        return disjEvent;
    }

    public ExclusiveDisjunctiveEvent xor(ProcessEvent anotherProcessEvent){
        //create a new exclusive disjunctive event and add myself as the first event
        ExclusiveDisjunctiveEvent disjEvent = new ExclusiveDisjunctiveEvent(this);
        //now add the other event
        disjEvent.xor(anotherProcessEvent);
        return disjEvent;
    }

    public boolean isTimedout() {
        boolean localTimedout = false;
        if (timeoutActive){
            localTimedout = this.timedout || (System.nanoTime() - getTimeoutNanoTime()) > 0;
            this.timedout = localTimedout;
        }
        else{
            localTimedout = false;
        }
        return localTimedout;
    }

    private String getStatusString(){
        if (statusString == null){
           statusString = "await(" + this.toString() + ")";
        }
        return statusString;
    }
    
    private long getTimeoutPeriod() {
        return timeoutPeriod;
    }

    private void setTimeoutPeriod(long timeoutPeriod) {
        this.timeoutPeriod = timeoutPeriod;
    }

    private long getTimeoutNanoTime() {
        return timeoutNanoTime;
    }

    private void setTimeoutNanoTime(long timeoutNanoTime) {
        this.timeoutNanoTime = timeoutNanoTime;
    }

    protected boolean isEmergencyStopOccured() {
        return emergencyStopOccured;
    }

    protected void setEmergencyStopOccured(boolean emergencyStopOccured) {
        this.emergencyStopOccured = emergencyStopOccured;
    }

    protected void setEmergencyStopCause(String cause) {
        this.emergencyStopCause = cause;
    }

    protected void setShutdownRequested(boolean shutdownRequested) {
        this.shutdownRequested = shutdownRequested;
    }

    protected boolean isShutdownRequested() {
        return this.shutdownRequested;
    }

    protected boolean isMonitoredEventOccured() {
        return this.monitoredEventOccured;
    }

    @Override
    public String toString(){
        return this.getClass().getSimpleName();
    } 
}
