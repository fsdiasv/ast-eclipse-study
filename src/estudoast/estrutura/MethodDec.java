package estudoast.estrutura;

import java.util.ArrayList;

public class MethodDec {
	private String name;
	private String classOrg;
	
	private ArrayList<MethodDec> methodsInvocates;
	
	public MethodDec() {
		this.methodsInvocates = new ArrayList<MethodDec>();
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getClassOrg() {
		return classOrg;
	}
	public void setClassOrg(String classOrg) {
		this.classOrg = classOrg;
	}
	public ArrayList<MethodDec> getMethodsInvocates() {
		return methodsInvocates;
	}
	public void setMethodsInvocates(MethodDec methodsInvocates) {
		this.methodsInvocates.add(methodsInvocates);
	}

	
}
