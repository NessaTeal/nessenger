export class Chat {
	constructor(onMessage) {
		this.socket = new WebSocket("ws://nenlmessenger.tk/chatserver");

		this.socket.onerror = function () {
			alert("Error connecting to server, please try later.")
		}

		this.socket.onmessage = function (event) {

			const response = JSON.parse(event.data);

			if(response.type === "chatroomList") {

				let chatroomList = [];

				for (let i = 0; i < response.chatrooms.length; i++) {

					chatroomList.push({'chatroomID':i,
						'chatroomName':response.chatrooms[i].chatroomName,
						'chatroomSize':response.chatrooms[i].chatroomSize});
				}

				onMessage({'type':'getChatroomList', chatroomList:chatroomList});

			} else {

				let date;

				date = new Date(response.date);

		        const dateString = ('0' + date.getDate()).slice(-2) + "/" + ('0' + (date.getMonth() + 1)).slice(-2) + " [" + date.getHours() + ":" + ('0' + date.getMinutes()).slice(-2) + "]";

		        onMessage({'type':'message',
		        	'message': {
		        	'origin':response.origin,
		        	'date':dateString,
		        	'message':response.message}});
		    }
		}
	}

	chooseNickname(nickname) {
		this.socket.send(JSON.stringify({'type':'chooseNickname','nickname':nickname}));
	}

	getChatroomList() {
		this.socket.send(JSON.stringify({'type':'getChatroomList'}));
	}

	sendMessage(message) {
		this.socket.send(JSON.stringify({'type':'message', 'message':message}));
	}

	createChatroom(chatroomName) {
		this.socket.send(JSON.stringify({'type':'createChatroom', 'chatroomName':chatroomName}));
	}

	joinChatroom(chatroomName) {
		this.socket.send(JSON.stringify({'type':'joinChatroom', 'chatroomName':chatroomName}));
	}

	quitChatroom() {
		this.socket.send(JSON.stringify({'type':'quitChatroom'}));
	}	
}