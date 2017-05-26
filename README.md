# Description:
A scoreboard is a centralized data structure that keeps track of where every executing instruction is in any functional unit pipeline.  
Using the scoreboard, it is possible to predict when an instruction will complete and forward results to dependencies.  
This way, it can be computed in advance when a dependent instruction can be issued.

The scoreboard will track the cycle-by-cycle progress of the following three types of functional units. All subclasses of a base class called Unit.  
When instantiating one of these units, you should provide a latency and a name for debugging purposes.  
Here are the three types:
Pipelined:  Fully pipelined with N pipeline stages, which can have up to N instructions in different phases of execution.  Example:  4-stage pipelined multiplier unit.
NonPipelined:  Non-pipelined, where only one instruction can execute at a time and requires N cycles to complete.  Example:  Divider unit.
PipelinedVariable:  Pipelined with uncertain delay.  A minimum delay of N cycles is specified, and N instructions can be in flight, but the pipeline may experience unexpected stalls.  
	A unit of this type will report an unknown completion time until M cycles before completion.  Example:  Load unit.

An instruction tracked by the scoreboard will be identified by its destination register number, so it cannot contain more than one instruction at a time with that same destination.


Example:  For a Pipelined unit with a single stage, it will report 1 on the cycle during which it is issued, 0 on the next cycle when it completes, and then “does not exist” on the cycle after that.
advanceClock:  Step all units forward one cycle.
This method must also report all instructions (destination registers) completing on this cycle (the cycle in which completionTime would report zero for their destination register).
dump:  Print the current state of the whole scoreboard at a particular cycle.

Example functional units implementedin this project:
One cycle ALU (add, sub, etc.)
4-cycle pipelined multiplier
8-cycle unpipelined divider
Load/store unit with N=4 and M=2 for loads (stores are fire-and-forget)

Important: The program takes the input file which contains the instruction sequence and generate the instruction issue schedulefor in-order and out-of-order processor

### Deliverables (Files in amodi1-pr1.tar.gz):
1. makefile
2. Scoreboard.java
3. Unit.java 
4. Inorder.java
5. Latency.java
6. NonPipelined.java
7. OutofOrder.java
8. Pipelined.java
9. PipelinedVariable.java
10. sample-input.txt

## Instructions to execute:
1. make 						       (This will compile the program)
2. java Scoreboard <sample_input_file> <inorder_output_file> <oo_output_file>  	(This will generate both the inorder and out-of-order output_file)
3. make clean 						(Optional : This will clean compiled .class files)

## Output of the program:
1. <inorder_output_file>
2. <oo_output_file>

### NOTE: 
For Load/Store, we have set N=4 and M=2 clock cycles. In case, we want to change it open the Latency.java and change LOAD_N and LOAD_M. 
