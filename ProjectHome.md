cuxna is a Java application designed to practice for the [SCJP (Sun Certified Java Professional) exam](http://education.oracle.com/pls/web_prod-plq-dad/db_pages.getpage?page_id=41&p_exam_id=1Z0_851).
It is meant as a free and open source alternative to the Windows application included with the excellent book
["SCJP Sun Certified Programmer for Java 6 Study Guide"](http://www.mhprofessional.com/product.php?isbn=0071591060) by Kathy Sierra and Bert Bates.
It works by reading the quiz data that is stored as FoxPro dbf files.
The project stem from my frustration for the shortcomings of the provided application,
and it has also been a good way to make some Java experience with a real project.

Main features:
  * free, open source, and being Java, multi-platform
  * questions can just be picked by the user rather than only given in random order. This was probably the feature I needed the most...
  * number of questions and time limit can be customized
  * font is fixed-width, as it should be when displaying source code. Can anyone confirm if maybe the real exam uses variable-width font ?
  * font size can be changed
  * simpler and more intuitive interface

When run for the first time the user is prompted for the path containing the database, that will be automatically saved in java.util.prefs.Preferences
The database can be found:
  * in the directory created by the Windows installer at: `LearnKey\MasterExam\database`
  * in the CD included with the book at: `Programs\MasterExam\robo\database`
The 'database' folder should contain the following files:<br />
075708b(directory) GTEST.CDX GTEST.FPT PTEST.DBF TESTHIST.cdx WS\_FTP.LOG<br />
075708d(directory) GTEST.DBF PTEST.CDX PTEST.FPT TESTHIST.dbf

The free bonus exam mentioned in the book can be downloaded after registering at a quite hard to find [location](http://osborne.onlineexpert.com/elearning/).
The download is well worth and will give you 75 extra questions on top of the 150 included with the software.
The bonus exam is contained in an .exe file though,
so you will need to use a Microsoft Windows operating system, or you can just use wine to install it.

cuxna is released under GNU GPL version 3 and has been developed by Andrea Chiavazza.<br />