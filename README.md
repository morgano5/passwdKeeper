# passwdKeeper
A command line, java based, small and basic password management utility.

This is just a very basic utility to store passwords in an encrypted file, I know there are already many awesome
utilities out there with a beautiful user interface and many features, but it happens that:

1) I'm too paranoid to trust my passwords to other developer's code.
2) I just want a way to have a simple encrypted file that I can keep in a USB stick.
3) I don't want to write my passwords in a clear-text format in the middle of the encryption process (yes, I'm too
 paranoid).
4) I don't want to depend on a graphic user interface.
5) I need a small utility that have as little dependencies as possible (no dependencies on external jars, etc) just a
 jar file I can take in the same USB stick

## Dependencies
* Java JRE 8
* [Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy
 Files]([http://www.oracle.com/technetwork/java/javase/downloads/index.html)

... and that's it

## Build

On the command line and the root directory of this repository as the current working directory, compile the code with:

Linux:

    $ mkdir out
    $ find -name '*.java' > sources.txt
    $ javac @sources.txt -d out

Windows:

    > mkdir out
    > dir /s /B *.java > sources.txt
    > javac @sources.txt -d out

Next, create the jar with:

Linux:

    $ _path_to_JDK_/bin/jar cvfm passwdKeeper.jar resources/META-INF/MANIFEST.MF -C out .

Windows

    > _path_to_JDK_\\bin\\jar.exe cvfm passwdKeeper.jar resources\\META-INF\\MANIFEST.MF -C out .

At this point there must be a jar in the dir called __passwdKeeper.jar__. after this the file __sources.txt__ and the
dir __out__ can be safely deleted to clean up.

## Execute

Just run:

    java -jar passwdKeeper _file_

Where _file_ is the file where the data is going to be stored. If _file_ doesn't exist it will be created.

## TODO
* To find a way of typing the password without being echoed on the terminal. I'm aware of __System.console()__, but in
 some circumstances System.console() doesn't return a console (like in a cygwin terminal under Windows)

