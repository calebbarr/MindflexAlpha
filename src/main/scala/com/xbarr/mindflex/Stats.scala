package com.xbarr.mindflex

import com.xbarr.mindflex.Ingest.unarchiveBrainwaves

object Stats {
  
  val QUANTILES = 10
  
  case class QuantileStats(
      size:Int,
      min:Double,
      max:Double,
      mean:Double,
      quantiles:Seq[QuantileStats])

  lazy val stats =  {
    val unarchivedBrainwaves = unarchiveBrainwaves
    val stats = (0 until unarchivedBrainwaves.head.size).toList map {i => 
      unarchivedBrainwaves map {_(i)} } map {_.toSeq.sorted} map { waves => 
        createStats(waves,recurse=true)}
    stats
  }
  
  def createStats(quantile:Seq[Double],recurse:Boolean=false):QuantileStats = 
    new QuantileStats(
      quantile.size,
      quantile.head,
      quantile.last,
      quantile.sum/quantile.size.toDouble,
      if(!recurse) Seq[QuantileStats]()
      else quantile.grouped(quantile.size/QUANTILES).toArray.map{x=>createStats(x)})
  
}