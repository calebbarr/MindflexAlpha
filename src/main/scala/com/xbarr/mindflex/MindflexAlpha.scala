package com.xbarr.mindflex

import com.xbarr.mindflex.Ingest.brainWaves
import com.xbarr.mindflex.Stats.{getStats=>stats}
import com.xbarr.mindflex.Constants._
import scala.math.Ordering.Implicits._


object MindflexAlpha {
      
  def main(args: Array[String]): Unit = 
    brainWaves.
      zip3(
          brainWaves.toRollingAvg().deltas,
          brainWaves.windowAvg()
      ).foreach { case(raw,delta,window) => Publish.publish(raw,delta,window) }
  
  implicit class BrainWaves(waves:Seq[Double]) {
    
    assert(waves.size == BrainWaves.NUM_WAVELENGTHS)
    
    val attention = waves(0)
    val meditation = waves(1)
    val delta = waves(2)
    val theta = waves(3)
    val lowAlpha = waves(4)
    val highAlpha = waves(5)
    val lowBeta = waves(6)
    val highBeta = waves(7)
    val lowGamma = waves(8)
    val highGamma = waves(9)
    
    def stringify = waves map { _.toString } reduce {_+" "+_}
    
  }
  
  object BrainWaves {
    
    val NUM_WAVELENGTHS = 10
    
  }
  
  implicit class BrainWavesArchive(unarchived:List[Seq[Double]]) {
    
    assert(unarchived.forall { waves => waves.size == BrainWaves.NUM_WAVELENGTHS})
    
    def compileByWavelength = 
      (0 until BrainWaves.NUM_WAVELENGTHS).toSeq.map{ i=>
        unarchived map {_(i)}
      }  map {_.toSeq.sorted}
  }
  
  implicit class BrainWavesIterator(it: Iterator[Seq[Double]]){
   
    def getDeltas(compareWindow: Iterator[Seq[Double]]) =
      compareWindow zip it map { case (x, y) => x zip y map Function.tupled(_/_) }
   
    def toRollingAvg(lastAvg:Seq[Double]=it.next,offset:Int=1) = 
      RollingAvg(it,lastAvg,offset)
   
    def windowAvg(size:Int=5) =
      it.sliding (size) map {
        _.reduce { (x, y) => x zip y map Function.tupled(_+_) } map { _/size.toDouble }
      }
    
  }
  
  implicit class Zip3Able[T](s:Iterator[T]) extends Iterator[T]{
    
    def zip3(b:Iterator[T],c:Iterator[T]) = 
      this.zip(b) zip(c) map {x=>(x._1._1,x._1._2,x._2)}
    
    def next = s.next
    
    def hasNext = s.hasNext
    
  }
  
  case class RollingAvg(
    stream: Iterator[Seq[Double]], 
    private var _lastAvg: Seq[Double],
    private var _index:Double=1) extends Iterator[Seq[Double]] {
      
    private def indexedAvg(x_n1:Seq[Double]) =
      // if m_n is the mean of x_1 ... x_n, then m_{n+1} = (n*m_n + x_{n+1})/(n+1).
      _lastAvg map { _ * _index } zip x_n1 map Function.tupled(_+_) map { _/(_index+1) }

    def hasNext = this.synchronized { stream.hasNext }
      
    def lastAvg = this.synchronized{ _lastAvg }
      
    def index = this.synchronized { _index}
      
    def next = this.synchronized {
      val nextAvg = indexedAvg(stream.next)
      _lastAvg = nextAvg
      _index = _index+1
      nextAvg
    }
      
    def deltas = new DeltaIterator(this.stream,this.lastAvg,this.index) 
      
  }
  
  class DeltaIterator(
    rollingAvgIt:Iterator[Seq[Double]],
    private var _lastAvg: Seq[Double],
    private var _index:Double=1)
    extends RollingAvg(rollingAvgIt,_lastAvg,_index) {
    
    private val windowAvgDeltas = 
      stream.getDeltas(stream.windowAvg())
      
    override def hasNext = 
      if(stats.isEmpty) windowAvgDeltas.hasNext else rollingAvgIt.hasNext  
    
    override def next = 
      if(stats.isEmpty) {
        rollingAvgIt.next //advance iterator
        windowAvgDeltas.next
      } else 
       rollingAvgIt.next zip {stats map {_.mean}} map Function.tupled(_/_) 
  
  }
  
}