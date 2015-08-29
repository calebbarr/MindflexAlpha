package com.xbarr.mindflex

object Constants {
  
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
    
  def getEnv[T](env:String,default:T) =
    if(System.getenv(env)!=null)
      cast(System.getenv(env),default).asInstanceOf[T] 
    else default
    
  def cast[T](env:String,default:T) = 
      default match {
        case "Integer" => env.toInt
        case "String" => env
        case _ => env.asInstanceOf[T]
      }
  
  def isSet(env:String) = !(env==null||env.isEmpty)
  
}
