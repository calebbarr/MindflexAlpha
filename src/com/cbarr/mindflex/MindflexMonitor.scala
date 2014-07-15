package com.cbarr.mindflex

import org.apache.spark.streaming._
import org.apache.spark.streaming.StreamingContext._



object MindflexMonitor {
  
  var ssc:StreamingContext = null
  
  val FRAME_SIZE = 30
  val REFRESH_RATE = 1
  val HISTORY_SIZE = 5
  
  case class BrainFrame(signalStrength:Double,attention:Double,meditation:Double,
      delta:Double,theta:Double,lowAlpha:Double, highAlpha:Double,lowBeta:Double,
      highBeta:Double,lowGamma:Double,highGamma:Double)
  
  def main(args: Array[String]) = {
    
    initialize
    
    val brainWaves = getWindowedBrainFrame(FRAME_SIZE,REFRESH_RATE)
    val lastStep = getWindowedBrainFrame(FRAME_SIZE*2,REFRESH_RATE*2)
    val recentHistory = getWindowedBrainFrame(HISTORY_SIZE*60,60)
    
    brainWaves.print
    lastStep.print
    recentHistory.print
    
    ssc.start
    ssc.awaitTermination
  }
  
  def initialize = {
    ssc = new StreamingContext("local[6]" /**TODO change once a cluster is up **/,
      "MindFlexMonitor", Seconds(1))
  }
  
  def getWindowedBrainFrame(windowDuration:Int,slideDuration:Int) =
    getBrainFrames.map(_ -> 1.0)
      .reduceByWindow(movingAverage, Seconds(windowDuration), Seconds(slideDuration))
      .map(_._1)
  
  def getBrainFrames =
    ssc.socketTextStream("localhost", 9999) map { line =>
          val cols = line.split(",").map(_.toDouble)
          new BrainFrame(cols(0),cols(1),cols(2),cols(3),cols(4),cols(5),cols(6),cols(7),cols(8),cols(9),cols(10)) 
    }
  
  // if m_n is the mean of x_1 ... x_n, then m_{n+1} = (n*m_n + x_{n+1})/(n+1).
  def movingAverage(a:(BrainFrame,Double), b:(BrainFrame,Double)) = 
    (new BrainFrame(
        (a._2 * a._1.signalStrength + b._2 * b._1.signalStrength)/(a._2+b._2),
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
}