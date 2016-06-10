package org.csstudio.logbook.olog.property.fault;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.handlers.HandlerUtil;

public class OpenFaultEditorDialog extends AbstractHandler{

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        FaultEditorDialog dialog = new FaultEditorDialog(HandlerUtil.getActiveShell(event));
        dialog.setBlockOnOpen(false);
        if (dialog.open() == Window.OK) {
        }
        return null;
    }

}
