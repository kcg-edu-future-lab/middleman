class Middleman{
	constructor(servicePath, room){
		var p = location.href.split("\/", 5);
		var contextUrl = (p[0] == "http:" ? "ws:" : "wss:") + "//" + p[2] + "/" + p[3];
		this.ws = new WebSocket(contextUrl + "/" + servicePath + "/" + room);
		this.handlers = [];
		this.ws.onmessage = e => this.onmessage(e);
		this.ws.onerror = e => {
			this.ws = null;
			this.onerror(e);
		};
	}

	onmessage(e){
		var msg = JSON.parse(e.data);
		if(msg.type == "invocation"){
			const f = this.handlers[msg.index];
			if(f) f.apply(null, msg.args);
			else this.prev(e.data);
		} else{
			this.onelse(msg);
		}
	}

	onelse(){
	}
	
	onerror(){
	}

	proxy(f){
		if(!this.ws) return f;
		const index = this.handlers.length;
		this.handlers.push(f);
		const self = this;
		return function(){
			if(self.ws == null){
				if(f) f.apply(null, arguments);
			} else{
				self.ws.send(JSON.stringify({
					type: "invocation",
					index: index,
					args: Array.from(arguments)
				}));
			}
		};
	}
}
