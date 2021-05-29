
メッセージ仕様

メッセージ構造
JSON
* sender - 送信元のIDを示す．同じセッションに接続しているクライアントまたはサーバ上の管理モジュールやプラグインのIDが設定される．
* type - メッセージのタイプ．
* body - メッセージの内容．

共有の種類
* 全てのクライアントで共有する物(all writable)
 * ものに対する操作はサーバに送られた後，全クライアントに送信され，実行される．
 * 全クライアントで同じ順序で作成される．

* 特定のクライアントが占有する物(one writable others readable)
 * ものに対する操作はクライアントで実行された後，他の全クライアントに送信され，実行される．
 * 各クライアントが任意のタイミングで作成する．
 * 作成自体も他のクライアントに通知され，そのタイミングで他のクライアントでも作成される．

メソッド
* constructor(serviceId, roomId, {displayName: "name"})

* setDisplayName(name)

* sendToAll(body, type)

* sendToOthers(body, type)

* sendTo(body, type, targets...)

* close

* shareWithAll(method): proxyMethod
 * all writableの登録を行う．

* shareWithOthers(id => { return []}): proxyMethod
 * one wriateable others readableの登録を行う．

イベント
* joined(selfId, name)
 * 自分自身がセッションに参加した

* leaved
 * 自分自身がセッションから離脱した．

* participantJoined(participantId, displayName)
 * 他の参加者がセッションに参加した．

* participantLeaved(participantId, displayName)
 * 他の参加者がセッションから離脱した．

* displayNameChanged(displayName, senderId)

* message(body, type, senderId)
 * メッセージが送信された

