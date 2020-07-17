class Middleman{
	constructor(servicePath){
		this.sharedFunctions = [];
		this.handlers = {};
		this.invocationConfigs = [];
		this.connecting = false;

		var p = document.querySelector("script[src$='middleman.js']").src.split("\/", 5);
		var contextUrl = (p[0] == "http:" ? "ws:" : "wss:") + "//" + p[2] + "/" + p[3];
		this.ws = new WebSocket(contextUrl + "/" + servicePath);
		this.ws.onopen = e => this.handleOnOpen(e);
		this.ws.onclose = e => this.handleOnClose(e);
		this.ws.onerror = e => this.handleOnError(e);
		this.ws.onmessage = e => this.handleOnMessage(e);
	}

	sendInvocationConfigs(){
		if(!this.connecting) return;
		for(const ic of this.invocationConfigs){
			this.ws.send(JSON.stringify(ic));
		}
		this.invocationConfigs = [];
	}

	handleOnOpen(e){
		this.connecting = true;
		this.sendInvocationConfigs();
		this.onopen(e);
	}

	handleOnClose(e){
		this.connecting = false;
		this.onclose(e);
		this.ws = null;
	}

	handleOnError(e){
		this.onerror(e);
	}

	handleOnMessage(e){
		var msg = JSON.parse(e.data);
		this.data(msg);
	}

	data(msg){
		if(msg.type == "bulk"){
			for(const m of msg.body){
				this.data(m);
			}
		} else if(msg.type == "invocation"){
			const f = this.sharedFunctions[msg.body.index];
			if(f) f.apply(null, msg.body.args);
			else this.onelse(msg.type, msg.body, msg.headers);
		} else if(this.handlers[msg.type]){
			this.handlers[msg.type](msg.body, msg.headers);
		} else{
			this.onelse(msg.type, msg.body, msg.headers);
		}
	}


	onopen(_e){
	}

	onclose(_e){
	}

	onerror(_e){
	}

	onelse(_type, _body, _headers){
	}

	send(type, body, headers){
		this.ws.send(JSON.stringify({
				type: type,
				headers: headers,
				body: body}));
	}

	share(f, option){
		if(!this.ws) return f;
		const index = this.sharedFunctions.length;
		this.sharedFunctions.push(f);
		console.log(option);
		if(option){
			this.invocationConfigs.push({
				type: "invocationConfig",
				body: {
					index: index,
					option: option
				}
			});
			this.sendInvocationConfigs();
		}
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
