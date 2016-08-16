package org.csstudio.logbook.olog.property.fault;

import org.csstudio.ui.util.PopupMenuUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

public class TableView extends ViewPart {

    private FaultTableWidget faultTableWidget;

    public TableView() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void createPartControl(Composite parent) {
        GridLayout gl_parent = new GridLayout(1, false);
        gl_parent.verticalSpacing = 0;
        gl_parent.marginHeight = 0;
        gl_parent.marginWidth = 0;
        gl_parent.horizontalSpacing = 0;
        parent.setLayout(gl_parent);
        faultTableWidget = new FaultTableWidget(parent, SWT.NONE);
        faultTableWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        PopupMenuUtil.installPopupForView(faultTableWidget, getSite(), faultTableWidget);
    }

    @Override
    public void setFocus() {
        // TODO Auto-generated method stub

    }

}
