package org.nenl;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Font;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatClient {
	
	private static Logger logger = LoggerFactory.getLogger(ChatClient.class);
	
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
	protected SettingsReaderWriter settingsWorker;
	protected volatile boolean stopListeningThread = false;
	
	public static void main(String[] args) {
		new ChatClient();
	}
	
	ChatClient() {
		initializeGUI();
		
		attachListeners();
		
		connectToServer();
		
		createMessageListenerThread();
		
		settingsWorker = new SettingsReaderWriter();
		
		if(settingsWorker.settingsExist()) {
			settingsWorker.getSettings();
			
			if(settingsWorker.settingExist("nickname")) {
				connectionHandler.setNickname(settingsWorker.getOneSetting("nickname"));
			}
			
			if(settingsWorker.settingExist("chatroomName")) {
				connectionHandler.joinChatroom(settingsWorker.getOneSetting("chatroomName"));
			}
		} else {
	        chooseNickname(true);
	        
	        chooseOrCreateChat(true);
		}
		
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
		messageField.setFont(new Font(display, "UTF-8", 9, SWT.NORMAL));
		
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
        
        logger.info("GUI is initialized.");
	}
	
	protected void attachListeners() {
		
		shell.addListener(SWT.Close, new Listener () {

			@Override
			public void handleEvent(Event event) {
				
				stopListeningThread = true;
				
				connectionHandler.disconnect();
			}
		});
		
		sendButton.addListener(SWT.Selection, event -> {
			connectionHandler.sendMessage(messageField.getText());
			messageField.setText("");
		});
		
		changeChatroomButton.addListener(SWT.Selection, event -> {
			chooseOrCreateChat(false);
		});
		
		changeNicknameButton.addListener(SWT.Selection, event -> {
			chooseNickname(false);
		});
		
		messageField.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.keyCode == SWT.CR) {
					e.doit = false;
					connectionHandler.sendMessage(messageField.getText());
					
					messageField.setText("");
				}
			}
		});
		
		logger.info("Listeners are attached.");
	}
	
	protected void connectToServer() {

		connectionHandler = new ConnectionHandler();
		
		try {
			connectionHandler.connect();
		} catch(IOException e) {
			MessageBox errorConnectingDialog = new MessageBox(shell, SWT.ERROR | SWT.OK);
			errorConnectingDialog.setText("Unable to connect to server");
			errorConnectingDialog.setMessage("Check your connection and try to ping 34.248.239.43 or check your firewall blocking port 61111, for further information refer to logs.");
			errorConnectingDialog.open();
			
			logger.error(e.getMessage());
			
			System.exit(0);
		}
	}
	
	protected void createMessageListenerThread() {

		new Thread(new Runnable() {
			public void run() {
				
				try {
					while(!stopListeningThread){
						
						if(!connectionHandler.messageIn.ready()) {
							TimeUnit.MILLISECONDS.sleep(20);
							continue;
						}
						
						createChatMessage(connectionHandler.messageIn.readLine());
					}
				}
				catch(Exception e) {
					logger.error(e.getMessage());
				}
			}
		}, "listener").start();
		
		logger.info("Listener thread is started.");
	}
	
	protected void createChatMessage(String line) {
		
		display.syncExec(new Runnable() {

			@Override
			public void run() {

				Text newChatMessage;
				newChatMessage = new Text(chatContent, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
				newChatMessage.setText(line);
				
				scrollingWrapper.setMinSize(chatContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				
				//If users currently looks at bottom of chat then scroll to the bottom
				
				if(chatContent.getSize().y - scrollingWrapper.getOrigin().y - scrollingWrapper.getSize().y <= 100) {
				
					scrollingWrapper.setOrigin(0, chatContent.getSize().y);
				}
				
				chatContent.layout();
			}
			
		});
		
		logger.info("New message received.");
	}
	
	protected void chooseNickname(boolean firstLaunch) {
		
		logger.info("Openning choose nickname dialog");

		String nickname = new ChooseNicknameDialog(shell, firstLaunch).open();
		
		if(nickname != null) {
			settingsWorker.setOneSetting("nickname", nickname);
			
			connectionHandler.setNickname(nickname);
			
			logger.info("Nickname is set to " + nickname);
		} else {
			logger.info("User decided to not change nickname");
		}
	}
	
	protected void chooseOrCreateChat(boolean firstLaunch) {
		
		List<String> chatroomList = connectionHandler.getChatroomList();
		
		logger.info("Openning choose chatroom dialog");
				
		String chatroomName = new ChooseChatroomDialog(shell, chatroomList, firstLaunch).open();
		
		if(chatroomName != null && !chatroomName.equals(chatroomNameLabel.getText())) {

			if(!firstLaunch) {
				connectionHandler.quitChatroom();
			}
			
			if(chatroomList.contains(chatroomName)) {
				connectionHandler.joinChatroom(chatroomName);
				
				logger.info("Connected to chat " + chatroomName);
			} else {
				connectionHandler.createChatroom(chatroomName);
				
				logger.info("Created chat " + chatroomName);
			}
			
			settingsWorker.setOneSetting("chatroomName", chatroomName);
			
			chatroomNameLabel.setText(chatroomName);
			
			for(Control control : chatContent.getChildren()) {
				control.dispose();
			}
		} else {
			logger.info("User didn't change chatroom");
		}
	}
}
