cerberus
========

Raspberry Pi java application for reading RFID

This application uses Java HID API by Codeminders (http://www.codeminders.com/)
under one of three licenses.
1. The GNU Public License, version 3.0, in LICENSE-gpl3.txt
2. A BSD-Style License, in LICENSE-bsd.txt.
3. The more liberal original HIDAPI license. LICENSE-orig.txt

the structure of project
cerberus-|
         |- lib    -   | hidapi-1.1.jar
		 |		       | hidapi-1.1-javadoc.jar
		 |             | hidapi-1.1-sources.jar
		 |		       | log4j-1.2.17.jar
		 |		       | pi4j-core.jar
		 |		       | ..... (pi4j)
		 |		       | sqlite-jdbc-3.7.2.jar
		 |			   
		 |- resources -| log4j.properties
		 |             | properties.properties
		 | 
		 |- src    -   | com .....

to compile use ant
$ ant build

to run application
$ ant run
or
$sudo java -Djava.library.path=/usr/local/lib/ -cp /home/pi/cerberus/build:/home/pi/cerberus/lib/hidapi-1.1.jar:/home/pi/cerberus/lib/sqlite-jdbc-3.7.2.jar:/opt/pi4j/lib/*:/home/pi/cerberus/lib/log4j-1.2.17.jar   com.trei.cerberus.Runner		 
