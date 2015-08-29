package com.xbarr.mindflex


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import Constants._

object Publish {
  
  lazy val logger = 
    LoggerFactory.getLogger(MindflexAlpha.getClass)
  if(WEBSOCKET_PORT != 0) WebSocket.start

  def log(brainWaves:List[Double],deltas:List[Double]) = {
    if(AWS_CONNECTED)
      logger.info(stringify(brainWaves))
    if(WebSocket.connectedToWebsocket)
      WebSocket.push(deltas)
    println(brainWaves)
  }
  
  def stringify(brainWaves:List[Double]) =
    brainWaves map { _.toString } reduce {_+" "+_}
}

object WebSocket { //FIXME connection refused
  
  import com.corundumstudio.socketio
  import com.google.gson.Gson
  
  lazy val gson = new Gson
  var connectedToWebsocket = false
  lazy val websocketServer =
          new socketio.SocketIOServer({
            val config = new socketio.Configuration
            config.setHostname("localhost")
            config.setPort(WEBSOCKET_PORT)
            config
          })
  
  case class BrainFrame(attention: Double, meditation: Double,
               delta: Double, theta: Double, lowAlpha: Double, highAlpha: Double, lowBeta: Double,
               highBeta: Double, lowGamma: Double, highGamma: Double)
  
  def start =
    new Thread(new Runnable {
      def run {
        startWebsocketServer
      }
    }).start
  
 def brainFrame(cols: List[Double]) = 
   BrainFrame(cols(0), cols(1), cols(2), cols(3),
     cols(4), cols(5), cols(6), cols(7), cols(8), cols(9))
     
  def push(deltas:List[Double]) = 
    if(connectedToWebsocket){
      val it = websocketServer.getAllClients.iterator
      while(it.hasNext()) {
        val client = it.next
        client.sendEvent("brainwaves", gson.toJson(brainFrame(deltas)))
      }
    }
  
  def startWebsocketServer = {
    websocketServer.addConnectListener(new socketio.listener.ConnectListener(){
      def onConnect(client:socketio.SocketIOClient){
        connectedToWebsocket = true
      }
    })
    websocketServer.addDisconnectListener(new socketio.listener.DisconnectListener(){
      def onDisconnect(client:socketio.SocketIOClient){
        if(websocketServer.getAllClients().size() == 0)
          connectedToWebsocket = false
      }
    })
    var serverStarted = false
    println("starting websocket server")
    while(!serverStarted) {
      try{
        websocketServer.start
        serverStarted = true
      } catch {
        case e:java.net.BindException => {
          Thread.sleep(500)
        }
      }
    }
    println("started websocket server")
  }
  
}

