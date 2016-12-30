
import java.util.HashSet;

public class Unit {
	
	private int index;
	private int latency;
	private int cycle_no;
	private String name;
	private String dest;
	private String src1;
	private String src2;
	private String status;
	
	boolean isExecuting;
	HashSet<Integer> dependents;
	
	public HashSet<Integer> getDependents() {
		return dependents;
	}

	public void setDependents(HashSet<Integer> dependents) {
		this.dependents = dependents;
	}
	
	public int getCycle_no() {
		return cycle_no;
	}

	public void setCycle_no(int cycle_no) {
		this.cycle_no = cycle_no;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getLatency() {
		return latency;
	}

	public void setLatency(int latency) {
		this.latency = latency;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDest() {
		return dest;
	}

	public void setDest(String dest) {
		this.dest = dest;
	}

	public String getSrc1() {
		return src1;
	}

	public void setSrc1(String src1) {
		this.src1 = src1;
	}

	public String getSrc2() {
		return src2;
	}

	public void setSrc2(String src2) {
		this.src2 = src2;
	}
	
	public boolean getIsExecuting() {
		return isExecuting;
	}

	public void setIsExecuting(boolean isExecuting) {
		this.isExecuting = isExecuting;
	}


	public Unit(int index, int latency, String name, String dest, String src1, String src2) {
		this.index = index;
		this.name = name;
		this.latency = latency;
		this.dest = dest;
		this.src1=src1;
		this.src2=src2;
		this.status="NA";
		this.dependents=new HashSet<Integer>();
	}
	
}
