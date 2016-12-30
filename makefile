all: Scoreboard 

Scoreboard: Scoreboard.java Unit.java Inorder.java Latency.java NonPipelined.java OutofOrder.java Pipelined.java PipelinedVariable.java
	javac *.java

clean:
	rm -rf *.class