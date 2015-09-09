package com.xbarr.mindflex

import com.xbarr.mindflex.Ingest.brainWaves
import com.xbarr.mindflex.Stats._
import com.xbarr.mindflex.Constants._
import com.xbarr.mindflex.Implicits._
import scala.math.Ordering.Implicits._



object MindflexAlpha {
  
  val WINDOW_SIZE = 5
  
  lazy val deltas = getDeltas(
      MindflexAlpha.runningAvg(brainWaves),
      windowAvg(brainWaves,WINDOW_SIZE))
  
  def main(args: Array[String]): Unit = 
    brainWaves.zip3(deltas,windowAvg(brainWaves,WINDOW_SIZE)) foreach { 
      case(waves,delts,window) =>
        Publish.publish(waves,delts,window)
    }
  
  def windowAvg(stream: Stream[Seq[Double]], size: Int = 1) =
    getWindow(stream, size) map {
      _.reduce { (x, y) => x zip y map Function.tupled(_ + _) } map { _ / size.toDouble }
    }

  def average(window: List[Seq[Double]]) =
    window.reduce { (x, y) => x zip y map Function.tupled(_ + _) } map { _ / window.size.toDouble }

  def getWindow[T](stream: Stream[T], size: Int = 1) = stream sliding (size) toStream

  def indexSeq[T](stream: Stream[T], offset: Int = 0) = stream.zipWithIndex map { x => (x._1, x._2 + offset + 1 toDouble) }

  def getDeltas(window: Stream[Seq[Double]], compareWindow: Stream[Seq[Double]]) =
    compareWindow zip window map { case (x, y) => x zip y map Function.tupled(_ / _) }

  def runningAvg(stream: Stream[Seq[Double]], getArchived: Boolean = AWS_CONNECTED) = {
    // if m_n is the mean of x_1 ... x_n, then m_{n+1} = (n*m_n + x_{n+1})/(n+1).
    def indexedAvg(m_n: (Seq[Double], Double), x_n1: (Seq[Double], Double)) =
      (m_n._1 map { _ * m_n._2 } zip x_n1._1 map Function.tupled(_ + _) map { _ / x_n1._2 }, x_n1._2)

    def rollAvg(stream: Stream[(Seq[Double], Double)])(lastAvg: (Seq[Double], Double) = 
      stream.head): Stream[(Seq[Double], Double)] = {
        val avg = indexedAvg(lastAvg, stream.tail.head)
        avg #:: rollAvg(stream.tail)(avg)
    }

    if (getArchived && !stats.isEmpty) {
      val lastAvg = stats map {_.mean} toList
      val indexedStream = indexSeq(stream, offset = stats.head.size)
      indexedStream.head #:: rollAvg(indexedStream)((lastAvg, stats.head.size.toDouble))
    } else {
      val indexedStream = indexSeq(stream)
      indexedStream.head #:: rollAvg(indexedStream)()
    }
  } map {_._1}

}

