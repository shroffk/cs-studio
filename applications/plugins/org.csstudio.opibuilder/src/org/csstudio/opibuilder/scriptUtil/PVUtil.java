/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.opibuilder.scriptUtil;


import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.util.BOYPVFactory;
import org.csstudio.opibuilder.util.DisplayUtils;
import org.csstudio.opibuilder.util.ErrorHandlerUtil;
import org.csstudio.simplepv.IPV;
import org.csstudio.simplepv.VTypeHelper;
import org.csstudio.ui.util.thread.UIBundlingThread;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartListener;
import org.eclipse.swt.widgets.Display;
import org.epics.util.time.Timestamp;
import org.epics.util.time.TimestampFormat;
import org.epics.vtype.AlarmSeverity;

/**The utility class to facilitate Javascript programming
 * for PV operation.
 * @author Xihui Chen
 *
 */
public class PVUtil{

	private static final TimestampFormat timeFormat = new TimestampFormat("yyyy/MM/dd HH:mm:ss.SSS"); //$NON-NLS-1$

	/**Create a PV and start it. PVListener can be added to the PV to monitor its
	 * value change, but please note that the listener is executed in non-UI thread.
	 * If the code need be executed in UI thread, please use {@link ScriptUtil#execInUI(Runnable, AbstractBaseEditPart)}.
	 * The monitor's maximum update rate is 50hz. If the PV updates faster than this rate, some updates
	 * will be discarded.
	 * <br>Example Jython script:
	 *
	 *  <pre>
	from org.csstudio.opibuilder.scriptUtil import PVUtil
	from org.csstudio.simplepv import IPVListener

	class MyPVListener(IPVListener):
		def valueChanged(self, pv):
			widget.setPropertyValue("text", PVUtil.getString(pv))

	pv = PVUtil.createPV("sim://noise", widget)
	pv.addListener(MyPVListener())
	 *  </pre>
	 *
	 * @param name name of the PV.
	 * @param widget the reference widget. The PV will stop when the widget is deactivated,
	 * so it is not needed to stop the pv in script.
	 * @return the PV.
	 * @throws Exception the exception that might happen while creating the pv.
	 */
	public final static IPV createPV(String name, AbstractBaseEditPart widget) throws Exception{

		final IPV pv = BOYPVFactory.createPV(name, false, 20);
		pv.start();
		widget.addEditPartListener(new EditPartListener.Stub(){

			@Override
			public void partDeactivated(EditPart arg0) {
				pv.stop();
			}

		});
		return pv;
	}

	 /** Try to get a double number from the PV.
     *  <p>
     *  Some applications only deal with numeric data,
     *  so they want to interprete integer, enum and double values
     *  all the same.
     *  @param pv the PV.
     *  @return A double, or <code>Double.NaN</code> in case the value type
     *          does not decode into a number, or
     *          <code>Double.NEGATIVE_INFINITY</code> if the value's severity
     *          indicates that there happens to be no useful value.
     */
	public final static double getDouble(IPV pv){
		checkPVValue(pv);
		return VTypeHelper.getDouble(pv.getValue());
	}

	protected static void checkPVValue(IPV pv) {
		if(pv.getValue() == null)
			throw new RuntimeException("PV " + pv.getName() + " has no value.");
	}

	 /** Try to get a long integer number from the PV.
     *  <p>
     *  Some applications only deal with numeric data,
     *  so they want to interprete integer, enum and double values
     *  all the same.
     *  @param pv the PV.
     *  @return A long integer.
     */
	public final static Long getLong(IPV pv){
		checkPVValue(pv);
		return VTypeHelper.getNumber(pv.getValue()).longValue();
	}

	  /** Try to get a double-typed array element from the Value.
     *  @param pv The PV.
     *  @param index The array index, 0 ... getSize()-1.
     *  @see #getSize(PV)
     *  @see #getDouble(PV)
     *  @return A double, or <code>Double.NaN</code> in case the value type
     *          does not decode into a number, or
     *          <code>Double.NEGATIVE_INFINITY</code> if the value's severity
     *          indicates that there happens to be no useful value.
     */
	public final static double getDouble(IPV pv, int index){
		checkPVValue(pv);
		return VTypeHelper.getDouble(pv.getValue(), index);
	}


	 /** Try to get a double-typed array from the pv.
     *  @param pv the pv.
     *  @see #getSize(IPV)
     *  @see #getDouble(IPV)
     *  @return A double array, or an empty double array in case the value type
     *          does not decode into a number, or if the value's severity
     *          indicates that there happens to be no useful value.
     */
	public final static double[] getDoubleArray(IPV pv){
		checkPVValue(pv);
		return VTypeHelper.getDoubleArray(pv.getValue());
	}

	 /** Try to get an integer-typed array from the pv.
     *  @param pv the pv.
     *  @see #getSize(IPV)
     *  @see #getLong(IPV)
     *  @return A long integer array, or an empty long integer array in case the value type
     *          does not decode into a number, or if the value's severity
     *          indicates that there happens to be no useful value.
     */
	public final static long[] getLongArray(IPV pv){
		checkPVValue(pv);
		Object wrappedArray = VTypeHelper.getWrappedArray(pv.getValue());
		if(wrappedArray != null && wrappedArray instanceof long[])
			return (long[]) wrappedArray;
		double[] dblArray = VTypeHelper.getDoubleArray(pv.getValue());
		long[] longArray = new long[dblArray.length];
		int i=0;
		for(double d : dblArray){
			longArray[i++] = (long) d;
		}
		return longArray;
	}

