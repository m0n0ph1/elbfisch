/**
 * PROJECT   : jPac java process automation controller
 * MODULE    : BooleanProperty.java
 * VERSION   : $Revision: 1.2 $
 * DATE      : $Date: 2012/06/27 15:07:44 $
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
 * LOG       : $Log: BooleanProperty.java,v $
 * LOG       : Revision 1.2  2012/06/27 15:07:44  schuster
 * LOG       : access of existing properties by foreign modules implemented
 * LOG       :
 * LOG       : Revision 1.1  2012/03/05 07:23:09  schuster
 * LOG       : introducing Properties
 * LOG       :
 */

package org.jpac.configuration;

import org.apache.commons.configuration.ConfigurationException;

/**
 *
 * @author berndschuster
 */
public class BooleanProperty extends Property{
    public BooleanProperty(Object owningObject, String key, boolean defaultValue, String comment, boolean classProperty) throws ConfigurationException{  
        super(owningObject, key, defaultValue, comment, classProperty);
    }  

    public BooleanProperty(Object owningObject, String key, boolean defaultValue, String comment) throws ConfigurationException{  
        super(owningObject, key, defaultValue, comment, false);
    }  

    public BooleanProperty(Object owningObject, String key, boolean defaultValue, boolean classProperty) throws ConfigurationException{  
        super(owningObject, key, defaultValue, null, classProperty);
    }  

    public BooleanProperty(Object owningObject, String key, boolean defaultValue) throws ConfigurationException{  
        super(owningObject, key, defaultValue, null, false);
    }  
    
    public BooleanProperty(String key) throws ConfigurationException{  
        super(key);
    }  

    public boolean get() throws ConfigurationException{
        return Configuration.getInstance().getBoolean(key);
    }
    
    public void set(boolean value) throws ConfigurationException{
        Configuration.getInstance().setProperty(key, value);
    }
}
