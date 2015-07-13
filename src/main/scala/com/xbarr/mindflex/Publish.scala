package com.xbarr.mindflex

import com.corundumstudio.socketio
import com.google.gson.Gson
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import MindflexAlpha.BrainFrame
import Constants._

object Publish {
  
  val logger = 
    LoggerFactory.getLogger(MindflexAlpha.getClass)
  val websocketServer =
          new socketio.SocketIOServer({
            val config = new socketio.Configuration
            config.setHostname("localhost")
            config.setPort(WEBSOCKET_PORT)
            config
          })
  val gson = new Gson
  var connectedToWebsocket = false
 
  def initialize =
    new Thread(new Runnable {
      def run {
        startWebsocketServer
      }
    }).start
  
  initialize
  
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
    
  def sendBrainwaves(brainWaves:BrainFrame) = {
    if(connectedToWebsocket){
      val it = websocketServer.getAllClients.iterator
      while(it.hasNext()) {
        val client = it.next
        client.sendEvent("brainwaves", gson.toJson(brainWaves))
      }
    }
    log(brainWaves)
  }
  
  def stringify(brainWaves:BrainFrame) =
    brainWaves.productIterator.map { _.asInstanceOf[Double].toString() } reduce {_+" "+_}
  
  def log(brainWaves:BrainFrame) = {
    println(brainWaves)
    logger.debug(stringify(brainWaves))
  }
}