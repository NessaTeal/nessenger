import React, {Component} from 'react';

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

export default ChatroomPage;