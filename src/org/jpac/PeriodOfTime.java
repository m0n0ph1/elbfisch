/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : PeriodOfTime.java
 * VERSION   : $Revision: 1.3 $
 * DATE      : $Date: 2012/03/09 10:30:29 $
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
 * LOG       : $Log: PeriodOfTime.java,v $
 * LOG       : Revision 1.3  2012/03/09 10:30:29  schuster
 * LOG       : Firable.fire(), Fireable.reset() made public
 * LOG       :
 * LOG       : Revision 1.2  2012/03/09 09:24:24  schuster
 * LOG       : JPac handling breakpoints
 * LOG       :
 */

package org.jpac;

public class PeriodOfTime extends ProcessEvent{
    private long           periodOfTime;
    private long           timeoutTime;
    private JPac.CycleMode cycleMode;
    private NthCycle       nthCycle;

    public PeriodOfTime(long periodOfTime){
        if (periodOfTime < 0){
            periodOfTime = 0;
        }
        this.periodOfTime = periodOfTime;
        this.cycleMode = JPac.getInstance().getCycleMode();
        switch(this.cycleMode){
            case FreeRunning: 
                break;
            case Bound:
            case LazyBound:
                //compute periodOfTime as rounded number of cycles
                long  cycleTime  = JPac.getInstance().getCycleTime();
                long  cycleCount = (periodOfTime + (cycleTime >> 1))/cycleTime;
                if (cycleCount > Integer.MAX_VALUE){
                    cycleCount = Integer.MAX_VALUE;
                }
                nthCycle = new NthCycle((int)cycleCount);
                break;
        }
    };

    public boolean fire() throws ProcessException{
       boolean localFired = false;
        switch(this.cycleMode){
            case FreeRunning:
                localFired = timeoutTime < System.nanoTime();
                break;
            case Bound:
            case LazyBound:
                localFired = nthCycle.fire();
                break;
        }
       return localFired;
    }

    @Override
    public void reset(){
        switch(this.cycleMode){
            case FreeRunning:
                timeoutTime = System.nanoTime() + periodOfTime;
                break;
            case Bound:
            case LazyBound:
                nthCycle.reset();
                break;
        }
        super.reset();
    }
}
