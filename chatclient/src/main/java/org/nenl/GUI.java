package org.nenl;

import java.io.IOException;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class GUI {
	
	private Shell shell;
	private ConnectionHandler connectionHandler;
	private ScrolledComposite scrolledComposite;
	private Composite composite;
	private Text messageText;
	private Button changeNickname;
	private Button changeChatroom;
	private Label chatroomNameLabel;
	
	GUI() {
		connectionHandler = new ConnectionHandler();

		Display display = new Display();
		shell = new Shell(display);
		
		try {
			connectionHandler.connect();
		} catch(IOException e) {
			MessageBox errorConnectingDialog = new MessageBox(shell, SWT.ERROR | SWT.OK);
			errorConnectingDialog.setText("Unable to connect to server");
			errorConnectingDialog.setMessage("Check your connection and try to ping 34.248.239.43 or check your firewall blocking port 61111");
			
			errorConnectingDialog.open();
			
			return;
		}
		
		shell.setSize(450, 335);
		shell.setText("NenlMessenger");
		
		shell.addListener(SWT.Close, new Listener () {

			@Override
			public void handleEvent(Event event) {
				
				connectionHandler.disconnect();
			}
		});
		
		scrolledComposite = new ScrolledComposite(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setBounds(10, 41, 414, 189);
		
		composite = new Composite(scrolledComposite, SWT.NONE);
		composite.setLayout(new GridLayout(1, true));
		
		scrolledComposite.setContent(composite);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		
		messageText = new Text(shell, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		messageText.setBounds(10, 243, 295, 44);
		
		Button sendButton = new Button(shell, SWT.NONE);
		sendButton.setBounds(311, 262, 113, 25);
		sendButton.setText("Send Message");
		
		changeNickname = new Button(shell, SWT.NONE);
		changeNickname.setBounds(192, 10, 113, 25);
		changeNickname.setText("Change Nickname");
		
		changeNickname.addListener(SWT.Selection, event -> {
			chooseNickname();
		});
		
		changeChatroom = new Button(shell, SWT.NONE);
		changeChatroom.setBounds(311, 10, 113, 25);
		changeChatroom.setText("Change Chatroom");
		
		changeChatroom.addListener(SWT.Selection, event -> {
			chooseOrCreateChat();
		});
		
		chatroomNameLabel = new Label(shell, SWT.WRAP);
		chatroomNameLabel.setBounds(10, 15, 176, 15);
		chatroomNameLabel.setText("Chatroom name");
		
		sendButton.addListener(SWT.Selection, event -> {
			connectionHandler.sendMessage(messageText.getText());
			messageText.setText("");
		});
        
        shell.open();
        
        chooseNickname();
        
        chooseOrCreateChat();
		
		new Thread(new Runnable() {
			public void run() {
				String line;
				
				try {
					while((line = connectionHandler.in.readLine()) != null) {
						createChatMessage(line);
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
		
        while(!shell.isDisposed()) {
        	if(!display.readAndDispatch()) {
        		display.sleep();
        	}
        }
	}
	
	protected void createChatMessage(String line) {
		
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {

				Text newChatMessage;
				newChatMessage = new Text(composite, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
				newChatMessage.setText(line);
				
				scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				composite.layout();
			}
			
		});
	}
	
	protected void chooseNickname() {

		String nickname = new ChooseNicknameDialog(shell).open();
		
		connectionHandler.setNickname(nickname);
	}
	
	protected void chooseOrCreateChat() {
		List<String> chatroomList = connectionHandler.getChatroomList();
				
		String chatroomName = new ChooseChatDialog(shell, chatroomList).open();
		
		if(chatroomList.contains(chatroomName)) {
			connectionHandler.joinChat(chatroomName);
		} else {
			connectionHandler.createChat(chatroomName);
		}
		
		chatroomNameLabel.setText(chatroomName);
	}
}
