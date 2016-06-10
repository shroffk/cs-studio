package org.csstudio.logbook.olog.property.fault;

import java.util.Collections;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class FaultEditorDialog extends Dialog {

    protected FaultEditorDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        getShell().setText("Create Fault Report");
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new FillLayout(SWT.HORIZONTAL));
        
        FaultEditorWidget faultEditorWidget = new FaultEditorWidget(container, SWT.NONE,
                FaultConfigurationFactory.getConfiguration(), 
                Collections.emptyList(), 
                Collections.emptyList());
        return container;
    }
    

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        // create OK and Cancel buttons by default
        createButton(parent, IDialogConstants.OK_ID, "Submit", true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }
    
    @Override
    protected boolean isResizable() {
        return true;
    }
}
