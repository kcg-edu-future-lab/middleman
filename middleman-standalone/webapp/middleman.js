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
		this.changedObjects = new Set();
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

		if(option == null) option = {};
		if(!option["keep"]){
			option["keep"] = "log";
			option["maxLog"] = 1000;
		}
		const type = option["type"];
		this.methodConfigs.push({
			type: "methodConfig",
			body: {
				index: index,
				option: option
			}
		});
		this.sendConfigs();

		this.sharedFunctions.push(f);
		const self = this;
		return function(){
			if(self.ws == null){
				if(f) f.apply(null, arguments);
			} else{
				if(type == "execAndSend") f.apply(null, arguments);
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

	shareObject(obj, methods, config){
		if(!this.ws) return;
		const objectIndex = this.sharedObjects.length;
		const methodIndexes = [];
		this.sharedObjects.push(obj);
		const hasStateMethod =
			typeof obj.getState === 'function' &&
			typeof obj.setState === 'function';
		for(let m of methods){
			methodIndexes.push(this.sharedFunctions.length);
			const mcExist = config && config["methods"] && config["methods"][m.name];
			const sm = this.share(m.bind(obj), mcExist ? config["methods"][m.name] : null,
				objectIndex);
			const self = this;
			obj[m.name] = function(){
				if(hasStateMethod) self.changedObjects.add(objectIndex);
				sm.apply(null, arguments);
			};
		}
		this.objectConfigs.push({
			type: "objectConfig",
			body: {
				objectIndex: objectIndex,
				methodIdexes: methodIndexes,
				config: config
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
		this.changedObjects.clear();
	}
}
