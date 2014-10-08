/*******************************************************************************
 * Copyright (c) 2014 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.display.pvtable.model;

import org.epics.pvmanager.PV;
import org.epics.vtype.VDouble;
import org.epics.vtype.VEnum;
import org.epics.vtype.VNumber;
import org.epics.vtype.VString;
import org.epics.vtype.VType;

/** Saved value of a table item
 * 
 *  <p>Values are always saved as String.
 *  When reading autosave-files, the PV's data type
 *  is unknown until the channel connects,
 *  and a channel may never connect.
 *  To allow reading and writing files without
 *  changing the exact value format, the text
 *  is kept.
 *
 *  @author Kay Kasemir
 */
public class SavedScalarValue extends SavedValue
{
    final private String saved_value;
    
    /** Initialize from text
     *  @param value_text
     */
    public SavedScalarValue(final String value_text)
    {
        saved_value = value_text;
    }
    
    /** {@inheritDoc} */
    public boolean isEqualTo(final VType current_value, final double tolerance) throws Exception
    {
        if (current_value == null)
            return true;
        if (current_value instanceof VNumber)
        {
            final double v1 = ((VNumber)current_value).getValue().doubleValue();
            final double v2 = Double.parseDouble(saved_value);
            return Math.abs(v2 - v1) <= tolerance;
        }
        if (current_value instanceof VString)
        {
            final String v1 = ((VString)current_value).getValue();
            return v1.equals(saved_value);
        }
        if (current_value instanceof VEnum)
        {
            final int v1 = ((VEnum)current_value).getIndex();
            final int v2 = Integer.parseInt(saved_value);
            return Math.abs(v2 - v1) <= tolerance;
        }
        // TODO Array classes
        throw new Exception("Cannot compare against unhandled type " + current_value.getClass().getName());
    }
    
    /** {@inheritDoc} */
    public void restore(final PV<VType, Object> pv)
    {
        // Determine what type to write based on current value of the PV
        final VType pv_type = pv.getValue();
        if (pv_type instanceof VDouble)
            pv.write(Double.parseDouble(saved_value));
        else if (pv_type instanceof VNumber)
            pv.write(Long.parseLong(saved_value));
        else // Write as text
            pv.write(saved_value);
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return saved_value;
    }
}
