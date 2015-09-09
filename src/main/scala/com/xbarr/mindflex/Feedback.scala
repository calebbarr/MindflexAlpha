package com.xbarr.mindflex

import com.xbarr.mindflex.Constants.PLAYLIST
import com.xbarr.mindflex.Implicits._
import com.xbarr.mindflex.Stats._

object Feedback {

  val channels = List(
    (Quiet,PLAYLIST)    
  ) filter {_._2.isSet} map {_._1.asInstanceOf[FeedbackChannel]}
  
  def update(brainWaves:Seq[Double]) = 
    channels foreach{_.update(brainWaves)}
  
}

abstract class FeedbackChannel {
  val STAGES = QUANTILES
  def update(brainWaves:Seq[Double])
  def initialize = {}
  initialize
  println("feedback " + this.getClass.getSimpleName +" initialized")
}

object Quiet extends FeedbackChannel {
  
  override def initialize = Player.play
  
  override def update(brainWaves:Seq[Double]) = {
    val quantile = stats.lowAlpha.quantiles.zipWithIndex.find{q => 
      brainWaves.lowAlpha >= q._1.min && brainWaves.lowAlpha <= q._1.max}
    if(!quantile.isEmpty)
      Player.Volume.set(1.0 - (quantile.get._2 * .1))
    else println("alpha was out of range, no volume change") // TODO deal with this later
  }
 
  
  private object Player {
    import javax.sound.sampled.{AudioSystem,AudioFormat,LineListener}
    
    private var track = AudioSystem.getClip
    
    if(PLAYLIST.isSet) initialize
    
    def play = track.start
    def pause = track.stop
    def skip = {
      loadTrack(new java.io.File(Playlist.next))
      play
    }
    
    def initialize = {
     track.addLineListener(TrackListener)
     loadTrack(new java.io.File(Playlist.next))
    }
    
    private def loadTrack(mp3file:java.io.File) = {
      println("loading track: " + mp3file.getAbsolutePath)
      val vol = if(track.isOpen) Volume.get else Volume.MAX
      
      track.close
      val audioStream = AudioSystem.getAudioInputStream(mp3file)
      val decodedFormat = new AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED, 
        audioStream.getFormat.getSampleRate(), 
        16, 
        audioStream.getFormat.getChannels(),
        audioStream.getFormat.getChannels() * 2, 
        audioStream.getFormat.getSampleRate(), 
        false)
      track.open(AudioSystem.getAudioInputStream(decodedFormat, audioStream))
      Volume._set(vol)
    }
    
    private object TrackListener extends LineListener {
      import javax.sound.sampled.LineEvent
      def update(event:LineEvent) = 
        event.getType match {
          case LineEvent.Type.STOP => Player.skip
        }
    }
    
    object Volume {
      import javax.sound.sampled.FloatControl
      
      private def gainControl = 
        track.getControl(FloatControl.Type.MASTER_GAIN).asInstanceOf[FloatControl]
      val MAX = 6.0f
      private val _MIN = -80f
      private val MIN_LIMITER = .75f
      val MIN = _MIN * MIN_LIMITER
      val RANGE = MAX-MIN
      
      def _set(f:Float) = {
        gainControl.setValue(f match {
          case min if(min < MIN) => MIN
          case max if(max > MAX) => MAX
          case _ => f })
        println("setting volume: " + f)
      }
      
      def set(d:Double) = {
        assert(d >= 0 && d <= 1)
        _set(MIN + (d * RANGE) toFloat)
      }

      def get = gainControl.getValue
      def turnUp = _set(get + (RANGE / STAGES) )
      def turnDown = _set(get - (RANGE / STAGES) )
      
    }
    
    private object Playlist {
      println("loading playlist from " + PLAYLIST)
      private val tracks = Iterator.continually(
          scala.io.Source.fromFile(PLAYLIST).getLines.filterNot(_.startsWith("#")).toList).flatten
      def next = tracks.next
    }
  }
  
}