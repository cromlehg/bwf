function initChat(url, receiveMsgCallback, statusCallback, sendMsgCallback, sendingDisabled, sendingEnabled) {

	sendingDisabled();

	if ("WebSocket" in window) {
		console.log("WebSocket is supported by your Browser!");
	} else {
		console.log("WebSocket NOT supported by your Browser!");
		return;
	}

	var connection = new WebSocket(url);

	connection.onopen = function () {
		statusCallback("connected");
		sendMsgCallback(function (text) {
			connection.send(JSON.stringify({msg: text}));
		});
		sendingEnabled();
	};

	connection.onerror = function (error) {
		console.log('WebSocket Error ', error);
	};

	connection.onmessage = function (event) {
		var ncMsg = JSON.parse(event.data);
		receiveMsgCallback(ncMsg.msg, ncMsg.login, ncMsg.timestamp);
	}

	console.log("chat app is running!");

};
