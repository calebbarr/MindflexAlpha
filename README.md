#MindflexAlpha

Streaming analytics on brain waves, extensible to various applications.  Requires a modified Mindflex EEG.

## modifying the Mindflex

* Use Arduino IDE to flash the [sketch](https://github.com/calebbarr/MindflexAlpha/blob/master/embedded/MindflexAlphaArduinoSketch.pde) onto an [Arduino Fio](http://arduino.cc/en/Main/ArduinoBoardFio).  It depends on the [Arduino Brain Library](https://github.com/kitschpatrol/Brain).  You may need to hit [reset](http://stackoverflow.com/a/20735393/1215687) at the correct time.
* Connect the T pin on the [Neurosky daughterboard](http://frontiernerds.com/files/imagecache/full-screen/t-pin-soldered.jpg) of a [Mindflex](http://www.ebay.com/sch/i.html?_from=R40&_trksid=p2050601.m570.l1313.TR0.TRC0.H0.Xmindflex+duel+replacement+headset&_nkw=mindflex+duel+replacement+headset&_sacat=0) headset to the [D2](http://www.instructables.com/file/F49LH28GZLW9939) pin of the Fio.  Connect [ground](http://frontiernerds.com/files/imagecache/full-column/4492255397_b86e4a8b56_o.jpg) to ground.
* Connect a [Bluetooth Bee](http://www.seeedstudio.com/depot/Bluetooth-Bee-p-598.html) and a [LiPo battery](https://www.sparkfun.com/products/731).

## communicating with the Mindflex
* 	pair mindflex
* 	`brew install ser2net`
* 	add the following line to the conf file: 
		`9999:raw:0:/dev/tty.mindflex-DevB:38400  XONXOFF`
* 	start ser2net, confirm you are proxying serial traffic with: 
		`lsof -i :9999`
* 	view Mindflex data:
		`nc localhost 9999`

## visualizing the Mindflex
* 	Use [this](https://github.com/calebbarr/MindflexAlpha/tree/master/website/source) [middleman](https://middlemanapp.com/) source directory to build a static website.
* 	It will listen on `8080` and visualize brainwaves using [flot](http://www.flotcharts.org/).
