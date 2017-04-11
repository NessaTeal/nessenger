import React, {Component} from 'react';

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
        <button className="btn btn-success" onClick={this.props.createChatroom}>
	        Proceed
	    </button>
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

export default ChooseChatroomPage;