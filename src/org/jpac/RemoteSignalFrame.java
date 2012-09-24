/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : RemoteSignalFrame.java
 * VERSION   : $Revision: 1.2 $
 * DATE      : $Date: 2012/06/22 10:16:13 $
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
 * LOG       : $Log: RemoteSignalFrame.java,v $
 * LOG       : Revision 1.2  2012/06/22 10:16:13  schuster
 * LOG       : corrected RemoteFrame.copy()
 * LOG       :
 * LOG       : Revision 1.1  2012/03/30 13:54:35  schuster
 * LOG       : introducing remote signal handling
 * LOG       :
 */
package org.jpac;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author berndschuster
 */
public class RemoteSignalFrame implements Serializable, Cloneable{
    static Logger Log = Logger.getLogger("jpac.Remote");

    private long                        cycleNumber;
    private List<RemoteSignalTransport> transports;
    
    public RemoteSignalFrame(){
        this.cycleNumber = 0L;
        this.transports  = new ArrayList<RemoteSignalTransport>(100);
    }
    
    public void add(RemoteSignalTransport remoteSignalTransport){
        transports.add(remoteSignalTransport.getIndex(), remoteSignalTransport);
    }
    
    public void setCycleNumber(long cycleNumber){
        this.cycleNumber = cycleNumber;
    }
    
    public long getCycleNumber(){
        return this.cycleNumber;
    }
    
    public boolean signalsEqual(RemoteSignalFrame remoteSignalFrame){
        boolean equal = false;
        equal = remoteSignalFrame != null && transports.size() == remoteSignalFrame.transports.size();
        for (int i = 0; i < transports.size() && equal; i ++){
            equal = transports.get(i).equals(remoteSignalFrame.transports.get(i));
        }
        return equal;
    }
    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder(getClass().getSimpleName() + "(" + getCycleNumber());
        for (RemoteSignalTransport rst: transports){
            sb.append(", ").append(rst);
        }
        sb.append(')');
        return sb.toString();
    }
    
    @Override
    public RemoteSignalFrame clone() throws CloneNotSupportedException{
        RemoteSignalFrame clonedFrame = new RemoteSignalFrame();
        
        clonedFrame.setCycleNumber(cycleNumber);
        for (RemoteSignalTransport rst: transports){
            RemoteSignalTransport clonedTransport = rst.clone();
            clonedFrame.add(clonedTransport);
        }
        return clonedFrame;
    }
    
    public void copy(RemoteSignalFrame sourceFrame) throws CloneNotSupportedException{        
        setCycleNumber(sourceFrame.getCycleNumber());
        for (int i = 0; i < sourceFrame.transports.size(); i++){
            if (transports.size() <= i || transports.get(i) == null){
                transports.add(i, sourceFrame.transports.get(i).clone());
            }
            else{
                transports.get(i).copyValue(sourceFrame.transports.get(i));
            }
        }
        //remove trailing entries
        int targetSize = sourceFrame.getTransports().size();
        while(transports.size() > targetSize){
            transports.remove(targetSize-1);
        }
    }

    public boolean structureDifferent(RemoteSignalFrame otherFrame){
        boolean structureChanged = false;
        //the frame structure is different, if the number of entries differ ...
        structureChanged = transports.size() != otherFrame.transports.size();
        //... or at least one entry differs in type
        for (int i = 0; i < transports.size() && !structureChanged; i++){
            if (transports.get(i) != null && otherFrame.transports.get(i) != null){
                structureChanged = transports.get(i).getSignature() != otherFrame.transports.get(i).getSignature();
            }
            else{
                //both transports must be either null or != null at the same time
                structureChanged = transports.get(i) == null ^ otherFrame.transports.get(i) == null;
            }
        }
        return structureChanged;
    }
    
    List<RemoteSignalTransport> getTransports(){
        return transports;
    }

}
