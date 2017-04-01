import React, { Component } from 'react';
import './bootstrap.min.css';
import {Chat} from './chat.js';

class Header extends Component {
  render() {
    return (
      <div className="page-header">
        <h1 className="text-center">
          Nenl Messenger
        </h1>
      </div>
    );
  }
}

class ChooseNicknamePage extends Component {

  onKeyUp(event) {
    if(event.keyCode === 13) {
      this.props.chooseNickname();
    }
  }

  render() {

    let warning;

    if(this.props.warning) {
      warning = <WarningPanel />
    }

    return (
      <div>
        {warning}
        <div className="form-group">
          <label>Enter your nickname:</label>
          <input type="text" className="form-control" value={this.props.nickname} onChange={this.props.onNicknameChange} onKeyUp={(event) => this.onKeyUp(event)} />
        </div>
        <ButtonSuccess onClick={this.props.chooseNickname} />
      </div>
    )
  }
}

class ButtonSuccess extends Component {
  render() {
    return (
      <button className="btn btn-success" onClick={this.props.onClick}>
        Proceed
      </button>
    )
  }
}

class WarningPanel extends Component {
  render() {
    return (
      <div className="alert alert-info" role="alert">
        <strong>Empty nickname.</strong> Please, enter any non-empty nickname.
      </div>
    )
  }
}

class ChooseChatroomPage extends Component {

  onKeyUp(event) {
    if(event.keyCode === 13) {
      this.props.createChatroom();
    }
  }

  render() {

    let chatrooms = this.props.chatroomList.map((chatroomData, i) =>
      <OneChatroom chatroomData={chatroomData} key={i} />
    );

    return (
      <div>
        <label>Choose your chatroom:</label>
        <div className="list-group" id="chatrooms">
          {chatrooms}
        </div>
        <div className="form-group">
          <label>Or create new one:</label>
          <input type="text" className="form-control" value={this.props.newChatroomName} onChange={this.props.onNewChatroomChange} onKeyUp={(event) => this.onKeyUp(event)} />
        </div>
        <ButtonSuccess onClick={this.props.createChatroom} />
      </div>
    )
  }
}

class OneChatroom extends Component {

  render() {
    return (
      <a className="list-group-item list-group-item-action flex-column align-items-start" onClick={this.props.chatroomData.onClick}>
        <div className="d-flex w-100 justify-content-between">
          <h5 className="mb-1">
            {this.props.chatroomData.chatroomName}
          </h5>
          <small>
            {this.props.chatroomData.chatroomSize}
          </small>
        </div>
      </a>
    )
  }
}

class ChatroomPage extends Component {
  constructor(props) {
    super(props);

    this.state = {
      message:''
    }
  }

  onMessageChange(event) {
    this.setState({message:event.target.value});
  }

  sendMessage() {
    if(this.state.message.length) {
      this.props.sendMessage(this.state.message);

      this.setState({message:''});
    }
  }

  onKeyUp(event) {
    if(event.keyCode === 13) {
      this.sendMessage();
    }
  }

  render() {

    let messages = this.props.messageList.map((messageData, i) =>
      <OneMessage messageData={messageData} key={i} />
    );

    return (
      <div>
        <div className="btn-group w-100">
          <button className="btn btn-success w-100" onClick={this.props.changeNickname}>
            Change nickname
          </button>
          <button className="btn btn-warning w-100" onClick={this.props.changeChatroom}>
            Change chatroom
          </button>
        </div>
        <div className="list-group" id="chat">
          {messages}
        </div>
        <div className="form-group">
          <label>
            Type your message here:
          </label>
          <input className="form-control" rows="5" value={this.state.message} onChange={(event) => this.onMessageChange(event)} onKeyUp={(event) => this.onKeyUp(event)}/>
        </div>
        <button className="btn btn-primary" onClick={() => this.sendMessage()}>
          Send Message
        </button>
      </div>
    )
  }
}

class OneMessage extends Component {
  render() {
    return (
      <div className="list-group-item list-group-item-action flex-column align-items-start">
        <div className="d-flex w-100 justify-content-between">
          <h5 className="mb-1">
            {this.props.messageData.origin}
          </h5>
          <small>
            {this.props.messageData.date}
          </small>
        </div>
        <p className="mb-1">
          {this.props.messageData.message}
        </p>
      </div>
    )
  }
}

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
      nicknameWarning:false
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
    if(this.state.newChatroomName !== '') {
      this.chat.quitChatroom();
      this.chat.createChatroom(this.state.newChatroomName);
      this.setState({messageList:[]});
      this.changePage('chatroomPage');
    }
  }

  sendMessage(message) {
    this.chat.sendMessage(message);
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

      let messageList = this.state.messageList;
  
      messageList.push(data.message);

      this.setState({messageList:messageList});
    }
  }

  chooseNickname() {

    if(this.state.nickname !== this.state.previousNickname) {
      this.chat.chooseNickname(this.state.nickname);

      this.setState({nicknameWarning:false, previousNickname:this.state.nickname});

    } else if (this.state.nickname === '') {
      this.setState({nicknameWarning:true});
      return;
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
                />
    }

    return (
      <div>
        <Header />
        <div className="container">
          <div className="row">
            <div className="col-md-12">
              {content}
            </div>
          </div>
        </div>
      </div>
    );
  }
}

export default App;
