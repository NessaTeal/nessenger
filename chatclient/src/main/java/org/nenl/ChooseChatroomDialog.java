package org.nenl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class ChooseChatroomDialog extends Dialog {

	protected String chatroomName = null;
	protected java.util.List<String> chatrooms;
	protected boolean firstLaunch;
	
	protected Display display;
	protected Shell shell;
	protected Button createChatroomButton;
	protected List chatroomList;
	protected Button joinChatroomButton;
	private Button cancelButton;
	
	public ChooseChatroomDialog(Shell parent, java.util.List<String> chatrooms, boolean firstLaunch) {
		super(parent);
		this.chatrooms = chatrooms;
		this.firstLaunch = firstLaunch;
		
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
        
        chatroomList = new List(shell, SWT.BORDER | SWT.SINGLE);
        chatroomList.setBounds(10, 31, 224, 180);
        chatroomList.setItems(chatrooms.toArray(new String[]{}));
        chatroomList.setFont(ChatClient.font);
        
        joinChatroomButton = new Button(shell, SWT.NONE);
        joinChatroomButton.setBounds(164, 217, 70, 25);
        joinChatroomButton.setText("Join");
        joinChatroomButton.setFont(ChatClient.font);
        
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
        
        chatroomList.addListener(SWT.MouseDoubleClick, event -> {
        	
        	if(chatroomList.getSelectionCount() != 0) {
        	
				chatroomName = chatroomList.getSelection()[0];
				
				shell.dispose();
        	}
        });
        
        joinChatroomButton.addListener(SWT.Selection, event -> {
        	
        	if(chatroomList.getSelectionCount() != 0) {
        	
				chatroomName = chatroomList.getSelection()[0];
				
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
