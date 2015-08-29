val scalaVersion="2.11.6"
ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

libraryDependencies += "com.corundumstudio.socketio" % "netty-socketio" % "1.7.5"
libraryDependencies += "com.google.code.gson" % "gson" % "2.3.1"
libraryDependencies += "org.slf4j" % "log4j-over-slf4j" % "1.7.7"
libraryDependencies += "com.papertrailapp" % "logback-syslog4j" % "1.0.0"
libraryDependencies += "com.amazonaws" % "aws-java-sdk" % "1.0.002"