package com.xbarr.mindflex


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import Constants._
import MindflexAlpha.BrainWaves
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

object Publish {
  
  private lazy val logger = 
    LoggerFactory.getLogger(MindflexAlpha.getClass)
  if(WEBSOCKET_PORT != 0) WebSocket.start

  def publish(brainWaves:Seq[Double],deltas:Seq[Double],window:Seq[Double]) = {
    if(AWS_CONNECTED)
      logger.info(brainWaves.stringify)
    if(WebSocket.connectedToWebsocket)
      WebSocket.push(deltas)
    Feedback.update(window)
    println(brainWaves)
  }
}

object WebSocket {
  
  import com.corundumstudio.socketio
  import com.google.gson.Gson
  
  private lazy val gson = new Gson
  var connectedToWebsocket = false
  private lazy val websocketServer =
          new socketio.SocketIOServer({
            val config = new socketio.Configuration
            config.setHostname("localhost")
            config.setPort(WEBSOCKET_PORT)
            config
          })
  
  // TODO remove BrainFrame objects in javascript and just use Array with indexes
  case class BrainFrame(brainWaves:Seq[Double]){
    val attention = brainWaves.attention
    val meditation = brainWaves.meditation
    val theta = brainWaves.theta
    val delta = brainWaves.delta
    val lowAlpha = brainWaves.lowAlpha
    val highAlpha = brainWaves.highAlpha
    val lowBeta = brainWaves.lowBeta
    val highBeta = brainWaves.highBeta
    val lowGamma = brainWaves.lowGamma
    val highGamma = brainWaves.highGamma
  }
  
  def start =
    Future {startWebsocketServer}
     
  def push(deltas:Seq[Double]) = 
    if(connectedToWebsocket){
      val it = websocketServer.getAllClients.iterator
      while(it.hasNext()) {
        val client = it.next
        client.sendEvent("brainwaves", gson.toJson(BrainFrame(deltas)))
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

