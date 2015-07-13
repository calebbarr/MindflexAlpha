package com.xbarr.mindflex

import java.net.{Socket,InetAddress}
import scala.io.BufferedSource
import Constants._

object Ingest {
  
  lazy val brainWaves = 
      new BufferedSource(new Socket(
          InetAddress.getByName("localhost"), MINDFLEX_PORT)
          .getInputStream).getLines.toStream.filter(!_.isEmpty)
          .map(_.split(",")
          .map(_.toDouble)) filter (_(0) == 0)
}