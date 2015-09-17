package com.xbarr.mindflex

import com.xbarr.mindflex.Ingest.brainWaves
import com.xbarr.mindflex.Stats._
import com.xbarr.mindflex.Constants._
import com.xbarr.mindflex.Implicits._
import scala.math.Ordering.Implicits._



// TODO change functions to use generic collections
// TODO create prependable iterator
object MindflexAlpha {
  
  val WINDOW_SIZE = 5
  
  private lazy val deltas = getDeltas(
      MindflexAlpha.runningAvg(brainWaves),
      windowAvg(brainWaves,WINDOW_SIZE))
      
  private lazy val window = windowAvg(brainWaves,WINDOW_SIZE)
  
  def main(args: Array[String]): Unit = 
    brainWaves.zip3(deltas,window) foreach { 
      case(raw,delta,window) =>
        Publish.publish(raw,delta,window)
    }
  
  def windowAvg(stream: Iterator[Seq[Double]], size: Int = 1) =
    getWindow(stream, size) map {
      _.reduce { (x, y) => x zip y map Function.tupled(_+_) } map { _/size.toDouble }
    }

  def average(window: Iterator[Seq[Double]]) =
    window.reduce { (x, y) => x zip y map Function.tupled(_+_) } map { _/window.size.toDouble }

  def getWindow[T](stream: Iterator[T], size: Int = 1) = stream sliding (size)

  def indexIterator[T](stream: Iterator[T], offset: Int = 0) = stream.zipWithIndex map { x => (x._1, x._2 + offset + 1 toDouble) }

  def getDeltas(window: Iterator[Seq[Double]], compareWindow: Iterator[Seq[Double]]) =
    compareWindow zip window map { case (x, y) => x zip y map Function.tupled(_/_) }

  def runningAvg(stream: Iterator[Seq[Double]], getArchived: Boolean = AWS_CONNECTED)
    :Iterator[Seq[Double]]= {
    if (getArchived && !stats.isEmpty) // this triggers S3 pull
      indexIterator(stream, offset = stats.head.size)
        .toRollingAvg()((stats.map{_.mean}.toSeq , stats.head.size.toDouble))
    else {
      val indexed = indexIterator(stream)
      indexed.toRollingAvg()(indexed.next)
    }
  }

}

