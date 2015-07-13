package com.xbarr.mindflex

import Ingest.brainWaves

object MindflexAlpha {

  case class BrainFrame(attention:Double,meditation:Double,
      delta:Double,theta:Double,lowAlpha:Double, highAlpha:Double,lowBeta:Double,
      highBeta:Double,lowGamma:Double,highGamma:Double)
  
  def main(args: Array[String]) = 
    deltas(runningAvg(brainWaves),windowAvg(brainWaves,5)) map 
      brainFrame foreach Publish.sendBrainwaves
  
  def brainFrame(cols:Array[Double]) = 
   BrainFrame(cols(1),cols(2),cols(3),cols(4),
     cols(5),cols(6),cols(7),cols(8),cols(9),cols(10))
   
  def windowAvg(stream:Stream[Array[Double]],size:Int=1) =
    getWindow(stream,size) map {
      _.reduce{ (x,y) =>x zip y map Function.tupled(_+_) } map {_/size.toDouble}}
 
  def getWindow[T](stream:Stream[T],size:Int=1) = stream sliding(size) toStream
 
  def indexSeq[T](stream:Stream[T]) = stream.zipWithIndex map{x=>(x._1,x._2+1 toDouble)}
  
  def deltas(window:Stream[Array[Double]], compareWindow:Stream[Array[Double]]) =
    compareWindow zip window map { case(x,y) => x zip y map Function.tupled(_/_)  }
 
  def runningAvg(stream:Stream[Array[Double]]) = {
   // if m_n is the mean of x_1 ... x_n, then m_{n+1} = (n*m_n + x_{n+1})/(n+1).
    def indexedAvg(m_n:(Array[Double],Double), x_n1:(Array[Double],Double)) =
      (m_n._1 map {_*m_n._2} zip x_n1._1 map Function.tupled(_+_) map {_/x_n1._2}, x_n1._2)

    def rollAvg(stream:Stream[(Array[Double],Double)])
      (lastAvg:(Array[Double],Double)=stream.head)
      :Stream[(Array[Double],Double)] = {
        val avg = indexedAvg(lastAvg,stream.tail.head)
        avg #:: rollAvg(stream.tail)(avg)
      }
    val indexedStream = indexSeq(stream)
    indexedStream.head #:: rollAvg(indexedStream)()
  } map {_._1}
    
}