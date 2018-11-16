JAVAC = javac
JFLAGS = -g
SOURCES = ArrayEditor.java

.SUFFIXES:  .java  .class

.java.class:
	$(JAVAC) -d bin/ *.java $<

# ArrayEditor.class: ArrayEditor.java

compile:
	$(JAVAC) -d bin/ src/ArrayEditor.java

clean:
	@rm –f *.class

run:
	java -ea -cp bin/ ArrayEditor