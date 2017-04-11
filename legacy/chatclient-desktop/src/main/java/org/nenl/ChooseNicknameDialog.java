package org.nenl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ChooseNicknameDialog extends Dialog {
	
	protected String nickname = null;
	protected boolean firstLaunch;
	
	protected Shell shell;
    protected Display display;
	protected Text nicknameField;
	protected Button chooseNicknameButton;
	private Button btnCancel;

	public ChooseNicknameDialog(Shell parent, boolean firstLaunch) {
		super(parent);
		this.firstLaunch = firstLaunch;
		
        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        display = parent.getDisplay();
        
		initializeGraphics();
		
		attachListeners();
	}
	
	public String open () {
		shell.open();
        
        while (!shell.isDisposed()) {
                if (!display.readAndDispatch()) {
                	display.sleep();
                }
        }
        
        return nickname;
	}
	
	protected void initializeGraphics() {
        shell.setSize(188, 119);
        shell.setText("Choose your nickname");
        
        Label chooseYourNicknameLabel = new Label(shell, SWT.NONE);
        chooseYourNicknameLabel.setBounds(10, 10, 162, 15);
        chooseYourNicknameLabel.setText("Choose your nickname:");
    	chooseYourNicknameLabel.setFont(ChatClient.font);

    	nicknameField = new Text(shell, SWT.BORDER);
        nicknameField.setBounds(10, 31, 163, 21);
    	nicknameField.setFont(ChatClient.font);
        
        chooseNicknameButton = new Button(shell, SWT.NONE);
        chooseNicknameButton.setBounds(98, 56, 75, 25);
        chooseNicknameButton.setText("Choose");
    	chooseNicknameButton.setFont(ChatClient.font);
    	
    	if(!firstLaunch) {
	    	btnCancel = new Button(shell, SWT.NONE);
	    	btnCancel.setBounds(10, 56, 75, 25);
	    	btnCancel.setText("Cancel");
    	}
	}
	
	protected void attachListeners() {
        shell.addListener(SWT.Close, event -> {
        	if(firstLaunch) {
        		event.doit = false;
        		
        		MessageBox noNicknameChosenDialog = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
    			noNicknameChosenDialog.setText("Nickname required");
    			noNicknameChosenDialog.setMessage("You should choose nickname.");
    			noNicknameChosenDialog.open();
        	}
        });
        
        chooseNicknameButton.addListener(SWT.Selection, event -> {

			nickname = nicknameField.getText();
			
			shell.dispose();
        	
        });
        
        nicknameField.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.keyCode == SWT.CR) {
					nickname = nicknameField.getText();
					
					shell.dispose();
				}
			}
		});
        
        if(!firstLaunch) {
        	btnCancel.addListener(SWT.Selection, event -> {
        		shell.dispose();
        	});
        }
	}
}
