class Middleman{
	constructor(servicePath){
		var p = document.querySelector("script[src$='middleman.js']").src.split("\/", 5);
		var contextUrl = (p[0] == "http:" ? "ws:" : "wss:") + "//" + p[2] + "/" + p[3];
		this.ws = new WebSocket(contextUrl + "/" + servicePath);
		this.sharedFunctions = [];
		this.handlers = {};
		this.ws.onopen = e => this.onopen(e);
		this.ws.onclose = e => this.onclose(e);
		this.ws.onmessage = e => this.onmessage(e);
		this.ws.onerror = e => this.onerror(e);
	}

	onopen(_e){
	}

	onclose(_e){
	}

	onerror(_e){
	}

	onmessage(e){
		var msg = JSON.parse(e.data);
		if(msg.type == "invocation"){
			const f = this.sharedFunctions[msg.body.index];
			if(f) f.apply(null, msg.body.args);
			else this.onelse(msg.type, msg.body, msg.headers);
		} else if(this.handlers[msg.type]){
			this.handlers[msg.type](msg.body, msg.headers);
		} else{
			this.onelse(msg.type, msg.body, msg.headers);
		}
	}

	onelse(_type, _body, _headers){
	}

	send(type, body, headers){
		this.ws.send(JSON.stringify({
				type: type,
				headers: headers,
				body: body}));
	}

	share(f){
		if(!this.ws) return f;
		const index = this.sharedFunctions.length;
		this.sharedFunctions.push(f);
		const self = this;
		return function(){
			if(self.ws == null){
				if(f) f.apply(null, arguments);
			} else{
				self.ws.send(JSON.stringify({
					type: "invocation",
					body: {
						index: index,
						args: Array.from(arguments)
					}
				}));
			}
		};
	}
}
