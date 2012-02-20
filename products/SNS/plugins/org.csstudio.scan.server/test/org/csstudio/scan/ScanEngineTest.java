/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The scan engine idea is based on the "ScanEngine" developed
 * by the Software Services Group (SSG),  Advanced Photon Source,
 * Argonne National Laboratory,
 * Copyright (c) 2011 , UChicago Argonne, LLC.
 *
 * This implementation, however, contains no SSG "ScanEngine" source code
 * and is not endorsed by the SSG authors.
 ******************************************************************************/
package org.csstudio.scan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.csstudio.scan.command.Comparison;
import org.csstudio.scan.command.DelayCommand;
import org.csstudio.scan.command.LogCommand;
import org.csstudio.scan.command.LoopCommand;
import org.csstudio.scan.command.SetCommand;
import org.csstudio.scan.command.WaitCommand;
import org.csstudio.scan.commandimpl.LogCommandImpl;
import org.csstudio.scan.commandimpl.LoopCommandImpl;
import org.csstudio.scan.commandimpl.SetCommandImpl;
import org.csstudio.scan.commandimpl.WaitCommandImpl;
import org.csstudio.scan.device.DeviceContext;
import org.csstudio.scan.device.DeviceInfo;
import org.csstudio.scan.server.ScanState;
import org.csstudio.scan.server.internal.Scan;
import org.csstudio.scan.server.internal.ScanEngine;
import org.junit.Test;

