class Middleman{
	constructor(servicePath, options){
		this.connecting = false;
		this.connectionConfig = {
				type: "connectionConfig",
				body: options
				};
		this.objectConfigs = [];
		this.methodConfigs = [];
		this.sharedObjects = [];
		this.sharedFunctions = [];
		this.changedObjects = {};
		this.handlers = {};

		const p = document.querySelector("script[src$='middleman.js']").src.split("\/", 5);
		const contextUrl = (p[0] == "http:" ? "ws:" : "wss:") + "//" + p[2] + "/" + p[3];
		this.ws = new WebSocket(contextUrl + "/" + servicePath);
		this.ws.onopen = e => this.handleOnOpen(e);
		this.ws.onclose = e => this.handleOnClose(e);
		this.ws.onerror = e => this.handleOnError(e);
		this.ws.onmessage = e => this.handleOnMessage(e);

		setInterval(() => { this.saveState();}, 10000);
	}

	sendConfigs(){
		if(!this.connecting) return;

		if(this.connectionConfig){
			this.ws.send(JSON.stringify(this.connectionConfig));
			this.connectionConfig = null;
		}
		for(const oc of this.objectConfigs){
			this.ws.send(JSON.stringify(oc));
		}
		this.objectConfigs = [];
		for(const ic of this.methodConfigs){
			this.ws.send(JSON.stringify(ic));
		}
		this.methodConfigs = [];
	}

	handleOnOpen(e){
		this.connecting = true;
		this.sendConfigs();
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
			if(msg.body) for(const m of msg.body){
				this.data(m);
			}
		} else if(msg.type == "invocation"){
			const f = this.sharedFunctions[msg.body.index];
			if(f) f.apply(null, msg.body.args);
			else this.onelse(msg.type, msg.body, msg.headers);
		} else if(msg.type == "state"){
			const obj = this.sharedObjects[msg.body.objectIndex];
			if(obj) obj.setState(msg.body.state);
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

	share(f, option, objectIndex){
		if(!this.ws) return f;
		const index = this.sharedFunctions.length;
		this.sharedFunctions.push(f);
		if(option){
			this.methodConfigs.push({
				type: "methodConfig",
				body: {
					index: index,
					option: option
				}
			});
			this.sendConfigs();
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
						objextIndex: objectIndex,
						args: Array.from(arguments)
					}
				}));
			}
		};
	}

	shareObject(obj, methods){
		if(!this.ws) return;
		const objectIndex = this.sharedObjects.length;
		const methodIndexes = [];
		this.sharedObjects.push(obj);
		for(let m of methods){
			methodIndexes.push(this.sharedFunctions.length);
			const sm = this.share(m.bind(obj), null, objectIndex);
			const self = this;
			obj[m.name] = function(){
				self.changedObjects[objectIndex] = obj;
				sm.apply(null, arguments);
			};
		}
		this.objectConfigs.push({
			type: "objectConfig",
			body: {
				objectIndex: objectIndex,
				methodIdexes: methodIndexes
			}
		});
		this.sendConfigs();
	}

	saveState(){
		for(let i in this.changedObjects){
			this.ws.send(JSON.stringify({
				type: "saveState",
				body: {
					objectIndex: i,
					state: this.sharedObjects[i].getState()
				},
			}));
		}
		this.changedObjects = {};
	}
}
