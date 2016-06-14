package org.csstudio.logbook.olog.property.fault;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.csstudio.apputil.ui.time.DateTimePickerDialog;
import org.csstudio.logbook.olog.property.fault.Fault.BeamLossState;
import org.csstudio.ui.util.dialogs.StringListSelectionDialog;
import org.csstudio.ui.util.widgets.MultipleSelectionCombo;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class FaultEditorWidget extends Composite {

    // general information

    private Label lblFaultId;
    private Label lblFaultID;
    private Text text;

    private Label lblArea;
    private CCombo comboArea;
    private Label lblSubSystem;
    private CCombo comboSubSystem;
    private Label lblDevice;
    private CCombo comboDevice;

    // Fault assignment and ownership
    private Label lblFault;
    private Label lblAssign;
    private CCombo comboAssign;
    private Label lblContact;
    private Text textContact;

    // Time related controls
    private Text textTimeOccoured;
    private Text textTimeCleared;
    private Button btnTimeOccoured;
    private CCombo comboBeamLossStatus;
    private Label lblNewLabel;
    private Label lblTimeCleared;
    private Button btnTimeCleared;
    private Text textBeamLossStart;
    private Button btnBeamLossTime;
    private Label lblRestored;
    private Text textBeamRestoredTime;
    private Button btnBeamRestoredTime;

    // comments
    private Text textCause;
    private Text textRepair;
    private Text textCorrectiveAction;

    // OLog
    private Group grpLogs;
    private Text textLogIds;
    private Label lblLogIds;
    private Button btnTags;
    private Label lblLogbooks;
    private Label lblTags;
    private Button btnLogbooks;
    private MultipleSelectionCombo<String> multiSelectionComboLogbook;
    private MultipleSelectionCombo<String> multiSelectionComboTag;

    // Configuration information
    private final FaultConfiguration fc;
    private final List<String> availableLogbooks;
    private final List<String> availableTags;

    public FaultEditorWidget(Composite parent, int style, FaultConfiguration fc, List<String> availableLogbooks,
            List<String> availableTags) {
        super(parent, style);
        
        this.fc = fc;
        this.availableLogbooks = availableLogbooks;
        this.availableTags = availableTags;
        setLayout(new GridLayout(1, false));
        
        composite = new Composite(this, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        gridData.minimumHeight = 600;
        gridData.minimumWidth= 400;
        composite.setLayoutData(gridData);
        
        composite.setLayout(new FormLayout());

        lblFaultId = new Label(composite, SWT.NONE);
        FormData fd_lblFaultId = new FormData();
        fd_lblFaultId.left = new FormAttachment(0, 5);
        fd_lblFaultId.top = new FormAttachment(0, 5);
        lblFaultId.setLayoutData(fd_lblFaultId);
        lblFaultId.setText("Fault Id:");

        lblFaultID = new Label(composite, SWT.NONE);
        FormData fd_lblFaultID = new FormData();
        fd_lblFaultID.top = new FormAttachment(0, 5);
        fd_lblFaultID.left = new FormAttachment(0, 75);
        fd_lblFaultID.right = new FormAttachment(100, -5);
        lblFaultID.setLayoutData(fd_lblFaultID);
        lblFaultID.setText("New Fault Report");

        lblArea = new Label(composite, SWT.NONE);
        FormData fd_lblContact_1 = new FormData();
        fd_lblContact_1.top = new FormAttachment(0, 25);
        fd_lblContact_1.left = new FormAttachment(0, 5);
        lblArea.setLayoutData(fd_lblContact_1);
        lblArea.setText("Area:");

        lblSubSystem = new Label(composite, SWT.NONE);
        FormData fd_lblSubSystem = new FormData();
        fd_lblSubSystem.top = new FormAttachment(0, 25);
        fd_lblSubSystem.left = new FormAttachment(33);
        fd_lblSubSystem.width = 75;
        lblSubSystem.setLayoutData(fd_lblSubSystem);
        lblSubSystem.setText("Sub System:");

        lblDevice = new Label(composite, SWT.NONE);
        FormData fd_lblDevice = new FormData();
        fd_lblDevice.top = new FormAttachment(0, 25);
        fd_lblDevice.left = new FormAttachment(66);
        fd_lblDevice.width = 75;
        lblDevice.setLayoutData(fd_lblDevice);
        lblDevice.setText("Device:");

        comboArea = new CCombo(composite, SWT.BORDER);
        comboArea.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                fault.setArea(comboArea.getItem(comboArea.getSelectionIndex()));
            }
        });
        FormData fd_comboArea = new FormData();
        fd_comboArea.right = new FormAttachment(lblSubSystem, -3);
        fd_comboArea.top = new FormAttachment(0, 25);
        fd_comboArea.left = new FormAttachment(0, 75);
        comboArea.setLayoutData(fd_comboArea);

        comboSubSystem = new CCombo(composite, SWT.BORDER);
        comboSubSystem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                fault.setSubsystem(comboSubSystem.getItem(comboSubSystem.getSelectionIndex()));
            }
        });
        FormData fd_comboSubSystem = new FormData();
        fd_comboSubSystem.left = new FormAttachment(lblSubSystem, 3);
        fd_comboSubSystem.right = new FormAttachment(lblDevice, -3);
        fd_comboSubSystem.top = new FormAttachment(0, 25);
        comboSubSystem.setLayoutData(fd_comboSubSystem);

        comboDevice = new CCombo(composite, SWT.BORDER);
        comboDevice.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                fault.setDevice(comboDevice.getItem(comboDevice.getSelectionIndex()));
            }
        });
        FormData fd_comboDevice = new FormData();
        fd_comboDevice.left = new FormAttachment(lblDevice, 3);
        fd_comboDevice.right = new FormAttachment(100, -5);
        fd_comboDevice.top = new FormAttachment(0, 25);
        comboDevice.setLayoutData(fd_comboDevice);

        lblFault = new Label(composite, SWT.NONE);
        FormData fd_lblFault = new FormData();
        fd_lblFault.left = new FormAttachment(0, 5);
        fd_lblFault.top = new FormAttachment(comboArea, 6);
        lblFault.setLayoutData(fd_lblFault);
        lblFault.setText("Fault:");

        text = new Text(composite, SWT.BORDER | SWT.V_SCROLL);
        text.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                fault.setDescription(text.getText());
            }
        });
        FormData fd_text = new FormData();
        fd_text.left = new FormAttachment(0, 75);
        fd_text.right = new FormAttachment(100, -5);
        fd_text.bottom = new FormAttachment(25);
        fd_text.top = new FormAttachment(comboArea, 6);
        text.setLayoutData(fd_text);

        lblAssign = new Label(composite, SWT.NONE);
        FormData fd_lblAssign = new FormData();
        fd_lblAssign.top = new FormAttachment(text, 6);
        fd_lblAssign.left = new FormAttachment(0, 5);
        lblAssign.setLayoutData(fd_lblAssign);
        lblAssign.setText("Assign:");

        lblContact = new Label(composite, SWT.NONE);
        FormData fd_lblContact = new FormData();
        fd_lblContact.bottom = new FormAttachment(0);
        fd_lblContact.right = new FormAttachment(0);
        fd_lblContact.top = new FormAttachment(0);
        fd_lblContact.left = new FormAttachment(0);
        lblContact.setLayoutData(fd_lblContact);
        FormData fd_lblContact_11 = new FormData();
        fd_lblContact_11.top = new FormAttachment(text, 6);
        fd_lblContact_11.left = new FormAttachment(50);
        lblContact.setLayoutData(fd_lblContact_11);
        lblContact.setText("Contact:");

        comboAssign = new CCombo(composite, SWT.BORDER);
        comboAssign.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String assigned = comboAssign.getItem(comboAssign.getSelectionIndex());
                fault.setAssigned(assigned);
                Optional<FaultConfiguration.Group> assignedGroup = fc.getGroups().stream()
                        .filter(new Predicate<FaultConfiguration.Group>() {
                    @Override
                    public boolean test(FaultConfiguration.Group t) {
                        return t.getName().equals(assigned);
                    }
                }).findFirst();
                assignedGroup.ifPresent(group -> {
                    fault.setContact(group.getOwner() + "<" + group.getContact() + ">");
                    textContact.setText(group.getOwner() + "<" + group.getContact() + ">");
                });

            }
        });
        FormData fd_comboAssign = new FormData();
        fd_comboAssign.right = new FormAttachment(lblContact, -5);
        fd_comboAssign.top = new FormAttachment(text, 6);
        fd_comboAssign.left = new FormAttachment(comboArea, 0, SWT.LEFT);
        comboAssign.setLayoutData(fd_comboAssign);

        textContact = new Text(composite, SWT.BORDER);
        FormData fd_textContact = new FormData();
        fd_textContact.right = new FormAttachment(100, -5);
        fd_textContact.left = new FormAttachment(lblContact, 5);
        fd_textContact.top = new FormAttachment(text, 6);
        textContact.setLayoutData(fd_textContact);
        // TODO Auto-generated constructor stub

        Group grpTimeInfo = new Group(composite, SWT.NONE);
        FormData fd_grpTimeInfo = new FormData();
        fd_grpTimeInfo.top = new FormAttachment(comboAssign);
        fd_grpTimeInfo.left = new FormAttachment(0, 1);
        fd_grpTimeInfo.right = new FormAttachment(100, -1);
        grpTimeInfo.setLayoutData(fd_grpTimeInfo);
        grpTimeInfo.setText("Time:");
        FormLayout fl_grpTimeInfo = new FormLayout();
        fl_grpTimeInfo.marginWidth = 5;
        fl_grpTimeInfo.marginHeight = 5;
        grpTimeInfo.setLayout(fl_grpTimeInfo);

        lblNewLabel = new Label(grpTimeInfo, SWT.NONE);
        FormData fd_lblNewLabel = new FormData();
        lblNewLabel.setLayoutData(fd_lblNewLabel);
        lblNewLabel.setText("Occurred:");

        btnTimeOccoured = new Button(grpTimeInfo, SWT.NONE);
        btnTimeOccoured.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Runnable openSearchDialog = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Display.getDefault().asyncExec(new Runnable() {
                                public void run() {
                                    DateTimePickerDialog dialog = new DateTimePickerDialog(getShell());
                                    dialog.setDateTime(Date.from(fault.getFaultOccuredTime() == null ? Instant.now()
                                            : fault.getFaultOccuredTime()));
                                    dialog.setBlockOnOpen(true);
                                    if (dialog.open() == IDialogConstants.OK_ID) {
                                        fault.setFaultOccuredTime(dialog.getDateTime().toInstant());
                                        textTimeOccoured.setText(dialog.getDateTime().toInstant().toString());
                                    }
                                }
                            });
                        } catch (final Exception e) {
                            Display.getDefault().asyncExec(new Runnable() {

                                @Override
                                public void run() {
                                    // errorBar.setException(e);
                                }
                            });
                        }
                    }
                };
                BusyIndicator.showWhile(Display.getDefault(), openSearchDialog);
            }
        });
        FormData fd_btnTimeOccoured = new FormData();
        fd_btnTimeOccoured.top = new FormAttachment(0, -2);
        fd_btnTimeOccoured.right = new FormAttachment(50, -5);
        fd_btnTimeOccoured.width = 25;
        btnTimeOccoured.setLayoutData(fd_btnTimeOccoured);
        btnTimeOccoured.setText("...");

        textTimeOccoured = new Text(grpTimeInfo, SWT.BORDER);
        textTimeOccoured.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
                    // TODO
                    
                }
            }
        });
        FormData fd_text_1 = new FormData();
        fd_text_1.right = new FormAttachment(btnTimeOccoured);
        fd_text_1.top = new FormAttachment(0);
        fd_text_1.left = new FormAttachment(0, 66);
        textTimeOccoured.setLayoutData(fd_text_1);

        lblTimeCleared = new Label(grpTimeInfo, SWT.NONE);
        FormData fd_lblTimeCleared = new FormData();
        fd_lblTimeCleared.top = new FormAttachment(lblNewLabel, 0, SWT.TOP);
        fd_lblTimeCleared.left = new FormAttachment(btnTimeOccoured, 6);
        lblTimeCleared.setLayoutData(fd_lblTimeCleared);
        lblTimeCleared.setText("Cleared:");

        btnTimeCleared = new Button(grpTimeInfo, SWT.NONE);
        btnTimeCleared.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Runnable openSearchDialog = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Display.getDefault().asyncExec(new Runnable() {
                                public void run() {
                                    DateTimePickerDialog dialog = new DateTimePickerDialog(getShell());
                                    dialog.setDateTime(Date.from(fault.getFaultClearedTime() == null ? Instant.now()
                                            : fault.getFaultClearedTime()));
                                    dialog.setBlockOnOpen(true);
                                    if (dialog.open() == IDialogConstants.OK_ID) {
                                        fault.setFaultClearedTime(dialog.getDateTime().toInstant());
                                        textTimeCleared.setText(dialog.getDateTime().toInstant().toString());
                                    }
                                }
                            });
                        } catch (final Exception e) {
                            Display.getDefault().asyncExec(new Runnable() {

                                @Override
                                public void run() {
                                    // errorBar.setException(e);
                                }
                            });
                        }
                    }
                };
                BusyIndicator.showWhile(Display.getDefault(), openSearchDialog);
            }
        });
        FormData fd_btnTimeCleared = new FormData();
        fd_btnTimeCleared.top = new FormAttachment(0, -2);
        fd_btnTimeCleared.right = new FormAttachment(100);
        fd_btnTimeCleared.width = 25;
        btnTimeCleared.setLayoutData(fd_btnTimeCleared);
        btnTimeCleared.setText("...");

        textTimeCleared = new Text(grpTimeInfo, SWT.BORDER);
        FormData fd_text_2 = new FormData();
        fd_text_2.left = new FormAttachment(lblTimeCleared, 4);
        fd_text_2.right = new FormAttachment(btnTimeCleared);
        fd_text_2.top = new FormAttachment(0);
        textTimeCleared.setLayoutData(fd_text_2);

        Label lblBeamlost = new Label(grpTimeInfo, SWT.NONE);
        fd_lblNewLabel.left = new FormAttachment(lblBeamlost, 0, SWT.LEFT);
        FormData fd_lblBeamlost = new FormData();
        fd_lblBeamlost.top = new FormAttachment(btnTimeOccoured, 5);
        lblBeamlost.setLayoutData(fd_lblBeamlost);
        lblBeamlost.setText("Beam Lost:");

        comboBeamLossStatus = new CCombo(grpTimeInfo, SWT.BORDER);
        comboBeamLossStatus.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String selection = comboBeamLossStatus.getItem(comboBeamLossStatus.getSelectionIndex());
                fault.setBeamLossState(BeamLossState.valueOf(selection));
                switch (selection) {
                case "True":
                    btnBeamLossTime.setEnabled(true);
                    textBeamLossStart.setEnabled(true);
                    btnBeamRestoredTime.setEnabled(true);
                    textBeamRestoredTime.setEnabled(true);
                    break;
                default:
                    btnBeamLossTime.setEnabled(false);
                    textBeamLossStart.setEnabled(false);
                    btnBeamRestoredTime.setEnabled(false);
                    textBeamRestoredTime.setEnabled(false);
                    break;
                }
            }
        });

        FormData fd_comboBeamLossStatus = new FormData();
        fd_comboBeamLossStatus.right = new FormAttachment(50, -6);
        fd_comboBeamLossStatus.top = new FormAttachment(textTimeOccoured, 6);
        fd_comboBeamLossStatus.left = new FormAttachment(textTimeOccoured, 0, SWT.LEFT);
        comboBeamLossStatus.setLayoutData(fd_comboBeamLossStatus);

        Label lblBeamLossTime = new Label(grpTimeInfo, SWT.NONE);
        lblBeamLossTime.setText("Start:");
        FormData fd_lblBeamLossTime = new FormData();
        fd_lblBeamLossTime.top = new FormAttachment(lblBeamlost, 13);
        lblBeamLossTime.setLayoutData(fd_lblBeamLossTime);

        btnBeamLossTime = new Button(grpTimeInfo, SWT.NONE);
        btnBeamLossTime.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Runnable openSearchDialog = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Display.getDefault().asyncExec(new Runnable() {
                                public void run() {
                                    DateTimePickerDialog dialog = new DateTimePickerDialog(getShell());
                                    dialog.setDateTime(Date.from(
                                            fault.getBeamlostTime() == null ? Instant.now() : fault.getBeamlostTime()));
                                    dialog.setBlockOnOpen(true);
                                    if (dialog.open() == IDialogConstants.OK_ID) {
                                        fault.setBeamlostTime(dialog.getDateTime().toInstant());
                                        textBeamLossStart.setText(dialog.getDateTime().toInstant().toString());
                                    }
                                }
                            });
                        } catch (final Exception e) {
                            Display.getDefault().asyncExec(new Runnable() {

                                @Override
                                public void run() {
                                    // errorBar.setException(e);
                                }
                            });
                        }
                    }
                };
                BusyIndicator.showWhile(Display.getDefault(), openSearchDialog);
            }
        });
        FormData fd_btnLossTimeStart1 = new FormData();
        fd_btnLossTimeStart1.top = new FormAttachment(btnTimeOccoured, 28);
        fd_btnLossTimeStart1.width = 25;
        fd_btnLossTimeStart1.right = new FormAttachment(50, -5);
        btnBeamLossTime.setLayoutData(fd_btnLossTimeStart1);
        btnBeamLossTime.setText("...");

        textBeamLossStart = new Text(grpTimeInfo, SWT.BORDER);
        FormData fd_textBeamLossStart = new FormData();
        fd_textBeamLossStart.top = new FormAttachment(textTimeOccoured, 32);
        fd_textBeamLossStart.right = new FormAttachment(btnBeamLossTime);
        fd_textBeamLossStart.left = new FormAttachment(0, 66);
        textBeamLossStart.setLayoutData(fd_textBeamLossStart);

        lblRestored = new Label(grpTimeInfo, SWT.NONE);
        FormData fd_lblRestored = new FormData();
        fd_lblRestored.left = new FormAttachment(btnBeamLossTime, 5);
        fd_lblRestored.top = new FormAttachment(lblBeamLossTime, 0, SWT.TOP);
        lblRestored.setLayoutData(fd_lblRestored);
        lblRestored.setText("Restored:");

        btnBeamRestoredTime = new Button(grpTimeInfo, SWT.NONE);
        btnBeamRestoredTime.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Runnable openSearchDialog = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Display.getDefault().asyncExec(new Runnable() {
                                public void run() {
                                    DateTimePickerDialog dialog = new DateTimePickerDialog(getShell());
                                    dialog.setDateTime(Date.from(fault.getBeamRestoredTime() == null ? Instant.now()
                                            : fault.getBeamRestoredTime()));
                                    dialog.setBlockOnOpen(true);
                                    if (dialog.open() == IDialogConstants.OK_ID) {
                                        fault.setBeamRestoredTime(dialog.getDateTime().toInstant());
                                        textBeamRestoredTime.setText(dialog.getDateTime().toInstant().toString());
                                    }
                                }
                            });
                        } catch (final Exception e) {
                            Display.getDefault().asyncExec(new Runnable() {

                                @Override
                                public void run() {
                                    // errorBar.setException(e);
                                }
                            });
                        }
                    }
                };
                BusyIndicator.showWhile(Display.getDefault(), openSearchDialog);
            }
        });
        FormData fd_btnBeamRestoredTime = new FormData();
        fd_btnBeamRestoredTime.top = new FormAttachment(btnBeamLossTime, 0, SWT.TOP);
        fd_btnBeamRestoredTime.right = new FormAttachment(100);
        fd_btnBeamRestoredTime.width = 25;
        btnBeamRestoredTime.setLayoutData(fd_btnBeamRestoredTime);
        btnBeamRestoredTime.setText("...");

        textBeamRestoredTime = new Text(grpTimeInfo, SWT.BORDER);
        FormData fd_textBeamRestoredTime = new FormData();
        fd_textBeamRestoredTime.top = new FormAttachment(textBeamLossStart, 0, SWT.TOP);
        fd_textBeamRestoredTime.right = new FormAttachment(btnBeamRestoredTime);
        fd_textBeamRestoredTime.left = new FormAttachment(lblRestored);
        textBeamRestoredTime.setLayoutData(fd_textBeamRestoredTime);

        Group grpComments = new Group(composite, SWT.NONE);
        grpComments.setText("Comments:");
        grpComments.setLayout(new FormLayout());
        FormData fd_grpComments = new FormData();
        fd_grpComments.bottom = new FormAttachment(75);
        fd_grpComments.top = new FormAttachment(grpTimeInfo, 2);
        fd_grpComments.left = new FormAttachment(0, 1);
        fd_grpComments.right = new FormAttachment(100, -1);
        grpComments.setLayoutData(fd_grpComments);

        textCause = new Text(grpComments, SWT.BORDER);
        textCause.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                fault.setRootCause(textCause.getText());
            }
        });
        FormData fd_textCause = new FormData();
        fd_textCause.bottom = new FormAttachment(33);
        fd_textCause.right = new FormAttachment(100, -6);
        fd_textCause.top = new FormAttachment(0, 5);
        fd_textCause.left = new FormAttachment(0, 74);
        textCause.setLayoutData(fd_textCause);

        textRepair = new Text(grpComments, SWT.BORDER);
        textRepair.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                fault.setRepairAction(textRepair.getText());
            }
        });
        FormData fd_textRepair = new FormData();
        fd_textRepair.bottom = new FormAttachment(66);
        fd_textRepair.top = new FormAttachment(textCause, 2);
        fd_textRepair.right = new FormAttachment(100, -6);
        fd_textRepair.left = new FormAttachment(0, 74);
        textRepair.setLayoutData(fd_textRepair);

        textCorrectiveAction = new Text(grpComments, SWT.BORDER);
        textCorrectiveAction.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                fault.setCorrectiveAction(textCorrectiveAction.getText());
            }
        });
        FormData fd_textCorrectiveAction = new FormData();
        fd_textCorrectiveAction.top = new FormAttachment(textRepair, 2);
        fd_textCorrectiveAction.bottom = new FormAttachment(100, -2);
        fd_textCorrectiveAction.right = new FormAttachment(100, -6);
        fd_textCorrectiveAction.left = new FormAttachment(0, 74);
        textCorrectiveAction.setLayoutData(fd_textCorrectiveAction);

        Label lblCause = new Label(grpComments, SWT.NONE);
        FormData fd_lblCause = new FormData();
        fd_lblCause.top = new FormAttachment(textCause, 0, SWT.TOP);
        lblCause.setLayoutData(fd_lblCause);
        lblCause.setText("Cause:");

        Label lblRepair = new Label(grpComments, SWT.NONE);
        FormData fd_lblRepair = new FormData();
        fd_lblRepair.top = new FormAttachment(textRepair, 0, SWT.TOP);
        lblRepair.setLayoutData(fd_lblRepair);
        lblRepair.setText("Repair:");

        Label lblCorrectiveAction = new Label(grpComments, SWT.NONE);
        FormData fd_lblCorrectiveAction = new FormData();
        fd_lblCorrectiveAction.top = new FormAttachment(textCorrectiveAction, 0, SWT.TOP);
        lblCorrectiveAction.setLayoutData(fd_lblCorrectiveAction);
        lblCorrectiveAction.setText("Corrective:");

        grpLogs = new Group(composite, SWT.NONE);
        grpLogs.setText("Logs:");
        grpLogs.setLayout(new FormLayout());
        FormData fd_grpLogs = new FormData();
        fd_grpLogs.bottom = new FormAttachment(grpComments, 80, SWT.BOTTOM);
        fd_grpLogs.right = new FormAttachment(100, -1);
        fd_grpLogs.top = new FormAttachment(grpComments, 2);
        fd_grpLogs.left = new FormAttachment(0, 1);
        grpLogs.setLayoutData(fd_grpLogs);

        textLogIds = new Text(grpLogs, SWT.BORDER);
        FormData fd_textLogIds = new FormData();
        fd_textLogIds.right = new FormAttachment(100, -6);
        fd_textLogIds.top = new FormAttachment(0, 5);
        fd_textLogIds.left = new FormAttachment(0, 74);
        textLogIds.setLayoutData(fd_textLogIds);

        lblLogIds = new Label(grpLogs, SWT.NONE);
        FormData fd_lblLogIds = new FormData();
        fd_lblLogIds.top = new FormAttachment(textLogIds, 0, SWT.TOP);
        fd_lblLogIds.left = new FormAttachment(0);
        lblLogIds.setLayoutData(fd_lblLogIds);
        lblLogIds.setText("Log Ids:");

        btnLogbooks = new Button(grpLogs, SWT.NONE);
        FormData fd_btnLogbooks = new FormData();
        fd_btnLogbooks.right = new FormAttachment(50);
        fd_btnLogbooks.width = 25;
        fd_btnLogbooks.top = new FormAttachment(textLogIds, 6);
        btnLogbooks.setLayoutData(fd_btnLogbooks);
        btnLogbooks.setText("...");
        btnLogbooks.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // Open a dialog which allows users to select tags
                StringListSelectionDialog dialog = new StringListSelectionDialog(parent.getShell(), availableLogbooks,
                        logbooks, "Add Logbooks");
                if (dialog.open() == IDialogConstants.OK_ID) {
                    logbooks = dialog.getSelectedValues();
                    multiSelectionComboLogbook.setSelection(dialog.getSelectedValues());
                }
            }
        });

        btnTags = new Button(grpLogs, SWT.NONE);
        FormData fd_btnTags = new FormData();
        fd_btnTags.right = new FormAttachment(100, -6);
        fd_btnTags.top = new FormAttachment(textLogIds, 6);
        fd_btnTags.width = 25;
        btnTags.setLayoutData(fd_btnTags);
        btnTags.setText("...");
        btnTags.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // Open a dialog which allows users to select tags
                StringListSelectionDialog dialog = new StringListSelectionDialog(parent.getShell(), availableTags, tags,
                        "Add Tags");
                if (dialog.open() == IDialogConstants.OK_ID) {
                    tags = dialog.getSelectedValues();
                    multiSelectionComboTag.setSelection(dialog.getSelectedValues());
                }
            }
        });

        lblLogbooks = new Label(grpLogs, SWT.NONE);
        FormData fd_lblLogbooks = new FormData();
        fd_lblLogbooks.top = new FormAttachment(textLogIds, 6);
        fd_lblLogbooks.left = new FormAttachment(0);
        lblLogbooks.setLayoutData(fd_lblLogbooks);
        lblLogbooks.setText("Logbooks:");

        lblTags = new Label(grpLogs, SWT.NONE);
        FormData fd_lblTags = new FormData();
        fd_lblTags.top = new FormAttachment(textLogIds, 6);
        fd_lblTags.left = new FormAttachment(btnLogbooks, 6);
        lblTags.setLayoutData(fd_lblTags);
        lblTags.setText("Tags:");

        multiSelectionComboLogbook = new MultipleSelectionCombo<String>(grpLogs, SWT.NONE);
        FormData fd_multiSelectionComboLogbook = new FormData();
        fd_multiSelectionComboLogbook.right = new FormAttachment(btnLogbooks, -3);
        fd_multiSelectionComboLogbook.top = new FormAttachment(textLogIds, 3);
        fd_multiSelectionComboLogbook.left = new FormAttachment(0, 74);
        multiSelectionComboLogbook.setLayoutData(fd_multiSelectionComboLogbook);
        multiSelectionComboLogbook.addPropertyChangeListener(e -> {
            if (e.getPropertyName().equals("selection")) {
                logbooks = multiSelectionComboLogbook.getSelection();
            }
        });

        multiSelectionComboTag = new MultipleSelectionCombo<String>(grpLogs, SWT.NONE);
        FormData fd_multiSelectionComboTag = new FormData();
        fd_multiSelectionComboTag.left = new FormAttachment(btnLogbooks, 74);
        fd_multiSelectionComboTag.right = new FormAttachment(btnTags, -3);
        fd_multiSelectionComboTag.top = new FormAttachment(textLogIds, 3);
        multiSelectionComboTag.setLayoutData(fd_multiSelectionComboTag);
        multiSelectionComboTag.addPropertyChangeListener(e -> {
            if (e.getPropertyName().equals("selection")) {
                tags = multiSelectionComboTag.getSelection();
            }
        });
        initialize();
    }

    /**
     * Initialize the widget with the defaults form the
     * 
     */
    private void initialize() {
        comboArea.setItems(fc.getAreas().toArray(new String[fc.getAreas().size()]));
        comboSubSystem.setItems(fc.getSubsystems().toArray(new String[fc.getSubsystems().size()]));
        comboDevice.setItems(fc.getDevices().toArray(new String[fc.getDevices().size()]));

        comboAssign.setItems(fc.getGroups().stream().map(FaultConfiguration.Group::getName).toArray(String[]::new));
        comboBeamLossStatus.setItems(
                Arrays.asList(BeamLossState.values()).stream().map(BeamLossState::toString).toArray(String[]::new));

        multiSelectionComboLogbook.setItems(availableLogbooks);
        multiSelectionComboTag.setItems(availableTags);

        updateUI();
    }

    // Model
    private Fault fault = new Fault();
    private List<String> logIds = Collections.emptyList();
    private List<String> logbooks = Collections.emptyList();
    private List<String> tags = Collections.emptyList();
    private Composite composite;

    /**
     * 
     */
    private void updateUI() {
        if (fault.getId() != 0) {
            lblFaultID.setText(String.valueOf(fault.getId()));
        } else {
            lblFaultID.setText("New Fault Report");
        }
        comboArea.setText(fault.getArea() != null ? fault.getArea() : "");
        comboSubSystem.setText(fault.getSubsystem() != null ? fault.getSubsystem() : "");
        comboDevice.setText(fault.getDevice() != null ? fault.getDevice() : "");

        text.setText(fault.getDescription() != null ? fault.getDescription() : "");

        comboAssign.setText(fault.getAssigned() != null ? fault.getAssigned() : "");
        textContact.setText(fault.getContact() != null ? fault.getContact() : "");

        textTimeOccoured.setText(fault.getFaultOccuredTime() != null ? fault.getFaultOccuredTime().toString() : "");
        textTimeCleared.setText(fault.getFaultClearedTime() != null ? fault.getFaultClearedTime().toString() : "");
        if (fault.getBeamLossState() != null) {
            comboBeamLossStatus.setText(fault.getBeamLossState().toString());
            switch (comboBeamLossStatus.getItem(comboBeamLossStatus.getSelectionIndex())) {
            case "True":
                btnBeamLossTime.setEnabled(true);
                textBeamLossStart.setEnabled(true);
                btnBeamRestoredTime.setEnabled(true);
                textBeamRestoredTime.setEnabled(true);
                break;
            default:
                btnBeamLossTime.setEnabled(false);
                textBeamLossStart.setEnabled(false);
                btnBeamRestoredTime.setEnabled(false);
                textBeamRestoredTime.setEnabled(false);
                break;
            }
        }

        textBeamLossStart.setText(fault.getBeamlostTime() != null ? fault.getBeamlostTime().toString() : "");
        textBeamRestoredTime.setText(fault.getBeamRestoredTime() != null ? fault.getBeamRestoredTime().toString() : "");

        textCause.setText(fault.getRootCause() != null ? fault.getRootCause() : "");
        textRepair.setText(fault.getRepairAction() != null ? fault.getRepairAction() : "");
        textCorrectiveAction.setText(fault.getCorrectiveAction() != null ? fault.getCorrectiveAction() : "");

        textLogIds.setText(String.join(";", logIds.stream().sorted().collect(Collectors.toList())));
        multiSelectionComboLogbook.setSelection(logbooks);
        multiSelectionComboTag.setSelection(tags);
    }

    public Fault getFault() {
        return this.fault;
    }

    public void setFault(Fault fault) {
        this.fault = fault;
        updateUI();
    }

    public List<String> getLogIds() {
        return logIds;
    }

    public void setLogIds(List<String> logIds) {
        this.logIds = logIds;
        textLogIds.setText(String.join(";", logIds.stream().sorted().collect(Collectors.toList())));
    }

    public List<String> getLogbooks() {
        return logbooks;
    }

    public void setLogbooks(List<String> logbooks) {
        this.logbooks = logbooks;
        multiSelectionComboLogbook.setSelection(this.logbooks);
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
        multiSelectionComboTag.setSelection(this.tags);
    }

}
