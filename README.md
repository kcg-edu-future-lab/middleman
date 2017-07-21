# middleman
WebSocketセッションサーバ

## middlemanとは

Javaで実装されたWebSocket用汎用セッションサーバです．同じルームIDを使用しているクライアント間のメッセージ配信を行います．

## 起動方法

このリポジトリをcloneし，Eclipseにimportして，Eclipse内からWebアプリケーションとして起動して下さい．
起動後， http://localhost:8080/middleman/ にブラウザでアクセスすると，簡単なチャットサンプルが表示されます．

## 利用方法(JavaScript)

JavaScriptに標準で用意されているWebSocketクラスを使います．
```
http://host:port/middleman/rooms/{ROOMID}
```
上記URLに接続すると，同じ{ROOMID}を使用しているクライアント間でメッセージが送受信できます．
```JavaScript
var roomId = "ljrfkjsldflsjfslj";
var ws = new WebSocket("http://localhost:8080/middleman/rooms/" + roomId);
ws.onmessage = function (e) {
	console.log(e.data);
};
ws.onopen = function(){
	ws.send("hello");
};
```
