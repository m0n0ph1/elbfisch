/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : DecimalEvent.java
 * VERSION   : $Revision: 1.4 $
 * DATE      : $Date: 2012/04/30 06:36:05 $
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
 * LOG       : $Log: DecimalEvent.java,v $
 * LOG       : Revision 1.4  2012/04/30 06:36:05  schuster
 * LOG       : introducing histogramm acquisition, some minor changes concerning toString()
 * LOG       :
 * LOG       : Revision 1.3  2012/04/24 06:37:08  schuster
 * LOG       : some improvements concerning consistency
 * LOG       :
 * LOG       : Revision 1.2  2012/03/09 10:30:29  schuster
 * LOG       : Firable.fire(), Fireable.reset() made public
 * LOG       :
 */

package org.jpac;

abstract class DecimalEvent extends ProcessEvent{
    protected Decimal decimal;
    protected double  threshold;
    
    
    DecimalEvent(Decimal decimal, double threshold){
        super();
        this.decimal   = decimal;
        this.threshold = threshold;
    }
    
    public void setDecimal(Decimal decimal){
        this.decimal = decimal;
    }
    
    public Decimal getDecimal(){
        return this.decimal;
    }
    
    public void setThreshold(double threshold){
        this.threshold = threshold;
    }
    
    public double getThreshold(){
        return this.threshold;
    }
    
    @Override
    public String toString(){
        return getClass().getSimpleName() + "(" + decimal + ")";
    }
}
