package org.nenl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ChooseChatroomDialog extends Dialog {

	protected String chatroomName = null;
	protected java.util.List<String> chatrooms;
	protected boolean firstLaunch;
	
	public ChooseChatroomDialog(Shell parent, java.util.List<String> chatrooms, boolean firstLaunch) {
		super(parent);
		this.chatrooms = chatrooms;
		this.firstLaunch = firstLaunch;
	}
	
	public String open() {
		Shell parent = getParent();
        Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        shell.setSize(250, 334);
        shell.setText("Choose chat to join");
        
        shell.addListener(SWT.Close, event -> {
        	if(firstLaunch) {
        		event.doit = false;
        		
        		MessageBox noChatroomChosenDialog = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
    			noChatroomChosenDialog.setText("Chatroom required");
    			noChatroomChosenDialog.setMessage("You should choose chatroom.");
    			noChatroomChosenDialog.open();
        	}
        });
        
        Label lblChooseYourChatroom = new Label(shell, SWT.NONE);
        lblChooseYourChatroom.setBounds(10, 10, 224, 15);
        lblChooseYourChatroom.setText("Double click to choose your chatroom:");
        
        Button createChatroom = new Button(shell, SWT.NONE);
        createChatroom.setBounds(159, 271, 75, 25);
        createChatroom.setText("Create");
        
        List chatroomList = new List(shell, SWT.BORDER);
        chatroomList.setBounds(10, 31, 224, 205);
        chatroomList.setItems(chatrooms.toArray(new String[]{}));
        
        Label lblOrCreateNew = new Label(shell, SWT.NONE);
        lblOrCreateNew.setBounds(10, 242, 82, 15);
        lblOrCreateNew.setText("Or create new:");

    	Text newChatroomName = new Text(shell, SWT.BORDER);
        newChatroomName.setBounds(98, 242, 136, 21);
        newChatroomName.setFont(new Font(parent.getDisplay(), "UTF-8", 9, SWT.NORMAL));
        
        createChatroom.addListener(SWT.Selection, event -> {

			if(!newChatroomName.getText().equals("")) {
				chatroomName = newChatroomName.getText();
				
				shell.dispose();
			}
        });
        
        chatroomList.addListener(SWT.MouseDoubleClick, event -> {
        	
        	if(chatroomList.getSelectionCount() == 1) {
        	
				chatroomName = chatroomList.getSelection()[0];
				
				shell.dispose();
        	}
        });
        
        shell.open();
        
        Display display = parent.getDisplay();
        while (!shell.isDisposed()) {
                if (!display.readAndDispatch()) {
                	display.sleep();
                }
        }
        
        return chatroomName;
	}
}
