package org.nenl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
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
	
	static Font font;
	
	public static void main(String[] args) {
		new ChatClient();
	}
	
	ChatClient() {
		initializeGraphics();
		
		attachListeners();
		
		connectToServer();
		
		createMessageListenerThread();
		
		settingsWorker = new SettingsReaderWriter();
		
		settingsWorker.getSettings();
		
		if(settingsWorker.settingExist("nickname")) {
			connectionHandler.setNickname(settingsWorker.getOneSetting("nickname"));
		} else {
			chooseNickname(true);
		}
		
		String chatroomName = settingsWorker.getOneSetting("chatroomName");
		
		if(settingsWorker.settingExist("chatroomName") && connectionHandler.chatroomExists(chatroomName)) {
			connectionHandler.joinChatroom(chatroomName);
			
			chatroomNameLabel.setText(chatroomName);
		} else {
			chooseOrCreateChat(true);
		}
		
        while(!shell.isDisposed()) {
        	if(!display.readAndDispatch()) {
        		display.sleep();
        	}
        }
	}
	
	protected void initializeGraphics() {

		display = new Display();
		shell = new Shell(display, SWT.MAX | SWT.MIN);
		shell.setSize(450, 335);
		shell.setText("NenlMessenger");
		
		font = new Font(display, "Segoe UI", 9, SWT.NORMAL);
		
		scrollingWrapper = new ScrolledComposite(shell, SWT.BORDER | SWT.V_SCROLL);
		scrollingWrapper.setBounds(10, 41, 424, 189);
		
		chatContent = new Composite(scrollingWrapper, SWT.NONE);
		chatContent.setFont(font);
		chatContent.setLayout(new RowLayout(SWT.VERTICAL));
		
		scrollingWrapper.setContent(chatContent);
		scrollingWrapper.setExpandHorizontal(true);
		scrollingWrapper.setExpandVertical(true);
		scrollingWrapper.setAlwaysShowScrollBars(true);
		
		messageField = new Text(shell, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		messageField.setBounds(10, 243, 305, 44);
		messageField.setFont(font);
		
		sendButton = new Button(shell, SWT.NONE);
		sendButton.setBounds(321, 243, 113, 44);
		sendButton.setText("Send Message");
		sendButton.setFont(font);
		
		changeNicknameButton = new Button(shell, SWT.NONE);
		changeNicknameButton.setBounds(146, 10, 142, 25);
		changeNicknameButton.setText("Change Nickname");
		changeNicknameButton.setFont(font);
		
		changeChatroomButton = new Button(shell, SWT.NONE);
		changeChatroomButton.setBounds(294, 10, 140, 25);
		changeChatroomButton.setText("Change Chatroom");
		changeChatroomButton.setFont(font);
		
		chatroomNameLabel = new Label(shell, SWT.WRAP);
		chatroomNameLabel.setBounds(10, 15, 130, 15);
		chatroomNameLabel.setText("Chatroom name");
		chatroomNameLabel.setFont(font);
		
        shell.open();
        
        logger.info("GUI is initialized.");
	}
	
	protected void attachListeners() {
		
		shell.addListener(SWT.Close, new Listener () {

			@Override
			public void handleEvent(Event event) {
				
				stopListeningThread = true;
				
				connectionHandler.disconnect();
				
				shell.dispose();
				
				font.dispose();
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
		
		messageField.addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.keyCode == SWT.CR) {
					if(e.stateMask != SWT.SHIFT) {
						e.doit = false;
						connectionHandler.sendMessage(messageField.getText());
						
						messageField.setText("");
					}
				}
				
				else if (e.stateMask == SWT.CTRL && e.keyCode == 'a') {
		            messageField.selectAll();
		            e.doit = false;
		        }
			}
		});
		
		logger.info("Listeners are attached");
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
		
		logger.info("Listener thread is started");
	}
	
	protected void createChatMessage(String line) {
		
		display.syncExec(new Runnable() {

			@Override
			public void run() {

				Text newChatMessage;
				newChatMessage = new Text(chatContent, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
				newChatMessage.setText(line);
				newChatMessage.setLayoutData(new RowData(398, SWT.DEFAULT));
				newChatMessage.setEnabled(false);
				
				scrollingWrapper.setMinSize(chatContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				
				//If users currently looks at bottom or almost bottom of chat then scroll to the bottom
				
				if(chatContent.getSize().y - scrollingWrapper.getOrigin().y - scrollingWrapper.getSize().y <= 100) {
				
					scrollingWrapper.setOrigin(0, chatContent.getSize().y);
				}
				
				chatContent.layout();
			}
			
		});
		
		logger.info("New message received");
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
	
	@SuppressWarnings("unchecked")
	protected void chooseOrCreateChat(boolean firstLaunch) {
		
		List<Object> chatroomList = connectionHandler.getChatroomList();
		
		List<String> chatroomNames = new ArrayList<>();
		
		for(Object chatroomData : chatroomList) {
			chatroomNames.add(((Map<String, String>)chatroomData).get("chatroomName"));
		}
		
		logger.info("Openning choose chatroom dialog");
				
		String chatroomName = new ChooseChatroomDialog(shell, chatroomList, firstLaunch).open();
		
		if(chatroomName != null && !chatroomName.equals(chatroomNameLabel.getText())) {

			if(!firstLaunch) {
				connectionHandler.quitChatroom();
			}
			
			if(!chatroomNames.contains(chatroomName)) {
				connectionHandler.createChatroom(chatroomName);
				
				logger.info("Created chat " + chatroomName);
			} else {
				connectionHandler.joinChatroom(chatroomName);
				
				logger.info("Connected chat " + chatroomName);
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
