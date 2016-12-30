
public class PipelinedVariable extends Unit{
	private int n;
	private int m;
	private String src3;
	
	public String getSrc3() {
		return src3;
	}
	public void setSrc3(String src3) {
		this.src3 = src3;
	}
	public int getN() {
		return n;
	}
	public void setN(int n) {
		this.n = n;
	}
	public int getM() {
		return m;
	}
	public void setM(int m) {
		this.m = m;
	}
	public PipelinedVariable (int index,int latency,String name, String dest, String src1,String src2, int n, int m) {
		super(index,latency, name, dest, src1, src2);
		this.n=n;
		this.m=m;
	}
	public PipelinedVariable (int index, int latency,String name, String dest, String src1,String src2, String src3,int n, int m) {
		
		super(index,latency, name, null, src1, src2);
		this.src3=src3;
		this.n=n;
		this.m=m;
	}
}
