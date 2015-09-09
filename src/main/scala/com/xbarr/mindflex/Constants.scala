package com.xbarr.mindflex

object Constants {
  
  import Implicits._
  
  val MINDFLEX_PORT = getEnv("MINDFLEX_PORT",9999)
  val WEBSOCKET_PORT = getEnv("WEBSOCKET_PORT",8081)
  val WEBSOCKET_HOST = getEnv("WEBSOCKET_HOST","localhost")
  val S3_BUCKET = getEnv("S3_BUCKET","")
  val S3_PREFIX = getEnv("S3_PREFIX","")
  
  val AWS_CONNECTED =
    List(
        System.getenv("AWS_ACCESS_KEY_ID"),
        System.getenv("AWS_SECRET_ACCESS_KEY"),
        S3_BUCKET,
        S3_PREFIX
      ) forall isSet
  val PLAYLIST = getEnv("MINDFLEX_PLAYLIST","")
      
  def getEnv[T](env:String,default:T) =
    if(System.getenv(env)!=null)
      cast(System.getenv(env),default).asInstanceOf[T] 
    else default
    
  def cast[T](env:String,default:T) = 
      default.getClass.getSimpleName match {
        case "Integer" => env.toInt
        case "String" => env
        case _ => env.asInstanceOf[T]
      }
  
  def isSet(env:String) = !(env==null||env.isEmpty)  
}

object Implicits {
    
  implicit class Constant(env:String) {
    def isSet = Constants isSet env
  }
  
  implicit class N(i:Int) {
    def times(fn:Unit) = (1 to i) foreach(x=>fn)
  }
  
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
  }
  
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
  
  object BrainWaves {
    val NUM_WAVELENGTHS = 10
  }
  
  implicit class Zip3Able[T](s:Seq[T]) extends Seq[T]{
    def zip3(b:Seq[T],c:Seq[T]) = 
      this.zip(b) zip(c) map {x=>(x._1._1,x._1._2,x._2)}
    def apply = s.apply _
    def apply(idx:Int) = s.apply(idx)
    def length = s.length
    def iterator = s.iterator
  }
  
}

