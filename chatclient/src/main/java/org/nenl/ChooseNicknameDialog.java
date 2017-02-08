package org.nenl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Font;
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

	public ChooseNicknameDialog(Shell parent, boolean firstLaunch) {
		super(parent);
		this.firstLaunch = firstLaunch;
	}
	
	public String open () {
        Shell parent = getParent();
        Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        shell.setSize(250, 119);
        shell.setText("Choose your nickname");
        
        shell.addListener(SWT.Close, event -> {
        	if(firstLaunch) {
        		event.doit = false;
        		
        		MessageBox noNicknameChosenDialog = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
    			noNicknameChosenDialog.setText("Nickname required");
    			noNicknameChosenDialog.setMessage("You should choose nickname.");
    			noNicknameChosenDialog.open();
        	}
        });
        
        Label lblChooseYourNickname = new Label(shell, SWT.NONE);
        lblChooseYourNickname.setBounds(10, 10, 224, 15);
        lblChooseYourNickname.setText("Choose your nickname:");

    	Text nicknameField = new Text(shell, SWT.BORDER);
        nicknameField.setBounds(10, 31, 224, 21);
		nicknameField.setFont(new Font(parent.getDisplay(), "UTF-8", 9, SWT.NORMAL));
        
        Button chooseNickname = new Button(shell, SWT.NONE);
        chooseNickname.setBounds(159, 56, 75, 25);
        chooseNickname.setText("Proceed");
        
        chooseNickname.addListener(SWT.Selection, event -> {

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
        
        shell.open();
        Display display = parent.getDisplay();
        while (!shell.isDisposed()) {
                if (!display.readAndDispatch()) {
                	display.sleep();
                }
        }
        
        return nickname;
	}
}
