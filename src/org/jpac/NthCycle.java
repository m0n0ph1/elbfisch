/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : NthCycle.java
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
 * LOG       : $Log: NthCycle.java,v $
 * LOG       : Revision 1.3  2012/03/09 10:30:29  schuster
 * LOG       : Firable.fire(), Fireable.reset() made public
 * LOG       :
 * LOG       : Revision 1.2  2012/03/09 09:24:24  schuster
 * LOG       : JPac handling breakpoints
 * LOG       :
 */

package org.jpac;

public class NthCycle extends ProcessEvent{
    private int cycleCount;
    private int n;

    public NthCycle(int n){
        if (n < 0){
            n = 0;
        }
        cycleCount = 0;
        this.n = n;
    };

    public boolean fire() throws ProcessException{
       boolean localFired;
       cycleCount++;
       localFired = cycleCount >= n;
       return localFired;
    }

    @Override
    public void reset(){
        super.reset();
        cycleCount = 0;
    }
    
    public void setN(int n){
        this.n = n;
    }
    
    public int getN(){
       return this.n;
    }
}
