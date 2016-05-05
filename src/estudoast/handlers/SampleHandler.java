package estudoast.handlers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jface.text.Document;

import estudoast.estrutura.MethodDec;
import estudoast.estrutura.MethodInv;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class SampleHandler extends AbstractHandler {

	private ArrayList<MethodDeclaration> methodsDec; 
	
	/**
	 * O método execute é chamado extraindo as informações necessárias do projeto
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
			this.methodsDec = new ArrayList<MethodDeclaration>();
		
			System.out.println("===================================================");
			System.out.println("===================================================");
			
			// Primeiro pegamos o root do workspace
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot root = workspace.getRoot();
			
			// Em seguida pegamos todos os projetos do workspace
			IProject[] projects = root.getProjects();
			
			// Percorre todos os projetos executando o método chamado
			for (IProject project : projects) {
				try {
					printProjectInfo(project);
					System.out.println("-------------------------------------");
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
			
			//this.getMethodsDec();
		return null;
	}
	
	/**
	 * O método printProjectInfo imprime as informações básicas do projeto
	 */
	private void printProjectInfo(IProject project) throws CoreException, JavaModelException {
			
		//checa se é um projeto java e cria um IJavaProject com o projeto recebido
		if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
			System.out.println("Project: " + project.getName());
			IJavaProject javaProject = JavaCore.create(project);
			printPackageInfo(javaProject);
		}
	}
	
	/**
	 * O método printPackageInfo imprime as informações do pacote 
	 */
	private void printPackageInfo(IJavaProject javaProject) throws JavaModelException {
		IPackageFragment[] packages = javaProject.getPackageFragments();

		
		for (IPackageFragment mypackage : packages) {
			if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) { //apenas os pacotes src
				System.out.println("Package: " + mypackage.getElementName());
				printICompilationUnitInfo(mypackage);
			}
		}
	}
	
	/** 
	 * O método printICompilationUnitInfo imprime as informações da unidade
	 */
	private void printICompilationUnitInfo(IPackageFragment mypackage) throws JavaModelException {
		for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
			Document doc = new Document(unit.getSource());
			
			System.out.println(" Unit Name: " + unit.getElementName() + " / " + doc.getNumberOfLines());
			printIMethods(unit);
		}
	}
	
	/**
	 * Método printIMethods
	 */
	private void printIMethods(ICompilationUnit unit) throws JavaModelException {
		IType[] allTypes = unit.getAllTypes();
		
		//Lista todas as classes do projeto, independente se estiver no mesmo arquivo ou não
		for (IType type : allTypes) {
			System.out.println("\n  Class Name: " + type.getElementName() + " / " + type.getFullyQualifiedName());
			String sourceCode = type.getSource();
			
			ASTParse(sourceCode, type);
			
			//printIMethodsDetails(type);
		}
	}
	
	private void ASTParse(String sourceCode, IType type) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		
		parser.setSource(sourceCode.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		 
		cu.accept(new ASTVisitor() {
 
			Set names = new HashSet();
 
			public boolean visit(MethodDeclaration node) {
				SimpleName name = node.getName();
				this.names.add(name.getIdentifier());
				System.out.println("    Declaration of '" + name.getFullyQualifiedName() + "' at line"
						+ cu.getLineNumber(name.getStartPosition()));
				
				
				methodsDec.add(node);
				
				
				/*MethodDec metodoDec = new MethodDec();
				metodoDec.setName(name.getFullyQualifiedName());
				metodoDec.setClassOrg(type.getElementName());
				*/
				
				Block body = node.getBody();
				body.accept(new ASTVisitor() {
					 
					Set names = new HashSet();
					
		 
					public boolean visit(MethodInvocation node) {
						SimpleName name = node.getName();
					
						this.names.add(name.getIdentifier());
						System.out.println("     Invocation of '" + name.getFullyQualifiedName() + "' at line"
								+ cu.getLineNumber(name.getStartPosition()));
						
						/*MethodDec metodoInv = new MethodDec();
						metodoInv.setName(name.getFullyQualifiedName());
						metodoInv.setClassOrg(name.getParent().toString());
						metodoDec.setMethodsInvocates(metodoInv);
						*/
						return false; // do not continue 
					}
				});

				//methodsDec.add(metodoDec);
				
				return false; // do not continue 
			}
 
			public boolean visit(SimpleName node) {
				if (this.names.contains(node.getIdentifier())) {
					System.out.println("Usage of '" + node + "' at line "
							+ cu.getLineNumber(node.getStartPosition()));
				}
				return true;
			}
		});
 
		
	}
	
	
	/** 
	 * Método printIMethodsDetails
	 */
	private void printIMethodsDetails(IType type) throws JavaModelException {
		IMethod[] methods = type.getMethods();
		
		for (IMethod method : methods) {
			System.out.println("    Method Name: " + method.getElementName());
		}
	}
	
	/**
	 * Imprimir métodos declarados
	 */
	/*private void getMethodsDec() {
		System.out.println("\n ==========================      ========================\n\n");
		
		for (int i = 0; i < this.methodsDec.size(); i++) {
			System.out.println(this.methodsDec.get(i).getClassOrg() + "." + this.methodsDec.get(i).getName());
			
			for(int j = 0 ; j < this.methodsDec.get(i).getMethodsInvocates().size(); j++) {
				System.out.println("  " + this.methodsDec.get(i).getMethodsInvocates().get(j).getName() + " / " 
						+ this.methodsDec.get(i).getMethodsInvocates().get(j).getClassOrg());
			}
		}
	}
	*/
}
