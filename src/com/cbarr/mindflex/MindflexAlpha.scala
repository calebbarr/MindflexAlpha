package com.cbarr.mindflex

import org.apache.spark.streaming._
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.streaming.dstream._
import org.apache.spark.rdd.RDD
import com.corundumstudio.socketio._
import com.google.gson._

object MindflexAlpha {
  
  val WEBSOCKET_PORT = 8080
  val MINDFLEX_PORT = 9999
  val FRAME_SIZE = 10
  val REFRESH_RATE = 1
  val HISTORY_SIZE = 5
  val LOGFILE = new java.io.PrintWriter(new java.io.File("logs/brainwaves.txt"))
  
  var ssc:StreamingContext = null
  var inputStream:ReceiverInputDStream[String] = null
  var websocketServer:com.corundumstudio.socketio.SocketIOServer = null
  var connectedToWebsocket = false
  val gson = new Gson
  
  case class BrainFrame(attention:Double,meditation:Double,
      delta:Double,theta:Double,lowAlpha:Double, highAlpha:Double,lowBeta:Double,
      highBeta:Double,lowGamma:Double,highGamma:Double)
  
  def main(args: Array[String]) = {
    
    initialize
    
    val brainWaves = getWindowedBrainFrame(FRAME_SIZE,REFRESH_RATE)
    val recentHistory = getWindowedBrainFrame(HISTORY_SIZE*60,REFRESH_RATE)
    
    val deltas = getDeltasAsPercentages(recentHistory,brainWaves)
    
    deltas foreachRDD {_.collect foreach sendBrainwaves}
        
    ssc.start
    ssc.awaitTermination
  }
  
  def initialize = {
    ssc = new StreamingContext("local[8]" /**TODO change once a cluster is up **/,
      "MindFlexMonitor", Seconds(1))
    inputStream = ssc.socketTextStream("localhost", MINDFLEX_PORT)
    websocketServer = getWebsocketServer
    startWebsocketServer
  }
  
  def getWebsocketServer = {
    val config = new Configuration
    config.setHostname("localhost");
    config.setPort(WEBSOCKET_PORT);
    new SocketIOServer(config)
  }
  
  def startWebsocketServer = {
    websocketServer.addConnectListener(new listener.ConnectListener(){
      def onConnect(client:SocketIOClient){
        connectedToWebsocket = true
      }
    }) 
    websocketServer.addDisconnectListener(new listener.DisconnectListener(){
      def onDisconnect(client:SocketIOClient){
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
          print(".")
          Thread.sleep(500)
        }
      }
    }
  }
  
  def sendBrainwaves(brainWaves:BrainFrame) {
    if(connectedToWebsocket){
      val it = websocketServer.getAllClients.iterator
      while(it.hasNext()) {
        val client = it.next
        client.sendEvent("brainwaves", gson.toJson(brainWaves))
      }
    }
    log(brainWaves)
  }
  
  def log(brainWaves:BrainFrame) = {
    LOGFILE.println(brainWaves)
    LOGFILE.flush
  }
    
  
  def getDeltasAsPercentages(dstream1:DStream[BrainFrame],dstream2:DStream[BrainFrame]) =
    dstream1.transformWith(dstream2, 
      (older:RDD[BrainFrame],younger:RDD[BrainFrame]) => {
        older.zip(younger).map{ case(older,younger) =>
          new BrainFrame(
            ((younger.attention - older.attention) / older.attention),
            ((younger.meditation - older.meditation) /older.meditation),
            ((younger.delta - older.delta) / older.delta),
            ((younger.theta - older.theta) / older.theta),
            ((younger.lowAlpha - older.lowAlpha) / older.lowAlpha),
            ((younger.highAlpha - older.highAlpha) / older.highAlpha),
            ((younger.lowBeta - older.lowBeta) / older.lowBeta),
            ((younger.highBeta - older.highBeta) / older.highBeta),
            ((younger.lowGamma - older.lowGamma) / older.lowGamma),
            ((younger.highGamma - older.highGamma) /older.highGamma) )
        }
     })
  
  def getDeltas(dstream1:DStream[BrainFrame],dstream2:DStream[BrainFrame]) =
    dstream1.transformWith(dstream2, 
      (older:RDD[BrainFrame],younger:RDD[BrainFrame]) => {
        older.zip(younger).map{ case(older,younger) =>
          new BrainFrame(
            younger.attention - older.attention,
            younger.meditation - older.meditation,
            younger.delta - older.delta,
            younger.theta - older.theta,
            younger.lowAlpha - older.lowAlpha,
            younger.highAlpha - older.highAlpha,
            younger.lowBeta - older.lowBeta,
            younger.highBeta - older.highBeta,
            younger.lowGamma - older.lowGamma,
            younger.highGamma - older.highGamma)
        }
     })
  
  def getWindowedBrainFrame(windowDuration:Int,slideDuration:Int) =
    getBrainFrames.map(_ -> 1.0)
      .reduceByWindow(movingAverage, Seconds(windowDuration), Seconds(slideDuration))
      .map(_._1)
  
  def getBrainFrames =
     inputStream filter {line => line.length > 0 && line(0) == '0'} map { line =>
          // first entry is signal strength, should be 0
          val cols = line.split(",").map(_.toDouble)
          new BrainFrame(cols(1),cols(2),cols(3),cols(4),cols(5),cols(6),cols(7),cols(8),cols(9),cols(10))
    }
  
  // if m_n is the mean of x_1 ... x_n, then m_{n+1} = (n*m_n + x_{n+1})/(n+1).
  def movingAverage(a:(BrainFrame,Double), b:(BrainFrame,Double)) = 
    (new BrainFrame(
      (a._2 * a._1.attention + b._2 * b._1.attention)/(a._2+b._2),
      (a._2 * a._1.meditation + b._2 * b._1.meditation)/(a._2+b._2),
      (a._2 * a._1.delta + b._2 * b._1.delta)/(a._2+b._2),
      (a._2 * a._1.theta + b._2 * b._1.theta)/(a._2+b._2),
      (a._2 * a._1.lowAlpha + b._2 * b._1.lowAlpha)/(a._2+b._2),
      (a._2 * a._1.highAlpha + b._2 * b._1.highAlpha)/(a._2+b._2),
      (a._2 * a._1.lowBeta + b._2 * b._1.lowBeta)/(a._2+b._2),
      (a._2 * a._1.highBeta + b._2 * b._1.highBeta)/(a._2+b._2),
      (a._2 * a._1.lowGamma + b._2 * b._1.lowGamma)/(a._2+b._2),
      (a._2 * a._1.highGamma + b._2 * b._1.highGamma)/(a._2+b._2)
    ), a._2+b._2)
    
  def getDeltas(older:RDD[BrainFrame],younger:RDD[BrainFrame]) = {
    older.zip(younger).map{ case(older,younger) =>
      new BrainFrame(
        older.attention - younger.attention,
        older.meditation - younger.meditation,
        older.delta - younger.delta,
        older.theta - younger.theta,
        older.lowAlpha - younger.lowAlpha,
        older.highAlpha - younger.highAlpha,
        older.lowBeta - younger.lowBeta,
        older.highBeta - younger.highBeta,
        older.lowGamma - younger.lowGamma,
        older.highGamma - younger.highGamma)
    }
  }
    
}