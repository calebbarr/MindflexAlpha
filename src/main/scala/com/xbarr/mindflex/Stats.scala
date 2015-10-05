package com.xbarr.mindflex
import MindflexAlpha.BrainWaves

object Stats {
  
  val QUANTILES = 10
  
  case class QuantileStats(
      size:Int,
      min:Double,
      max:Double,
      mean:Double,
      quantiles:Seq[QuantileStats])
      
  implicit class BrainWavesStats(waves:Seq[Stats.QuantileStats]) {
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
  }

  private var stats = Seq[QuantileStats]()
  
  def getStats = this.synchronized { stats }
  
  def updateStats(brainWaves:List[Seq[Double]]) =
    if(!brainWaves.isEmpty) 
      this.synchronized {
        this.stats = 
          if(stats.isEmpty)
            compileByWavelength(brainWaves) map {w => createStats(w,recurse=true)}
          else
            this.stats zip compileByWavelength(brainWaves).map{ waves => 
              createStats(waves,recurse=true)} map {x => weightedAverage(x._1,x._2)}
      println("updated stats")
    }
  
  def compileByWavelength(brainWaves:List[Seq[Double]]) = 
    (0 until BrainWaves.NUM_WAVELENGTHS).toSeq.map{ i=>
      brainWaves map {_(i)}
    }  map {_.toSeq.sorted}
  
  def weightedAverage(stats1:QuantileStats,stats2:QuantileStats):QuantileStats =
    new QuantileStats(
      size = stats1.size + stats2.size,
      min = math.min(stats1.min, stats2.min),
      max = math.max(stats1.max, stats2.max),
      mean = ((stats1.mean * stats1.size) + (stats2.mean * stats2.size)) / (stats1.size + stats2.size),
      quantiles = stats1.quantiles zip stats2.quantiles map {x => weightedAverage(x._1,x._2)} 
    )
  
  def createStats(quantile:Seq[Double],recurse:Boolean=false):QuantileStats = 
    new QuantileStats(
      size=quantile.size,
      min=if(quantile.size > 0) quantile.head else 0,
      max=if(quantile.size > 0) quantile.last else 0,
      mean=if(quantile.size > 0) quantile.sum/quantile.size.toDouble else 0,
      quantiles=if(!recurse || quantile.size == 0) Seq[QuantileStats]()
      else quantile.grouped(quantile.size/QUANTILES).toArray.map{x=>createStats(x)})
  
}