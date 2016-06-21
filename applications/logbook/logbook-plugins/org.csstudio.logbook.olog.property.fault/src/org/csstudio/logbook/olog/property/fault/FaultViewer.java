package org.csstudio.logbook.olog.property.fault;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.csstudio.logbook.LogbookClient;
import org.csstudio.logbook.LogbookClientManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * 
 * @author Kunal Shroff
 *
 */
public class FaultViewer extends ViewPart {
    
    
    // Initialize resources:
    public FaultViewer() {
        initialize();
    }
    
    private Future<LogbookClient> client;
    private LogbookClient logbookClient;

    private void initialize() {
        ExecutorService ex = Executors.newFixedThreadPool(1);
        client = ex.submit(()->{
            try {
                return LogbookClientManager.getLogbookClientFactory().getClient();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    @Override
    public void createPartControl(Composite arg0) {
        FaultViewWidget faultViewWidget = new FaultViewWidget(arg0, SWT.NONE);
        updateUI();

    }

    private void updateUI() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setFocus() {
        // TODO Auto-generated method stub

    }

}
