package org.csstudio.logbook.olog.property.fault;

import java.io.File;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.csstudio.logbook.LogEntry;
import org.csstudio.logbook.LogbookClient;
import org.csstudio.logbook.LogbookClientManager;
import org.csstudio.logbook.ui.LogQueryListener;
import org.csstudio.logbook.ui.PeriodicLogQuery;
import org.csstudio.logbook.ui.PeriodicLogQuery.LogResult;
import org.eclipse.core.runtime.Platform;
import org.eclipse.fx.ui.workbench3.FXViewPart;
import org.eclipse.swt.widgets.Display;

import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import jfxtras.controls.agenda.AgendaLastWeekDaysFromDisplayedSkin;
import jfxtras.scene.control.agenda.Agenda;
import jfxtras.scene.control.agenda.Agenda.Appointment;
import jfxtras.scene.control.agenda.Agenda.AppointmentGroup;
import jfxtras.scene.control.agenda.Agenda.AppointmentImplLocal;

/**
 * 
 * @author Kunal Shroff
 *
 */
public class FaultViewer extends FXViewPart {

    // Initialize resources:
    public FaultViewer() {
    }

    private LogbookClient logbookClient;
    private Agenda agenda;
    private AgendaLastWeekDaysFromDisplayedSkin skin;

    // Model
    private PeriodicLogQuery logQuery;

    // Model listener
    private LogQueryListener listener = new LogQueryListener() {

        @Override
        public void queryExecuted(final LogResult result) {
            Display.getDefault().asyncExec(() -> {
                List<Appointment> appointments = result.logs.stream().map(new Function<LogEntry, Agenda.Appointment>() {
                    @Override
                    public Agenda.Appointment apply(LogEntry t) {
                        Fault fault = FaultAdapter.extractFaultFromLogEntry(t);
                        if (fault.getFaultOccuredTime() != null) {
                            AppointmentImplLocal appointment = new Agenda.AppointmentImplLocal();
                            appointment.withSummary(faultSummaryString(fault));
                            appointment.withDescription(faultString(fault));
                            appointment.withStartLocalDateTime(
                                    LocalDateTime.ofInstant(fault.getFaultOccuredTime(), ZoneId.systemDefault()));
                            if (fault.getFaultClearedTime() != null) {
                                appointment.withEndLocalDateTime(
                                        LocalDateTime.ofInstant(fault.getFaultClearedTime(), ZoneId.systemDefault()));
                            } else {
                                appointment.withEndLocalDateTime(
                                        LocalDateTime.ofInstant(fault.getFaultOccuredTime(), ZoneId.systemDefault()));
                            }
                            appointment.setAppointmentGroup(appointmentGroupMap.get("group00"));
                            return appointment;
                        } else{
                            return null;
                        }
                    }
                }).filter(a -> a != null).collect(Collectors.toList());
                agenda.appointments().setAll(appointments);
            });
        };
    };
    Map<String, Agenda.AppointmentGroup> appointmentGroupMap = new TreeMap<String, Agenda.AppointmentGroup>();

    private void initialize() {
        ExecutorService ex = Executors.newFixedThreadPool(1);
        ex.execute(() -> {
            try {
                logbookClient = LogbookClientManager.getLogbookClientFactory().getClient();
                logQuery = new PeriodicLogQuery("property:fault limit:500", logbookClient, 60, TimeUnit.SECONDS);
                logQuery.addLogQueryListener(listener);
                logQuery.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected Scene createFxScene() {
        AnchorPane anchorpane = new AnchorPane();
        final Scene scene = new Scene(anchorpane);
        agenda = new Agenda();
        agenda.setEditAppointmentCallback(new Callback<Agenda.Appointment, Void>() {
            
            @Override
            public Void call(Appointment param) {
                // show context menu
                System.out.println("context");
                return null;
            }
        });
        agenda.setActionCallback((appointment) -> {
            // show detailed view
            System.out.println("double click");
            return null;
        });
        
        agenda.allowDraggingProperty().set(false);
        agenda.allowResizeProperty().set(false);
        // find the css file
        String faultCSS = Platform.getPreferencesService().getString("org.csstudio.logbook.olog.property.fault",
                            "fault.css", "Agenda.css", null);
        try {            
            agenda.getStylesheets().add(getClass().getResource("/Agenda.css").toString());
            agenda.getStylesheets().add(new File(faultCSS).toURI().toURL().toString());
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // load the groups from the configured css file
        appointmentGroupMap = agenda.appointmentGroups().stream()
                .collect(Collectors.toMap(AppointmentGroup::getDescription, Function.identity()));
        skin = new AgendaLastWeekDaysFromDisplayedSkin(agenda);
        skin.setDaysBeforeFurthest(-14);
        skin.setDaysAfterFurthest(7);
        agenda.setSkin(skin);

        AnchorPane.setTopAnchor(agenda, 6.0);
        AnchorPane.setBottomAnchor(agenda, 6.0);
        AnchorPane.setLeftAnchor(agenda, 6.0);
        AnchorPane.setRightAnchor(agenda, 6.0);
        anchorpane.getChildren().add(agenda);

        initialize();

        return scene;
    }

    @Override
    protected void setFxFocus() {
    }

    @Override
    public void dispose() {
        if (logQuery != null) {
            logQuery.removeLogQueryListener(listener);
            logQuery.stop();
        }
        super.dispose();
    }
    
    /**
     * A helper method to create string representation of fault for this calender view
     * @return
     */
    private String faultString(Fault fault) {
        StringBuffer sb = new StringBuffer();
        sb.append(fault.getArea() != null ? fault.getArea():"None");
        sb.append(":");
        sb.append(fault.getSubsystem() != null ? fault.getSubsystem():"None");
        sb.append(":");
        sb.append(fault.getDevice() != null ? fault.getDevice():"None");
        sb.append(System.lineSeparator());
        
        sb.append(fault.getAssigned() != null ? fault.getAssigned() : "no owner");
        sb.append(System.lineSeparator());
        
        sb.append(fault.getDescription());
        sb.append(System.lineSeparator());

        sb.append(fault.getRootCause());
        sb.append(System.lineSeparator());

        sb.append(fault.getRepairAction());
        sb.append(System.lineSeparator());
        
        sb.append(fault.getCorrectiveAction());
        sb.append(System.lineSeparator());
        return sb.toString();
    }
    
    /**
     * A helper method to create string representation of fault for this calender view
     * @return
     */
    private String faultSummaryString(Fault fault) {
        StringBuffer sb = new StringBuffer();
        sb.append(fault.getArea() != null ? fault.getArea():"None");
        sb.append(":");
        sb.append(fault.getSubsystem() != null ? fault.getSubsystem():"None");
        sb.append(":");
        sb.append(fault.getDevice() != null ? fault.getDevice():"None");
        sb.append(System.lineSeparator());
        
        sb.append(fault.getAssigned() != null ? fault.getAssigned() : "no owner");
        sb.append(System.lineSeparator());

        sb.append(fault.getDescription());
        sb.append(System.lineSeparator());
        return sb.toString();
    }
}
