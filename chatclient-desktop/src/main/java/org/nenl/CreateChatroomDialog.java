package org.nenl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;

public class CreateChatroomDialog extends Dialog {

	protected String newChatroomName;
	
	protected Shell shell;
	protected Display display;
	protected Text newChatroomNameField;
	protected Button createChatroomButton;
	protected Button cancelCreatingButton;
	
	CreateChatroomDialog(Shell parent) {
		super(parent);
		
        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        display = parent.getDisplay();
        
        initializeGraphics();
        
        attachListeners();
	}

	String open() {
        shell.open();
        
        while (!shell.isDisposed()) {
                if (!display.readAndDispatch()) {
                	display.sleep();
                }
        }
        
        return newChatroomName;
	}
	
	protected void initializeGraphics() {
        shell.setSize(300, 118);
		shell.setText("Create new chatroom");
        
        Label newChatroomNameLabel = new Label(shell, SWT.NONE);
        newChatroomNameLabel.setBounds(10, 10, 274, 15);
        newChatroomNameLabel.setText("New chatroom name:");
        
        newChatroomNameField = new Text(shell, SWT.BORDER);
        newChatroomNameField.setBounds(10, 31, 274, 21);
        
        createChatroomButton = new Button(shell, SWT.NONE);
        createChatroomButton.setBounds(209, 58, 75, 25);
        createChatroomButton.setText("Create");
        
        cancelCreatingButton = new Button(shell, SWT.NONE);
        cancelCreatingButton.setBounds(128, 58, 75, 25);
        cancelCreatingButton.setText("Cancel");
	}
	
	protected void attachListeners() {
		createChatroomButton.addListener(SWT.Selection, event -> {
			if(!newChatroomNameField.getText().equals("")) {
				newChatroomName = newChatroomNameField.getText();
				
				shell.dispose();
			} else {
				MessageBox chatroomNameEmptyDialog = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
    			chatroomNameEmptyDialog.setText("Chatroom name is empty");
    			chatroomNameEmptyDialog.setMessage("Chatroom name cannot be empty.");
    			chatroomNameEmptyDialog.open();
			}
		});
		
		cancelCreatingButton.addListener(SWT.Selection, event -> {
				shell.dispose();
		});
	}
}
