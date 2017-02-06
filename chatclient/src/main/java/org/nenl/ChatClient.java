package org.nenl;

import java.io.IOException;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ChatClient {
	
	protected Display display;
	protected Shell shell;
	protected ConnectionHandler connectionHandler;
	protected ScrolledComposite scrollingWrapper;
	protected Composite chatContent;
	protected Text messageField;
	protected Button changeNicknameButton;
	protected Button changeChatroomButton;
	protected Button sendButton;
	protected Label chatroomNameLabel;
	
	public static void main(String[] args) {
		new ChatClient();
	}
	
	ChatClient() {
        
		initializeGUI();
		
		attachListeners();
		
		connectToServer();
		
		createMessageListenerThread();
		
        chooseNickname();
        
        chooseOrCreateChat();
		
        while(!shell.isDisposed()) {
        	if(!display.readAndDispatch()) {
        		display.sleep();
        	}
        }
	}
	
	protected void initializeGUI() {

		display = new Display();
		shell = new Shell(display);
		
		shell.setSize(450, 335);
		shell.setText("NenlMessenger");
		
		scrollingWrapper = new ScrolledComposite(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrollingWrapper.setBounds(10, 41, 414, 189);
		
		chatContent = new Composite(scrollingWrapper, SWT.NONE);
		chatContent.setLayout(new GridLayout(1, true));
		
		scrollingWrapper.setContent(chatContent);
		scrollingWrapper.setExpandHorizontal(true);
		scrollingWrapper.setExpandVertical(true);
		
		messageField = new Text(shell, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		messageField.setBounds(10, 243, 295, 44);
		
		sendButton = new Button(shell, SWT.NONE);
		sendButton.setBounds(311, 262, 113, 25);
		sendButton.setText("Send Message");
		
		changeNicknameButton = new Button(shell, SWT.NONE);
		changeNicknameButton.setBounds(192, 10, 113, 25);
		changeNicknameButton.setText("Change Nickname");
		
		changeChatroomButton = new Button(shell, SWT.NONE);
		changeChatroomButton.setBounds(311, 10, 113, 25);
		changeChatroomButton.setText("Change Chatroom");
		
		chatroomNameLabel = new Label(shell, SWT.WRAP);
		chatroomNameLabel.setBounds(10, 15, 176, 15);
		chatroomNameLabel.setText("Chatroom name");
		
        shell.open();
	}
	
	protected void attachListeners() {
		
		shell.addListener(SWT.Close, new Listener () {

			@Override
			public void handleEvent(Event event) {
				
				connectionHandler.disconnect();
			}
		});
		
		sendButton.addListener(SWT.Selection, event -> {
			connectionHandler.sendMessage(messageField.getText());
			messageField.setText("");
		});
		
		changeChatroomButton.addListener(SWT.Selection, event -> {
			chooseOrCreateChat();
		});
		
		changeNicknameButton.addListener(SWT.Selection, event -> {
			chooseNickname();
		});
		
	}
	
	protected void connectToServer() {

		connectionHandler = new ConnectionHandler();
		
		try {
			connectionHandler.connect();
		} catch(IOException e) {
			MessageBox errorConnectingDialog = new MessageBox(shell, SWT.ERROR | SWT.OK);
			errorConnectingDialog.setText("Unable to connect to server");
			errorConnectingDialog.setMessage("Check your connection and try to ping 34.248.239.43 or check your firewall blocking port 61111");
			errorConnectingDialog.open();
			
			return;
		}
	}
	
	protected void createMessageListenerThread() {

		new Thread(new Runnable() {
			public void run() {
				String line;
				
				try {
					while((line = connectionHandler.messageIn.readLine()) != null) {
						createChatMessage(line);
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	protected void createChatMessage(String line) {
		
		display.asyncExec(new Runnable() {

			@Override
			public void run() {

				Text newChatMessage;
				newChatMessage = new Text(chatContent, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
				newChatMessage.setText(line);
				
				scrollingWrapper.setMinSize(chatContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				chatContent.layout();
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
		
		if(!chatroomName.equals(chatroomNameLabel.getText())) {

			if(!chatroomNameLabel.getText().equals("Chatroom name")){
				connectionHandler.quitChatroom();
			}
			
			if(chatroomList.contains(chatroomName)) {
				connectionHandler.joinChatroom(chatroomName);
			} else {
				connectionHandler.createChatroom(chatroomName);
			}
			
			chatroomNameLabel.setText(chatroomName);
			
			for(Control control : chatContent.getChildren()) {
				control.dispose();
			}
		}
	}
}
