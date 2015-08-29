package com.xbarr.mindflex

import com.xbarr.mindflex.Ingest.{ brainWaves, unarchivedBrainwaves }
import com.xbarr.mindflex.Constants._

object MindflexAlpha {
  
  def main(args: Array[String]): Unit =
    brainWaves zip deltas(runningAvg(brainWaves),windowAvg(brainWaves,5)) foreach { 
      case(waves,deltas) =>
        Publish.log(waves,deltas)
    }

  def windowAvg(stream: Stream[List[Double]], size: Int = 1) =
    getWindow(stream, size) map {
      _.reduce { (x, y) => x zip y map Function.tupled(_ + _) } map { _ / size.toDouble }
    }

  def average(window: List[List[Double]]) =
    window.reduce { (x, y) => x zip y map Function.tupled(_ + _) } map { _ / window.size.toDouble }

  def getWindow[T](stream: Stream[T], size: Int = 1) = stream sliding (size) toStream

  def indexSeq[T](stream: Stream[T], offset: Int = 0) = stream.zipWithIndex map { x => (x._1, x._2 + offset + 1 toDouble) }

  def deltas(window: Stream[List[Double]], compareWindow: Stream[List[Double]]) =
    compareWindow zip window map { case (x, y) => x zip y map Function.tupled(_ / _) }

  def runningAvg(stream: Stream[List[Double]], getArchived: Boolean = AWS_CONNECTED) = {
    // if m_n is the mean of x_1 ... x_n, then m_{n+1} = (n*m_n + x_{n+1})/(n+1).
    def indexedAvg(m_n: (List[Double], Double), x_n1: (List[Double], Double)) =
      (m_n._1 map { _ * m_n._2 } zip x_n1._1 map Function.tupled(_ + _) map { _ / x_n1._2 }, x_n1._2)

    def rollAvg(stream: Stream[(List[Double], Double)])(lastAvg: (List[Double], Double) = 
      stream.head): Stream[(List[Double], Double)] = {
        val avg = indexedAvg(lastAvg, stream.tail.head)
        avg #:: rollAvg(stream.tail)(avg)
    }

    if (getArchived && !unarchivedBrainwaves.isEmpty) {
      val lastAvg = average(unarchivedBrainwaves)
      val indexedStream = indexSeq(stream, offset = unarchivedBrainwaves.size.toInt)
      indexedStream.head #:: rollAvg(indexedStream)((lastAvg, unarchivedBrainwaves.size.toDouble))
    } else {
      val indexedStream = indexSeq(stream)
      indexedStream.head #:: rollAvg(indexedStream)()
    }
  } map {_._1}

}