/** [Headless] JUnit Plug-in test of the {@link ScanEngine}
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ScanEngineTest
{
    private void waitForState(final Scan scan, final ScanState state) throws InterruptedException
    {
        do
        {
            Thread.sleep(200);
        }
        while (scan.getScanInfo().getState() != state);
    }


    private DeviceContext getDemoDevices() throws Exception
    {
        final DeviceContext devices = new DeviceContext();
        devices.addPVDevice(new DeviceInfo("motor_x", "xpos", true, true));
        devices.addPVDevice(new DeviceInfo("motor_y", "ypos", true, true));
        return devices;
    }

    /** Test scans with pause/resume (15 secs) */
    @Test(timeout=30000)
    public void testScanEngine() throws Exception
    {
        final DeviceContext devices = getDemoDevices();

        final Scan scan_x = new Scan("Scan Motor X",
            new LoopCommandImpl(
                new LoopCommand("xpos", 1.0, 5.0, 1.0,
                    new DelayCommand(1.0),
                    new LogCommand("xpos")
                )
            )
        );

        final Scan scan_y = new Scan("Scan Motor Y",
            new LoopCommandImpl(
                new LoopCommand("ypos", 1.0, 5.0, 1.0,
                    new DelayCommand(1.0),
                    new LogCommand("ypos"))));

        final ScanEngine engine = new ScanEngine();
        engine.start();

        engine.submit(devices, scan_x);
        engine.submit(devices, scan_y);

        // List scans and their state
        List<Scan> scans = engine.getScans();
        assertEquals(2, scans.size());

        // Second scan should be idle.
        assertEquals(ScanState.Idle, scans.get(1).getScanInfo().getState());
        assertEquals("Scan Motor Y", scans.get(1).getScanInfo().getName());

        // Wait for 1st scan to start
        do
        {
            scans = engine.getScans();
            System.out.println(scans.get(0).getScanInfo());
            assertSame(scan_x, scans.get(0));
            assertFalse(engine.isIdle());
            Thread.sleep(200);
        }
        while (scan_x.getScanInfo().getState() != ScanState.Running);

        // Pause it
        scan_x.pause();

        for (int i=0; i<4; ++i)
        {
            System.out.println(scan_x.getScanInfo());
            assertFalse(engine.isIdle());
            assertEquals(ScanState.Paused, scan_x.getScanInfo().getState());
            Thread.sleep(200);
        }

        // Resume, wait for 1st scan to finish
        scan_x.resume();
        do
        {
            scans = engine.getScans();
            System.out.println(scans.get(0).getScanInfo());
            assertSame(scan_x, scans.get(0));
            assertFalse(engine.isIdle());
            Thread.sleep(200);
        }
        while (scan_x.getScanInfo().getState() != ScanState.Finished);
        System.out.println(scan_x.getScanInfo());

        // Wait for 2nd scan to finish
        do
        {
            scans = engine.getScans();
            System.out.println(scans.get(1).getScanInfo());
            assertSame(scan_y, scans.get(1));
            Thread.sleep(200);
        }
        while (! engine.isIdle());

        System.out.println(scan_x.getScanInfo());
        System.out.println(scan_y.getScanInfo());

        engine.stop();
        scans = engine.getScans();
        assertEquals(0, scans.size());
    }

    @Test(timeout=10000)
    public void testErrors() throws Exception
    {
        final DeviceContext devices = getDemoDevices();

        final Scan scan = new Scan("Scan Motor X",
            new LoopCommandImpl(
                new LoopCommand("xpos", 1.0, 5.0, 1.0,
                    new LogCommand("xpos")
                )
            )
        );

        final ScanEngine engine = new ScanEngine();
        engine.start();
        engine.submit(devices, scan);

        waitForState(scan, ScanState.Finished);

        // Submit same scan again, which causes error
        engine.submit(devices, scan);

        // Wait for failure...
        waitForState(scan, ScanState.Failed);

        final String error = scan.getScanInfo().getError();
        assertNotNull(error);
        System.out.println("Received expected error: " + error);
        assertTrue(error.toLowerCase().contains("cannot"));
        assertTrue(error.toLowerCase().contains("finished"));

        engine.stop();
    }

    @Test(timeout=10000)
    public void testEngineStop() throws Exception
    {
        final DeviceContext devices = getDemoDevices();

        // Scan that will hang
        final Scan scan = new Scan("Scan Motor X",
                new SetCommandImpl(new SetCommand("xpos", 2.0)),
                new WaitCommandImpl(new WaitCommand("xpos", Comparison.EQUALS, 2.0, 0.1, 0.0)),
                new LogCommandImpl(new LogCommand("xpos")),
                new WaitCommandImpl(new WaitCommand("xpos", Comparison.EQUALS, 10.0, 0.1, 0.0))
        );

        final ScanEngine engine = new ScanEngine();
        engine.start();
        engine.submit(devices, scan);

        // Wait for scan to start
        waitForState(scan, ScanState.Running);
        // Allow it to get hung up
        Thread.sleep(1000);

        // Stop engine
        engine.stop();
        // Thread should not continue...
        Thread.sleep(1000);

        final List<Scan> scans = engine.getScans();
        assertEquals(0, scans.size());

        System.out.println(scan.getScanInfo());
        assertEquals(ScanState.Aborted, scan.getScanInfo().getState());
    }

    @Test(timeout=10000)
    public void testAbort() throws Exception
    {
        final DeviceContext devices = getDemoDevices();

        // Scan that will hang
        final Scan scan = new Scan("Scan Motor X",
                new SetCommandImpl(new SetCommand("xpos", 2.0)),
                new WaitCommandImpl(new WaitCommand("xpos", Comparison.EQUALS, 2.0, 0.1, 0.0)),
                new LogCommandImpl(new LogCommand("xpos")),
                new WaitCommandImpl(new WaitCommand("xpos", Comparison.EQUALS, 10.0, 0.1, 0.0))
        );

        final ScanEngine engine = new ScanEngine();
        engine.start();
        engine.submit(devices, scan);

        // Wait for scan to start
        waitForState(scan, ScanState.Running);
        // Allow it to do a little work
        Thread.sleep(1000);

        engine.abortScan(scan);

        // Thread should not continue...
        Thread.sleep(1000);

        System.out.println(scan.getScanInfo());
        assertEquals(ScanState.Aborted, scan.getScanInfo().getState());

        engine.stop();
    }
}
