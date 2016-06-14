package org.csstudio.logbook.olog.property.fault;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.csstudio.logbook.LogEntry;
import org.csstudio.logbook.LogEntryBuilder;
import org.csstudio.logbook.Logbook;
import org.csstudio.logbook.LogbookBuilder;
import org.csstudio.logbook.LogbookClient;
import org.csstudio.logbook.LogbookClientManager;
import org.csstudio.logbook.Property;
import org.csstudio.logbook.PropertyBuilder;
import org.csstudio.logbook.Tag;
import org.csstudio.logbook.TagBuilder;
import org.csstudio.logbook.util.LogEntryUtil;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import static org.csstudio.logbook.LogEntryBuilder.*;
import static org.csstudio.logbook.TagBuilder.*;

public class FaultEditorDialog extends Dialog {
    

    private LogbookClient client;
    private List<String> tags = Collections.emptyList();
    private List<String> logbooks = Collections.emptyList();
    private List<LogEntry> data;
    
    private FaultEditorWidget faultEditorWidget;
    private String level;
    
    protected FaultEditorDialog(Shell parentShell, List<LogEntry> data) {
        super(parentShell);
        this.data = data;
        //TODO This needs to be moved to another thread.
        if (client == null) {
            try {
                client = LogbookClientManager.getLogbookClientFactory().getClient();
                tags = client.listTags().stream().map(Tag::getName).collect(Collectors.toList());
                logbooks = client.listLogbooks().stream().map(Logbook::getName).collect(Collectors.toList());
                level = Platform.getPreferencesService().getString(
                        "org.csstudio.logbook.olog.property.fault", "fault.level",
                        "Problem",
                        null);
            } catch (Exception e) {
                Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Failed to open fault dialog", e);
                ErrorDialog.openError(parentShell, "Failed to open fault dialog",
                        e.getLocalizedMessage(), status);
            }
        }
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        getShell().setText("Create Fault Report");
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new FillLayout(SWT.HORIZONTAL));
        FaultConfiguration fc = FaultConfigurationFactory.getConfiguration();
        faultEditorWidget = new FaultEditorWidget(container, SWT.NONE, fc, logbooks, tags);
        if(data == null || data.isEmpty()){
            
        } else if(data.size() == 1 && LogEntryUtil.getProperty(data.get(0), FaultAdapter.FAULT_PROPERTY_NAME) != null){
            Property faultProperty = LogEntryUtil.getProperty(data.get(0), FaultAdapter.FAULT_PROPERTY_NAME);
            faultEditorWidget.setFault(FaultAdapter.extractFaultFromLogEntry(data.get(0)));
            if (faultProperty.getAttributeNames().contains(FaultAdapter.FAULT_PROPERTY_ATTR_LOGIDS)) {
                faultEditorWidget.setLogIds(Arrays
                        .asList(faultProperty.getAttributeValue(FaultAdapter.FAULT_PROPERTY_ATTR_LOGIDS).split(";")));
            }
            faultEditorWidget.setLogbooks(LogEntryUtil.getLogbookNames(data.get(0)));
            faultEditorWidget.setTags(LogEntryUtil.getTagNames(data.get(0)));
        } else {
            faultEditorWidget.setLogIds(data.stream().map(new Function<LogEntry, String>() {
                @Override
                public String apply(LogEntry logEntry) {
                    return String.valueOf(logEntry.getId()); 
                }
            }).collect(Collectors.toList()));
        }
        return container;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        // create OK and Cancel buttons by default
        createButton(parent, IDialogConstants.OK_ID, "Submit", true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected void okPressed() {
        // Create the fault report
        Fault fault = faultEditorWidget.getFault();

        String faultText = FaultAdapter.createFaultText(fault);

        Collection<LogbookBuilder> newLogbooks = faultEditorWidget.getLogbooks().stream().map((logbookName) -> {
            return LogbookBuilder.logbook(logbookName);
        }).collect(Collectors.toList());

        Collection<TagBuilder> newTags = faultEditorWidget.getTags().stream().map((tagName) -> {
            return TagBuilder.tag(tagName);
        }).collect(Collectors.toList());

        PropertyBuilder prop = FaultAdapter.createFaultProperty(fault, faultEditorWidget.getLogIds());

        try {
            if (fault.getId() == 0) {
                client.createLogEntry(LogEntryBuilder
                        .withText(faultText)
                        .setLevel(level)
                        .setLogbooks(newLogbooks)
                        .setTags(newTags)
                        .addTag(tag("Fault"))
                        .addProperty(prop).build());
            } else {
                client.updateLogEntry(LogEntryBuilder
                        .logEntry(data.get(0))
                        .setText(faultText)
                        .setLevel(level)
                        .setLogbooks(newLogbooks)
                        .setTags(newTags)
                        .addTag(tag("Fault"))
                        .addProperty(prop)
                        .build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.okPressed();
    }
    
    @Override
    protected boolean isResizable() {
        return true;
    }
    
}
