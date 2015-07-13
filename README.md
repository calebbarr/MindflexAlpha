#MindflexAlpha

Streaming analytics on brain waves, extensible to various applications.  Requires a modified Mindflex EEG.
![ScreenShot](http://xbarr.me/mindflexalpha.png)
## modifying the Mindflex

* Use Arduino IDE to flash the [sketch](https://github.com/calebbarr/MindflexAlpha/blob/master/embedded/MindflexAlphaArduinoSketch.pde) onto an [Arduino Fio](http://arduino.cc/en/Main/ArduinoBoardFio).  It depends on the [Arduino Brain Library](https://github.com/kitschpatrol/Brain).  You may need to hit [reset](http://stackoverflow.com/a/20735393/1215687) at the correct time.
* Connect the T pin on the [Neurosky daughterboard](http://frontiernerds.com/files/imagecache/full-screen/t-pin-soldered.jpg) of a [Mindflex](http://www.ebay.com/sch/i.html?_from=R40&_trksid=p2050601.m570.l1313.TR0.TRC0.H0.Xmindflex+duel+replacement+headset&_nkw=mindflex+duel+replacement+headset&_sacat=0) headset to the [D2](http://www.instructables.com/file/F49LH28GZLW9939) pin of the Fio.  Connect [ground](http://frontiernerds.com/files/imagecache/full-column/4492255397_b86e4a8b56_o.jpg) to ground.
* Connect a [Bluetooth Bee](http://www.seeedstudio.com/depot/Bluetooth-Bee-p-598.html) and a [LiPo battery](https://www.sparkfun.com/products/731).

## communicating with the Mindflex
* 	Pair mindflex
* 	`brew install ser2net`
*		`/usr/local/sbin/ser2net -C 9999:raw:0:/dev/tty.mindflex-DevB:38400,XONXOFF -u`
* 	Confirm you are proxying serial traffic with: 
	* 	`lsof -i :9999`
* 	View Mindflex data:
	* 	`nc localhost 9999`

## visualizing the Mindflex
* 	[Serve](https://echo.co/blog/os-x-1010-yosemite-local-development-environment-apache-php-and-mysql-homebrew) the [website directory](https://github.com/calebbarr/MindflexAlpha/tree/master/website) with any web server.
* 	It will listen on `8081` and visualize brainwaves using [flot](http://www.flotcharts.org/).
