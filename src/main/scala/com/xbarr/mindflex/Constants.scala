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
  
  implicit class Zip3Able[T](s:Iterator[T]) extends Iterator[T]{
    def zip3(b:Iterator[T],c:Iterator[T]) = 
      this.zip(b) zip(c) map {x=>(x._1._1,x._1._2,x._2)}
    def next = s.next
    def hasNext = s.hasNext
  }
  
  case class RollingAvg(stream: Iterator[(Seq[Double], Double)])
      (lastAvg: (Seq[Double], Double) = stream.next)
      extends Iterator[Seq[Double]]  {
      
      private var _lastAvg:(Seq[Double],Double) = lastAvg
      
      def indexedAvg(m_n: (Seq[Double], Double), x_n1: (Seq[Double], Double)) =
          // if m_n is the mean of x_1 ... x_n, then m_{n+1} = (n*m_n + x_{n+1})/(n+1).
          (m_n._1 map { _ * m_n._2 } zip x_n1._1 
          map Function.tupled(_+_) map { _/x_n1._2 }
          , x_n1._2)
       
      def hasNext = stream.hasNext
      
      def next = this.synchronized {
         val n_i = stream.next
         val nextAvg = indexedAvg(this._lastAvg,n_i)
         this._lastAvg = nextAvg
         nextAvg._1
        }
  }

  
  implicit class Rollable(stream: Iterator[(Seq[Double], Double)]) {
    def toRollingAvg()(lastAvg:(Seq[Double], Double)=stream.next) = 
      RollingAvg(stream)(lastAvg)
  }
}

