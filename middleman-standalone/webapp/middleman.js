Middleman = {
	connect: (servicePath, room) => {
		var p = location.href.split("\/", 5);
		p = ["http:", "", "127.0.0.1:8080", "middleman"];
		var contextUrl = (p[0] == "http:" ? "ws:" : "wss:") + "//" + p[2] + "/" + p[3];
		return new WebSocket(contextUrl + "/" + servicePath + "/" + room);
	}
};
