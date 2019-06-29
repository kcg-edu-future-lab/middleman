Middleman = {
	connect: (servicePath, room) => {
		var p = location.href.split("\/", 5);
		var contextUrl = (p[0] == "http:" ? "ws:" : "wss:") + "//" + p[2] + "/" + p[3];
		return new WebSocket(contextUrl + "/" + servicePath + "/" + room);
	}
};
