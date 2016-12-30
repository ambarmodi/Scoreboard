import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.sound.midi.Instrument;

public class Inorder {
	
	private static final String STATUS_EXECUTE = "Executing";
	private static final String STATUS_DISPATCH = "Dispatch";
	private static final String STATUS_ISSUE = "Issue";
	private static final String STATUS_COMPLETE = "Complete";
	private static final String SEPARATOR = " ";
	public static ArrayList<Integer> DUMP = new ArrayList<Integer>();

	public static LinkedHashMap<Integer, Unit> issuedSequence = new LinkedHashMap<Integer, Unit>();
	public static LinkedHashMap<Integer, Unit> scoreBoard = new LinkedHashMap<Integer, Unit>();

	public static int ISSUEWIDTH;
	public static int CACHEMISS;
	public static int clock_cycle = Latency.CLOCK_CYCLE;
	public static int seq = 0;

	public static StringBuilder inorderResult = new StringBuilder();
	//public static StringBuilder outOfOrderResult = new StringBuilder();
	
	public static void main(String[] args) {
		String inputFile = args[0];
		String inorderOutputFile = args[1];

		BufferedReader bufferedReader = null;
		BufferedWriter inorderBufferedWriter = null;
		
		try {
			FileReader fileReader = new FileReader(inputFile);
			bufferedReader = new BufferedReader(fileReader);

			File inorderfile = new File(inorderOutputFile);
			if (inorderfile.exists()) {
				inorderfile.delete();
			}
			FileWriter inorderFileWriter = new FileWriter(inorderOutputFile, true);
			inorderBufferedWriter = new BufferedWriter(inorderFileWriter);

			String instr_seq;
			for (int i = 0; i < 3; i++) {
				instr_seq = bufferedReader.readLine();
				if (instr_seq.contains("DUMP")) {
					String dump_seq[] = instr_seq.split(" ");
					for (int dump_index = 1; dump_index < dump_seq.length; dump_index++)
						DUMP.add(Integer.parseInt(dump_seq[dump_index]));
				}
				if (instr_seq.contains("ISSUEWIDTH")) {
					ISSUEWIDTH = Integer.parseInt(instr_seq.split(" ")[1]);
				}
				if (instr_seq.contains("CACHEMISS")) {
					CACHEMISS = Integer.parseInt(instr_seq.split(" ")[1]);
				}
			}
			while ((instr_seq = bufferedReader.readLine()) != null) {
				
				Unit instruction = addUnit(instr_seq);
				checkCompletion();
				issueInstruction();
				dispatch(instruction);
				// issue();
				// checkCompletion();
				if (DUMP.contains(clock_cycle)) {
					System.out.println("Dumping the scoreboard data:");
					inorderResult.append("Dumping the scoreboard data:\n");
					dump();
				}
				advanceClock();
			}

			handleRemaining();

		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			bufferedReader.close();
			inorderBufferedWriter.write(inorderResult.toString());
			inorderBufferedWriter.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method executes all the instructions dispatched to the IQ.
	 */
	private static void handleRemaining() {
		while (true) {
			checkCompletion();
			issueInstruction();
			if (DUMP.contains(clock_cycle)) {
				System.out.println("Dumping the scoreboard data:");
				inorderResult.append("Dumping the scoreboard data:\n");
				dump();
			}
			advanceClock();
			int count = issuedSequence.size();
			int current_count = 0;
			for (Entry<Integer, Unit> entry : issuedSequence.entrySet()) {
				Unit temp = entry.getValue();
				if (temp.getStatus().equals(STATUS_COMPLETE)) {
					current_count++;
				}
			}
			if (current_count == count) {
				break;
			}
		}
	}

	@SuppressWarnings("unused")
	private static void printing() {
		// Printing value of score-board and IQ
		System.out.println("Scoreboard:");
		for (Entry<Integer, Unit> entry : scoreBoard.entrySet()) {
			Unit temp = entry.getValue();
			
			String result = "   " + temp.getIndex() + " : " + temp.getName()
					+ " : " + temp.getStatus() + " : CT=" + temp.getLatency();
			
			System.out.println(result);
			inorderResult.append(result);
			inorderResult.append("\n");
		}
		System.out.println("IQ:");
		inorderResult.append("IQ");
		for (Entry<Integer, Unit> entry : issuedSequence.entrySet()) {
			Unit temp = entry.getValue();
			String result = "   " + temp.getIndex() + " : " + temp.getName()
					+ " : " + temp.getStatus() + " : CT=" + temp.getLatency();
			
			System.out.println(result);
			inorderResult.append(result);
			inorderResult.append("\n");
		}
		System.out.println("");

	}

	/**
	 * Increments the clock cycle
	 */
	private static void advanceClock() {
		Unit instruction = null;
		for (Entry<Integer, Unit> entry : scoreBoard.entrySet()) {
			instruction = entry.getValue();
			int completionTime = instruction.getLatency();
			if (instruction.getStatus().equals(STATUS_EXECUTE)) {
				instruction.setLatency(--completionTime);
			}
		}
		clock_cycle++;
	}

	/**
	 * 
	 */
	private static void dump() {
		Unit instr;
		String result1;
		String result2;
		
		result1 = "Cycle " + clock_cycle + " : Dump";
		System.out.println(result1);
		inorderResult.append(result1);
		inorderResult.append("\n");
		
		for (Entry<Integer, Unit> entry : scoreBoard.entrySet()) {
			instr = entry.getValue();
			//TODO : Change this for STR
			if(instr.getName().startsWith("ST")) {
				result2 = " - " + instr.getName()  + " "
						+ instr.getLatency() + " " + instr.getStatus();
			} else if(instr.getName().startsWith("LD")) {
				//if my instr latency > m -->NA else
				if(instr.getLatency() > ((PipelinedVariable)instr).getM()) {
					result2 = " - " + instr.getName() + " " + instr.getDest() + " "
							+ " NA " + " " + instr.getStatus();
				} else {
					result2 = " - " + instr.getName() + " " + instr.getDest() + " "
							+ instr.getLatency() + " " + instr.getStatus();
				}
			}
			else {
				result2 = " - " + instr.getName() + " " + instr.getDest() + " "
					+ instr.getLatency() + " " + instr.getStatus();
			}
			System.out.println(result2);
			inorderResult.append(result2);
			inorderResult.append("\n");
		}
	}

	/**
	 * Marks all the executing instruction as completed at completion timeand delete the completed instr from score-board
	 */
	private static void checkCompletion() {
		// TODO: Check for completionTime().
		String result;
		Unit instruction;
		ArrayList<Integer> removeKey = new ArrayList<Integer>();
		for (Entry<Integer, Unit> entry : scoreBoard.entrySet()) {
			// if completionTime == 0..instr is complete..remove from SB.
			instruction = entry.getValue();
			// TODO: canBeCompleted(instruction);
			if (instruction.getLatency() == 0) {
				
				instruction.setStatus(STATUS_COMPLETE);
				//TODO : Change this for STR
				if(instruction.getName().startsWith("ST")) {
					result="Cycle " + clock_cycle + " Complete : "
							+ instruction.getName() + " " + instruction.getSrc1()
							+ " " + instruction.getSrc2() + " "
							+ ((PipelinedVariable)instruction).getSrc3();
						
				} else {
				result="Cycle " + clock_cycle + " Complete : "
						+ instruction.getName() + " " + instruction.getDest()
						+ " " + instruction.getSrc1() + " "
						+ instruction.getSrc2();
				}
				System.out.println(result);
				inorderResult.append(result);
				inorderResult.append("\n");
				
				removeKey.add(entry.getKey());
			}
		}

		for (int i = 0; i < removeKey.size(); i++) {
			scoreBoard.remove(removeKey.get(i));
		}

	}

	/**
	 * Issues the instruction based on scoreboard
	 */
	private static void issueInstruction() {
		int issue_width = ISSUEWIDTH;
		String result;
		for (Entry<Integer, Unit> entry : issuedSequence.entrySet()) {
			Unit instruction = entry.getValue();
			if (entry.getKey() != clock_cycle) {
				boolean noDependency = checkDependency(instruction);
				boolean fuAvailable = isFuAvailable(instruction);

				if ((noDependency && fuAvailable)
						&& instruction.getStatus().equals(STATUS_DISPATCH)) {
					if (issue_width > 0 && canbeIssued(instruction)) {

						instruction.setStatus(STATUS_ISSUE);
						
						if(instruction.getName().startsWith("ST")) {
							result="Cycle " + clock_cycle
									+ " Issued : " + instruction.getName() + " "
									+ instruction.getSrc1() + " "
									+ instruction.getSrc2() + " "
									+ ((PipelinedVariable)instruction).getSrc3();
						} else {
						result="Cycle " + clock_cycle
								+ " Issued : " + instruction.getName() + " "
								+ instruction.getDest() + " "
								+ instruction.getSrc1() + " "
								+ instruction.getSrc2();
						}
						System.out.println(result);
						inorderResult.append(result);
						inorderResult.append("\n");
						
						instruction.setStatus(STATUS_EXECUTE);
						scoreBoard.put(instruction.getIndex(), instruction);
						issue_width--;
					}
				}
			}
		}
	}

	/**
	 * @param instruction
	 * @return true is Instruction can be issued else return false
	 */
	private static boolean canbeIssued(Unit instruction) {
		Unit prev_instr;
		for (Entry<Integer, Unit> entry : issuedSequence.entrySet()) {
			prev_instr = entry.getValue();
			if (instruction.getIndex() > prev_instr.getIndex()) {
				if (prev_instr.getStatus().equals(STATUS_DISPATCH)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * @param instruction
	 * @return true is FU is Free else return false
	 */
	private static boolean isFuAvailable(Unit instruction) {
		// return true if FU is available else false
		Unit prev_instr;
		boolean isAvailable = true;
		for (Entry<Integer, Unit> entry : issuedSequence.entrySet()) {
			prev_instr = entry.getValue();
			if (prev_instr.getStatus().equals(STATUS_EXECUTE)) {
				if (prev_instr.getName().equals(instruction.getName())) {
					if (instruction.getName().equals("LDH")) {
						if (Latency.LOAD_N == prev_instr.getLatency())
							isAvailable = false;

					} else if (instruction.getName().equals("LDM")) {
						if (Latency.LOAD_N == prev_instr.getLatency())
							isAvailable = false;

					} else if (instruction.getName().equals("ST")) {
						if (Latency.STORE == prev_instr.getLatency())
							isAvailable = false;

					} else if (instruction.getName().equals("MUL")) {
						if (Latency.MUL == prev_instr.getLatency())
							isAvailable = false;

					} else if (instruction.getName().equals("DIV")) {
						if (prev_instr.getStatus().equals(STATUS_EXECUTE))
							isAvailable = false;
					} else {
						if (prev_instr.getStatus().equals(STATUS_EXECUTE))
							isAvailable = false;
					}

				}
			}
		}
		return isAvailable;

	}

	/**
	 * @param instruction
	 * @return true if instruction has no dependency else return false.
	 */
	private static boolean checkDependency(Unit instruction) {
		// Return true for No Dependency...else false

		String src1 = instruction.getSrc1();
		String src2 = instruction.getSrc2();
		String dest = instruction.getDest();
		String src3;
		
		Unit prev_instr;
		String prev_src1, prev_src2, prev_src3, prev_dest;
		HashSet<Integer> dependents;
		for (Entry<Integer, Unit> entry : issuedSequence.entrySet()) {
			prev_instr = entry.getValue();
			prev_src1 = prev_instr.getSrc1();
			prev_src2 = prev_instr.getSrc2();
			prev_dest = prev_instr.getDest();

			if(instruction.getName().equals("ST") && prev_instr.getName().equals("ST")){
				continue;
			}else if(instruction.getName().equals("ST")){
				src3=((PipelinedVariable)instruction).getSrc3();
				// check for true dependencies:R_A_W
				if (prev_instr.getStatus().equals(STATUS_EXECUTE)
						&& ((prev_dest.equals(src1) || prev_dest.equals(src2)
								|| prev_dest.equals(src3)))) {
						
					dependents = prev_instr.getDependents();
					dependents.add(instruction.getIndex());
					prev_instr.setDependents(dependents);
					return false;
				}
			} else if(prev_instr.getName().equals("ST")){
				prev_src3=((PipelinedVariable)prev_instr).getSrc3();
				// check for anti dependencies:W_A_R
				if (prev_instr.getStatus().equals(STATUS_EXECUTE)
						&& ((prev_src1.equals(dest) || prev_src2.equals(dest)
								|| prev_src3.equals(dest)))) {
						
					dependents = prev_instr.getDependents();
					dependents.add(instruction.getIndex());
					prev_instr.setDependents(dependents);
					return false;
				}
			} else {
				// check for output, flow, anti dependencies: W-A-W R-A-W W-AR 
				if (prev_instr.getStatus().equals(STATUS_EXECUTE)
						&& ((prev_dest.equals(dest) || prev_dest.equals(src1)
								|| prev_dest.equals(src2) || prev_src1.equals(dest) || prev_src2
									.equals(dest)))) {
					dependents = prev_instr.getDependents();
					dependents.add(instruction.getIndex());
					prev_instr.setDependents(dependents);
					return false;
				}
			}
		}
		return true;

	}

	/**
	 * Dispatch the instruction
	 * 
	 * @param instruction
	 */
	private static void dispatch(Unit instruction) {
		String result;
		
		instruction.setStatus(STATUS_DISPATCH);
		issuedSequence.put(instruction.getIndex(), instruction);

		if(instruction.getName().startsWith("ST")) {
			result= "Cycle " + clock_cycle + " Dispatch : "
					+ instruction.getName() + " " + instruction.getSrc1() + " "
					+ instruction.getSrc2() + " " + ((PipelinedVariable)instruction).getSrc3();
		}
		else {
		result= "Cycle " + clock_cycle + " Dispatch : "
				+ instruction.getName() + " " + instruction.getDest() + " "
				+ instruction.getSrc1() + " " + instruction.getSrc2();
		}
		System.out.println(result);
		inorderResult.append(result);
		inorderResult.append("\n");
	}

	/**
	 * Create instruction as a respective FU object.
	 * 
	 * @param instr
	 * @return Derived Class of UNIT based on the instruction type
	 */
	private static Unit addUnit(String instr) {
		String[] res = instr.split(SEPARATOR);
		Unit instruction = null;
		if (instr.contains("LDH")) {
			instruction = new PipelinedVariable(seq, Latency.LOAD_N, res[0],
					res[1], res[2], res[3], Latency.LOAD_N, Latency.LOAD_M);

		} else if (instr.contains("LDM")) {
			instruction = new PipelinedVariable(seq,
					Latency.LOAD_N + CACHEMISS, res[0], res[1], res[2], res[3],
					Latency.LOAD_N, Latency.LOAD_M);

		} else if (instr.contains("ST")) {
			instruction = new PipelinedVariable(seq, Latency.STORE, res[0],null,
					res[1], res[2], res[3], Latency.LOAD_N, Latency.LOAD_M);

		} else if (instr.contains("MUL")) {
			instruction = new Pipelined(seq, Latency.MUL, res[0], res[1],
					res[2], res[3]);

		} else if (instr.contains("DIV")) {
			instruction = new NonPipelined(seq, Latency.DIV, res[0], res[1],
					res[2], res[3]);
		} else {
			instruction = new NonPipelined(seq, Latency.ALU, res[0], res[1],
					res[2], res[3]);
		}
		seq++; // IMP
		return instruction;
	}


}
