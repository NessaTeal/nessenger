package org.nenl;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class ChooseChatroomDialog extends Dialog {

	protected String chatroomName;
	protected java.util.List<Object> chatrooms;
	protected boolean firstLaunch;
	protected java.util.List<String> chatroomList;
	
	protected Display display;
	protected Shell shell;
	protected Button createChatroomButton;
	protected Button joinChatroomButton;
	private Button cancelButton;
	private Table chatroomTable;
	
	public ChooseChatroomDialog(Shell parent, java.util.List<Object> chatrooms, boolean firstLaunch) {
		super(parent);
		this.chatrooms = chatrooms;
		this.firstLaunch = firstLaunch;
		
		chatroomList = new ArrayList<>();
		
        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        display = parent.getDisplay();
        
		initializeGraphics();
		
		attachListeners();
	}
	
	public String open() {
        shell.open();
        
        while (!shell.isDisposed()) {
                if (!display.readAndDispatch()) {
                	display.sleep();
                }
        }
        
        return chatroomName;
	}
	
	@SuppressWarnings("unchecked")
	protected void initializeGraphics() {
        shell.setSize(250, 275);
        shell.setText("Choose chat to join");
		
		Label lblChooseYourChatroom = new Label(shell, SWT.NONE);
        lblChooseYourChatroom.setBounds(10, 10, 224, 15);
        lblChooseYourChatroom.setText("Choose your chatroom:");
        lblChooseYourChatroom.setFont(ChatClient.font);
        
        createChatroomButton = new Button(shell, SWT.NONE);
        createChatroomButton.setBounds(87, 217, 70, 25);
        createChatroomButton.setText("Create new");
        createChatroomButton.setFont(ChatClient.font);
        
        joinChatroomButton = new Button(shell, SWT.NONE);
        joinChatroomButton.setBounds(164, 217, 70, 25);
        joinChatroomButton.setText("Join");
        joinChatroomButton.setFont(ChatClient.font);
        
        chatroomTable = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
        chatroomTable.setBounds(10, 31, 224, 180);
        chatroomTable.setHeaderVisible(true);
        chatroomTable.setLinesVisible(false);
        chatroomTable.setFont(ChatClient.font);
        
        TableColumn chatroomNameTableColumn = new TableColumn(chatroomTable, SWT.NONE);
        chatroomNameTableColumn.setResizable(false);
        chatroomNameTableColumn.setWidth(130);
        chatroomNameTableColumn.setText("Chatroom name");
        
        TableColumn chatroomSizeTableColumn = new TableColumn(chatroomTable, SWT.NONE);
        chatroomSizeTableColumn.setResizable(false);
        chatroomSizeTableColumn.setWidth(70);
        chatroomSizeTableColumn.setText("Users");
        chatroomSizeTableColumn.setAlignment(SWT.RIGHT);
        
        for(Object oneChatroomMap : chatrooms) {
        	String [] tableRowValues = new String[2];
        	
        	tableRowValues[0] = ((Map<String, Object>)oneChatroomMap).get("chatroomName").toString();
        	tableRowValues[1] = ((Map<String, Object>)oneChatroomMap).get("chatroomSize").toString();
            
        	chatroomList.add(tableRowValues[0]);
        	
            TableItem chatroomTableItem = new TableItem(chatroomTable, SWT.NONE);
            chatroomTableItem.setFont(ChatClient.font);
            chatroomTableItem.setText(tableRowValues);
        }
        
        if(!firstLaunch) {
	        cancelButton = new Button(shell, SWT.NONE);
	        cancelButton.setBounds(10, 217, 70, 25);
	        cancelButton.setText("Cancel");
	        cancelButton.setFont(ChatClient.font);
        }
	}
	
	protected void attachListeners() {

        shell.addListener(SWT.Close, event -> {
        	if(firstLaunch) {
        		event.doit = false;
        		
        		MessageBox noChatroomChosenDialog = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
    			noChatroomChosenDialog.setText("Chatroom required");
    			noChatroomChosenDialog.setMessage("Please select the chatroom.");
    			noChatroomChosenDialog.open();
        	}
        });
        
        createChatroomButton.addListener(SWT.Selection, event -> {
        	
        	String createdChatroomName = new CreateChatroomDialog(shell).open(); 
        	
        	if(createdChatroomName != null) {
	        	chatroomName = createdChatroomName;
	        	
				shell.dispose();
        	}
        });
        
        chatroomTable.addListener(SWT.MouseDoubleClick, event -> {
        	if(chatroomTable.getSelectionCount() != 0) {
            	
				chatroomName = chatroomList.get(chatroomTable.getSelectionIndex());
				
				shell.dispose();
        	} else {
        		MessageBox noChatroomChosenDialog = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
    			noChatroomChosenDialog.setText("No chatroom is selected");
    			noChatroomChosenDialog.setMessage("Please choose the chatroom.");
    			noChatroomChosenDialog.open();
        	}
        });
        
        chatroomTable.addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.keyCode == SWT.CR) {
					if(chatroomTable.getSelectionCount() != 0) {
		            	
						chatroomName = chatroomList.get(chatroomTable.getSelectionIndex());
						
						shell.dispose();
		        	} else {
		        		MessageBox noChatroomChosenDialog = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
		    			noChatroomChosenDialog.setText("No chatroom is selected");
		    			noChatroomChosenDialog.setMessage("Please choose the chatroom.");
		    			noChatroomChosenDialog.open();
		        	}
				}
			}
        });
        
        joinChatroomButton.addListener(SWT.Selection, event -> {
        	
        	if(chatroomTable.getSelectionCount() != 0) {
        	
				chatroomName = chatroomList.get(chatroomTable.getSelectionIndex());
				
				shell.dispose();
        	} else {
        		MessageBox noChatroomChosenDialog = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
    			noChatroomChosenDialog.setText("No chatroom is selected");
    			noChatroomChosenDialog.setMessage("Please choose the chatroom.");
    			noChatroomChosenDialog.open();
        	}
        });
        
        if(!firstLaunch) {
        	cancelButton.addListener(SWT.Selection, event -> {
        		shell.dispose();
        	});
        }
	}
}
