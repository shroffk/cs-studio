package org.csstudio.logbook.olog.property.fault;
import java.util.ArrayList;
import java.util.List;

import org.csstudio.logbook.LogEntry;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

public class OpenFaultEditorDialog extends AbstractHandler{

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        final ISelection selection = HandlerUtil.getActiveMenuSelection(event);

        IAdapterManager adapterManager = Platform.getAdapterManager();
        final List<LogEntry> data = new ArrayList<LogEntry>();

        if (selection instanceof IStructuredSelection) {
            IStructuredSelection strucSelection = (IStructuredSelection) selection;

            for (Object iterable_element : strucSelection.toList()) {
                data.add((LogEntry) adapterManager.getAdapter(iterable_element, LogEntry.class));
            }

        }
        FaultEditorDialog dialog = new FaultEditorDialog(HandlerUtil.getActiveShell(event), data);
        dialog.setBlockOnOpen(false);
        // Initialize the logbooks and tags

        Display.getDefault().asyncExec(() -> {
            if (dialog.open() == Window.OK) {
            }
        });
        return null;
    }

}
