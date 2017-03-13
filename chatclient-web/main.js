var socket;

var globalNickname;

var globalChatroomName;

function sendMessage() {

	var message = {};

	message.type = "message";

	message.message = $("#message").val();

	message.chatroom = globalChatroomName;

	$("#message").val("");

	socket.send(JSON.stringify(message));
}

function goToChooseChatPageFromLoginPage() {

	$("#chooseNicknamePage").css("display", "none");

	$("#chooseNicknameBtn").prop("onclick", null);

	$("#chooseNicknameBtn").click(function(e) {
		goToChatPageFromChooseNicknamePage();
	});

	globalNickname = $("#nickname").val();

	setNickname(globalNickname);

	goToChooseChatPage();
}

function goToChooseChatPageFromChatPage() {

	$("#chatPage").css("display", "none");

	goToChooseChatPage();
}

function goToChooseChatPage() {

	$("#chooseChatPage").css("display", "block");

	var message = {};

	message.type = "getChatroomList";

	socket.send(JSON.stringify(message));
}

function goToChooseNicknamePage() {

	$("#chatPage").css("display", "none");

	$("#chooseNicknamePage").css("display", "block");	
}

function chooseChatroom(chatroomName) {

	globalChatroomName = chatroomName;

	message = {};

	message.type = "joinChatroom";

	message.chatroomName = chatroomName;

	socket.send(JSON.stringify(message));

	$("#chooseChatPage").css("display", "none");

	goToChatPage();
}

function goToChatPageFromChooseNicknamePage() {

	$("#chooseNicknamePage").css("display", "none");

	globalNickname = $("#nickname").val();

	setNickname(globalNickname);

	goToChatPage();
}

function setNickname(nickname) {

	var message = {};

	message.type = "chooseNickname";

	message.nickname = nickname;

	socket.send(JSON.stringify(message));
}

function goToChatPage() {

	$("#chat").empty();	

	$("#chatPage").css("display", "block");
}

function createNewChatroom() {

		var message = {};

		message.type = "quitChatroom";

		socket.send(JSON.stringify(message));
	}

	$("#chooseChatPage").css("display", "none");

	var message = {};

	message.type = "createChatroom";

	message.chatroomName = $("#newChatroomName").val();

	socket.send(JSON.stringify(message));

	goToChatPage();
}

$(document).ready(function() {
	socket = new WebSocket("ws://nenlmessenger.tk/chatserver");

	socket.onerror = function () {

		alert("Error connecting to server, please try later.")
	}

	socket.onmessage = function (event) {

		var response = JSON.parse(event.data);

		if(response.type == "chatroomList") {
			$("#chatrooms").html("");
			
			for (var i = response.chatrooms.length - 1; i >= 0; i--) {

				var chatroomName = response.chatrooms[i].chatroomName;
				
				var oneChatroom = templates.chatroom({'chatroomID':i,'chatroomName':chatroomName,'chatroomSize':response.chatrooms[i].chatroomSize});

				$("#chatrooms").append(oneChatroom);

				$("#chatroom" + i).click(function(e){
					var newChatroomName = e.currentTarget.attributes[2].value;

					if(newChatroomName != globalChatroomName) {
						var message = {};

						message.type = "quitChatroom";

						socket.send(JSON.stringify(message));

						chooseChatroom(e.currentTarget.attributes[2].value);
					} else {

						$("#chooseChatPage").css("display", "none");

						goToChatPage();
					}

					return false;
				});
			}

		} else {

			var date;

			//Current workaround that MongoDB stores long in weird manner
			if(isNaN(response.date)) {

				var date = new Date(parseInt(response.date.$numberLong));

			} else {

				var date = new Date(response.date);

			}

			var dateString = date.getDate() + "/" + (date.getMonth() + 1) + " [" + date.getHours() + ":" + ('0' + date.getMinutes()).slice(-2) + "]";

			$("#chat").append(templates.message({'origin':response.origin,'date':dateString,'message':response.message}));

			if($(document).height() - window.scrollY - screen.height <= 300) {
				window.scrollTo(window.scrollX, $(document).height());
			}
		}
	}

	$(window).focus(function() {
		if(socket.readyState == WebSocket.CLOSED || socket.readyState == WebSocket.CLOSING) {
			socket = new WebSocket("ws://nenlmessenger.tk/chatserver");

			setNickname(globalNickname);

			joinChatroom(globalChatroomName);
		}  
	});

	$("#nickname").keydown(function(e) {
		var keyCode = e.which;

		if(keyCode == 13) {
			$("#chooseNicknameBtn").trigger("click");
		}
	});

	$("#newChatroomName").keydown(function(e) {
		var keyCode = e.which;

		if(keyCode == 13) {
			createNewChatroom();
		}
	});

	$("#message").keydown(function(e) {
		var keyCode = e.which;

		if(keyCode == 13) {
			e.preventDefault();
			sendMessage();
		}
	});
});