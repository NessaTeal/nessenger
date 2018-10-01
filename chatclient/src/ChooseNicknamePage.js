import React, {Component} from 'react';

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
      <div className="row">
        <div className="col-md-12">
          {warning}
          <div className="form-group">
            <label>Enter your nickname:</label>
            <input type="text" className="form-control" value={this.props.nickname} onChange={this.props.onNicknameChange} onKeyUp={(event) => this.onKeyUp(event)} />
          </div>
          <button className="btn btn-success" onClick={this.props.chooseNickname}>
            Proceed
          </button>
        </div>
      </div>
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

export default ChooseNicknamePage;