    /**Get the size of the pv's value
     * @param pv the pv.
     * @return Array length of the pv value. <code>1</code> for scalars. */
	public final static double getSize(IPV pv){
		checkPVValue(pv);
		return VTypeHelper.getSize(pv.getValue());
	}


	  /**
	 * Converts the given pv's value into a string representation. For string values,
	 * returns the value. For numeric (double and long) values, returns a
	 * non-localized string representation. Double values use a point as the
	 * decimal separator. For other types of values, the value's
	 * {@link IValue#format()} method is called and its result returned.
	 *
	 * @param pv
	 *            the pv.
	 * @return a string representation of the value.
	 */
	public final static String getString(IPV pv){
		checkPVValue(pv);
		return VTypeHelper.getString(pv.getValue());
	}

	/**Get the full info from the pv in this format
	 * <pre>timestamp value severity, status</pre>
	 * @param pv
	 * @return the full info string
	 */
	public final static String getFullString(IPV pv){
		checkPVValue(pv);
		return pv.getValue().toString();
	}


	/**Get the timestamp string of the pv
	 * @param pv the pv
	 * @return the timestamp in string.
	 */
	public final static String getTimeString(IPV pv){
		checkPVValue(pv);
		Timestamp time = VTypeHelper.getTimestamp(pv.getValue());
		if(time != null)
			return timeFormat.format(time);
		return ""; //$NON-NLS-1$
	}

	 /** Get milliseconds since epoch, i.e. 1 January 1970 0:00 UTC.
     *  <p>
     *  Note that we always return milliseconds relative to this UTC epoch,
     *  even if the original control system data source might use a different
     *  epoch (example: EPICS uses 1990), because the 1970 epoch is most
     *  compatible with existing programming environments.
     *  @param pv the pv
     *  @return milliseconds since 1970.
     */
	public final static double getTimeInMilliseconds(IPV pv){
		checkPVValue(pv);
		Timestamp time = VTypeHelper.getTimestamp(pv.getValue());
		if(time != null)
			return time.getSec()*1000 + time.getNanoSec()/1000000;
		return 0;
	}


	/**Get severity of the pv as an integer value.
	 * @param pv the PV.
	 * @return 0:OK; -1: Invalid or Undefined; 1: Major; 2:Minor.
	 */
	public final static int getSeverity(IPV pv){
		checkPVValue(pv);
		AlarmSeverity severity = VTypeHelper.getAlarmSeverity(pv.getValue());
		if(severity == null)
			return -1;
		switch (severity) {
		case MAJOR:
			return 1;
		case MINOR:
			return 2;
		case NONE:
			return 0;
		case UNDEFINED:
		case INVALID:
		default:
			return -1;
		}
	}

	/**Get severity of the PV as a string.
	 * @param pv the PV.
	 * @return The string representation of the severity.
	 */
	public final static String getSeverityString(IPV pv){
		checkPVValue(pv);
		AlarmSeverity severity = VTypeHelper.getAlarmSeverity(pv.getValue());
		if(severity == null)
			return "No Severity Info.";
		return severity.toString();
	}

	/**Get the status text that might describe the severity.
	 * @param pv the PV.
	 * @return the status string.
	 */
	public final static String getStatus(IPV pv){
		checkPVValue(pv);
		return VTypeHelper.getAlarmName(pv.getValue());
	}

    /**Write a PV in a background job. It will first creates and connects to the PV. After
     * PV is connected, it will set the PV with the value. If it fails to write, an error
 	 * dialog will pop up.
     * @param pvName name of the PV.
     * @param value value to write.
     * @param timeout maximum time to try connection.
     */
    public final static void writePV(final String pvName, final Object value,
    		final int timeout){
    	final Display display = DisplayUtils.getDisplay();
		Job job = new Job("Writing PV: " + pvName) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {

				try {
					IPV pv = BOYPVFactory.createPV(pvName);
					pv.start();
					try
					{
					    if(!pv.setValue(value, timeout*1000))
					        throw new Exception("Write Failed!");
					}
					finally
					{
					    pv.stop();
					}
				} catch (final Exception e) {
					UIBundlingThread.getInstance().addRunnable(
							display, new Runnable() {
                                public void run() {
									String message = "Failed to write PV:" + pvName
											+ "\n" +
											(e.getCause() != null? e.getCause().getMessage():e.getMessage());
									ErrorHandlerUtil.handleError(message, e, true, true);
								}
							});
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}

		};

		job.schedule();
    }

    /**Write a PV in a background job. It will first creates and connects to the PV. After
     * PV is connected, it will set the PV with the value. If it fails to write, an error
 	 * dialog will pop up. The maximum time to try connection is 10 second.
     * @param pvName name of the PV.
     * @param value value to write.
     */
    public final static void writePV(String pvName, Object value){
    	writePV(pvName, value, 10);
    }

}
