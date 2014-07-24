package com.cbarr.mindflex

object MindflexMusic {
//  import org.jfugue._
  
  
  import java.net.URL;
  import org.apache.log4j.PropertyConfigurator;
  import com.soundhelix.component.player.Player;
  import com.soundhelix.misc.SongContext;
  import com.soundhelix.util.SongUtils;

  
  
  def main(args: Array[String]) = {
//    PropertyConfigurator.configureAndWatch("log4j.properties", 60 * 1000);
    val songContext = SongUtils.generateSong(new URL("http://www.soundhelix.com/applet/examples/SoundHelix-Piano.xml"),
                                                             System.nanoTime());
    val player = songContext.getPlayer();
    
    player.open();
    player.play(songContext);
    player.close();
    
    
    // use this instead to generate a specific song based on the given song name
            // SongContext songContext = SongUtils.generateSong(new URL("http://www.soundhelix.com/applet/examples/SoundHelix-Piano.xml"),
            //                                                  "My song name");
    
  }

}



//    val player = new Player();
//    player.play("C D E F G A B");
//    player.play("C3w D6h E3q F#5i Rs Ab7q Bb2i");
//    player.play("I[Piano] C5q D5q I[Flute] G5q F5q");
//    player.play("Cmaj5q F#min2h Bbmin13^^^");
//    player.play("E5s A5s C6s B5s E5s B5s D6s C6i E6i G#5i E6i | A5s E5s A5s C6s B5s E5s B5s D6s C6i A5i Ri");
    
//    val pattern1 = new Pattern("C5q D5q E5q C5q");
//    val pattern2 = new Pattern("E5q F5q G5h");
//    val pattern3 = new Pattern("G5i A5i G5i F5i E5q C5q");
//    val pattern4 = new Pattern("C5q G4q C5h");
//    val song = new Pattern();
//    song.add(pattern1, 2); // Adds 'pattern1' to 'song' twice
//    song.add(pattern2, 2); // Adds 'pattern2' to 'song' twice
//    song.add(pattern3, 2); // Adds 'pattern3' to 'song' twice
//    song.add(pattern4, 2); // Adds 'pattern4' to 'song' twice
////    player.play(song);
//    
//     val doubleMeasureRest = new Pattern("Rw Rw");
//     val round1 = new Pattern("V0");
//     round1.add(song);
//     
//    val round2 = new Pattern("V1");
//    round2.add(doubleMeasureRest);
//    round2.add(song);
//    
//    
//    val round3 = new Pattern("V2");
//    round3.add(doubleMeasureRest, 2);
//    round3.add(song);
//    
//    val roundSong = new Pattern();
//    roundSong.add(round1);
//    roundSong.add(round2);
//    roundSong.add(round3);
//    
//    player.play(roundSong);
    
//    val rhythm = new Rhythm();
//    rhythm.setLayer(1, "O..oO...O..oOO..");
//    rhythm.setLayer(2, "..*...*...*...*.");
//    rhythm.setLayer(3, "^^^^^^^^^^^^^^^^");
//    rhythm.setLayer(4, "...............!");
//    
//    rhythm.addSubstitution('O', "[BASS_DRUM]i");
//    rhythm.addSubstitution('o', "Rs [BASS_DRUM]s");
//	rhythm.addSubstitution('*', "[ACOUSTIC_SNARE]i");
//	rhythm.addSubstitution('^', "[PEDAL_HI_HAT]s Rs");
//	rhythm.addSubstitution('!', "[CRASH_CYMBAL_1]s Rs");
//	rhythm.addSubstitution('.', "Ri");
//	
//	val pattern = rhythm.getPattern();
//	pattern.repeat(4);
//	player.play(pattern);