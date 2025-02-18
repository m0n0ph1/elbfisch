/**
 * PROJECT   : Elbfisch - java process automation controller (jPac)
 * MODULE    : IoLogical.java
 * VERSION   : -
 * DATE      : -
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
 */

package org.jpac.plc;

import org.apache.log4j.Logger;
import org.jpac.AbstractModule;
import org.jpac.Logical;
import org.jpac.SignalAccessException;
import org.jpac.SignalAlreadyExistsException;
import org.jpac.SignalInvalidException;

/**
 *
 * @author berndschuster
 */
public class IoLogical extends Logical{
    static  Logger  Log = Logger.getLogger("com.msk.atlas.IoHandler");      
    private Address      address;
    private Data         data;
    private Data         bitData;
    private WriteRequest writeRequest;
    private IoDirection  ioDirection;
    private Connection   connection;
    
    public IoLogical(AbstractModule containingModule, String name, Data data, Address address, IoDirection ioDirection) throws SignalAlreadyExistsException{
        super(containingModule, name);
        this.data        = data;
        this.address     = address; 
        this.ioDirection = ioDirection;
    }
    /**
     * used to check, if this signal has been changed by the plc. If so, the signal change is automatically
     * propagated to all connected signals
     * @throws SignalAccessException
     * @throws AddressException 
     */
    public void check() throws SignalAccessException, AddressException{
        set(data.getBIT(address.getByteIndex(), address.getBitIndex()));        
        if (isChanged()){
            try{if (Log.isDebugEnabled()) Log.debug(this + " set to " + is(true));}catch(SignalInvalidException exc){/*cannot happen*/};
        }
            try{if (Log.isInfoEnabled()) Log.info(this + " set to " + is(true));}catch(SignalInvalidException exc){/*cannot happen*/};
    }
    
    /**
     * returns a write request suitable for transmitting this signal to the plc
     * @param connection
     * @return 
     */
    public WriteRequest getWriteRequest(Connection connection){
       boolean errorOccured = false;
        try{
            if (bitData == null || this.connection == null || this.connection != connection){
                bitData           = connection.generateDataObject(1);
                this.connection   = connection;
                this.writeRequest = null;
            }
            bitData.setBYTE(0, isValid() && is(true) ? 0x01 : 0x00);
            if (writeRequest == null){
               writeRequest = connection.generateWriteRequest(Request.DATATYPE.BIT, address, 0, bitData);
            }
            else{
               writeRequest.setData(bitData);
            }
        }
        catch(Exception exc){
            Log.error("Error: " + exc);
            errorOccured = true;
        }
        return errorOccured ? null : writeRequest;  
    }
    
    /**
     * @return the ioDirection
     */
    public IoDirection getIoDirection() {
        return ioDirection;
    }
    
    @Override
    public String toString(){
       return super.toString() + (ioDirection == IoDirection.INPUT ? " <- " : " -> ") + address.toString(); 
    }    
}
