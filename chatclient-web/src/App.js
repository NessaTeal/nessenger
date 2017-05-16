import React, {Component} from 'react';
import {Chat} from './chat.js';
import Header from './Header.js';
import ChatroomPage from './ChatroomPage.js';
import ChooseChatroomPage from './ChooseChatroomPage.js';
import ChooseNicknamePage from './ChooseNicknamePage.js';

class App extends Component {
  constructor(props) {
    super(props);

    this.state = {
      page:'loginPage',
      next:'chooseChatroomPage',
      nickname:'Guest' + Math.floor(Math.random() * 100000),
      previousNickname:'',
      chatroomName:'',
      chatroomList:[],
      messageList:[],
      newChatroomName:'',
      nicknameWarning:false,
      scrolled:false
      };

    this.chat = new Chat((data) => this.onMessage(data));
  }

  getChatroomList() {
    this.chat.getChatroomList();
  }

  joinChatroom(chatroomName) {
    if(chatroomName !== this.state.chatroomName) {
      this.setState({messageList:[]});
      this.setState({chatroomName:chatroomName});
      this.chat.quitChatroom();
      this.chat.joinChatroom(chatroomName);
    }

    this.changePage('chatroomPage');
  }

  createChatroom() {
    if(this.state.newChatroomName.trim() !== '') {

      if(this.state.chatroomName !== '') {
      	this.chat.quitChatroom();
	  }

      this.chat.createChatroom(this.state.newChatroomName);
      this.setState({messageList:[]});
      this.changePage('chatroomPage');
    }
  }

  sendMessage(message) {
  	if(message.trim() !== '')  {
    	this.chat.sendMessage(message);
	}
  }

  changePage(next) {
    if(next === 'chooseChatroomPage') {
      this.getChatroomList();
      this.setState({page:'chooseChatroomPage', next:'chatroomPage'});
    } else if(next === 'chatroomPage') {
      this.setState({page:'chatroomPage'});
    } else if(next === 'loginPage') {
      this.setState({page:'loginPage'});
    }
  }

  onMessage(data) {
    if(data.type === 'getChatroomList') {

      data.chatroomList.map((chatroom) =>
        chatroom.onClick = (() => this.joinChatroom(chatroom.chatroomName)));

      this.setState({chatroomList:data.chatroomList});
    } else {
	    if(this.state.page === 'chatroomPage') {

			//check if user scrolled to far from bottom of the page, if yes then disable autoscroll
		    if (window.scrollY + window.innerHeight - document.body.scrollHeight < -150) {
		      this.scrolled = true;
		    } else {
		      this.scrolled = false;
		    }
		}

      let messageList = this.state.messageList;
  
      messageList.push(data.message);

      this.setState({messageList:messageList});
    }
  }

  componentDidUpdate() {
	if(!this.scrolled && this.state.page === 'chatroomPage') {
		window.scrollTo(0, document.body.scrollHeight);
	}
  }

  chooseNickname() {
 	if (this.state.nickname.trim() === '') {
      this.setState({nicknameWarning:true});
      return;
    }

    if(this.state.nickname !== this.state.previousNickname) {
      this.chat.chooseNickname(this.state.nickname);

      this.setState({nicknameWarning:false, previousNickname:this.state.nickname});

    }

    this.changePage(this.state.next);
  }

  onNicknameChange(event) {
    this.setState({nickname:event.target.value});
  }

  onNewChatroomChange(event) {
    this.setState({newChatroomName:event.target.value});
  }

  render() {
    let content;

    if(this.state.page === 'loginPage') {
      content = <ChooseNicknamePage
                  warning={this.state.nicknameWarning}
                  nickname={this.state.nickname}
                  onNicknameChange={(event) => this.onNicknameChange(event)}
                  chooseNickname={() => this.chooseNickname()}
                />

    } else if(this.state.page === 'chooseChatroomPage') {
      content = <ChooseChatroomPage
                  createChatroom={() => this.createChatroom(this.state.newChatroomName)}
                  onNewChatroomChange={(event) => this.onNewChatroomChange(event)}
                  chatroomList={this.state.chatroomList}
                />

    } else if(this.state.page === 'chatroomPage') {
      content = <ChatroomPage
                  changeNickname={() => this.changePage('loginPage')}
                  changeChatroom={() => this.changePage('chooseChatroomPage')}
                  sendMessage={(message) => this.sendMessage(message)}
                  messageList={this.state.messageList}
                  ref="chatroomPage"
                />
    }

    return (
      <div>
        <Header />
        <div className="container">
              {content}
        </div>
      </div>
    );
  }
}

export default App;