package apps;

import java.io.*;

import java.util.*;

import structures.Stack;

import java.util.concurrent.*;

public class Expression {

	/**
	 * Expression to be evaluated
	 */
	String expr;                

	/**
	 * Scalar symbols in the expression 
	 */
	ArrayList<ScalarSymbol> scalars;   

	/**
	 * Array symbols in the expression
	 */
	ArrayList<ArraySymbol> arrays;

	/**
	 * String containing all delimiters (characters other than variables and constants), 
	 * to be used with StringTokenizer
	 */
	public static final String delims = " \t*+-/()[]";

	/**
	 * Initializes this Expression object with an input expression. Sets all other
	 * fields to null.
	 * 
	 * @param expr Expression
	 */
	public Expression(String expr) {
		this.expr = expr;
	}

	/**
	 * Populates the scalars and arrays lists with symbols for scalar and array
	 * variables in the expression. For every variable, a SINGLE symbol is created and stored,
	 * even if it appears more than once in the expression.
	 * At this time, values for all variables are set to
	 * zero - they will be loaded from a file in the loadSymbolValues method.
	 */
	public void buildSymbols() {
		scalars = new ArrayList<ScalarSymbol>();
		arrays = new ArrayList<ArraySymbol>();
		ArrayList<String> listOfTokens = new ArrayList<String>();
		StringTokenizer str = new StringTokenizer(expr, delims,true);
		while(str.hasMoreTokens()){
			listOfTokens.add(str.nextToken());
		}
		for(int n = 0; n < listOfTokens.size(); n++){
			if( delims.indexOf(listOfTokens.get(n).charAt(0)) == -1 && !isNumeric(listOfTokens.get(n))){//checking to see if the a delimiter exists or not and if the character is whether or not, an integer 
				if(n+1 < listOfTokens.size() && listOfTokens.get(n+1).charAt(0)=='['){
					if(!duplicateArraySymbol(listOfTokens.get(n))){//We don't want duplicates of an ArraySymbol
						arrays.add(new ArraySymbol(listOfTokens.get(n)));
					}
				}
				else{
					if(!duplicateScalarSymbol(listOfTokens.get(n)))//We don't want duplicates of a ScalarSymbol
						scalars.add(new ScalarSymbol(listOfTokens.get(n)));
				}
			}
		}

		printScalars();
		printArrays();

	}

	private boolean isNumeric(String s){// to check if a integer exists in the string 
		try{
			int num= Integer.parseInt(s);
			return true;
		} catch(Exception e){
			return false;
		}
	}
	private boolean duplicateArraySymbol(String s){//To check if a duplicate of an ArraySymbol exists
		for(ArraySymbol as : arrays){
			if(as.name.equals(s)){
				return true;
			}
		}
		return false;
	}
	private boolean duplicateScalarSymbol(String s){
		for(ScalarSymbol as : scalars){//To check if a duplicate of a ScalarSymbol exists 
			if(as.name.equals(s)){
				return true ;
			}

		}
		return false;
	}


	/**
	 * Loads values for symbols in the expression
	 * 
	 * @param sc Scanner for values input
	 * @throws IOException If there is a problem with the input 
	 */
	public void loadSymbolValues(Scanner sc) 
			throws IOException {
		while (sc.hasNextLine()) {
			StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
			int numTokens = st.countTokens();
			String sym = st.nextToken();
			ScalarSymbol ssymbol = new ScalarSymbol(sym);
			ArraySymbol asymbol = new ArraySymbol(sym);
			int ssi = scalars.indexOf(ssymbol);
			int asi = arrays.indexOf(asymbol);
			if (ssi == -1 && asi == -1) {
				continue;
			}
			int num = Integer.parseInt(st.nextToken());
			if (numTokens == 2) { // scalar symbol
				scalars.get(ssi).value = num;
			} else { // array symbol
				asymbol = arrays.get(asi);
				asymbol.values = new int[num];
				// following are (index,val) pairs
				while (st.hasMoreTokens()) {
					String tok = st.nextToken();
					StringTokenizer stt = new StringTokenizer(tok," (,)");
					int index = Integer.parseInt(stt.nextToken());
					int val = Integer.parseInt(stt.nextToken());
					asymbol.values[index] = val;              
				}
			}
		}
	}


	/**
	 * Evaluates the expression, using RECURSION to evaluate subexpressions and to evaluate array 
	 * subscript expressions.
	 * 
	 * @return Result of evaluation
	 */
	public float evaluate() {
		/** COMPLETE THIS METHOD **/
		// following line just a placeholder for compilation*/
		//return 0;
		return evaluateExpr(expr);
	}
	
	private float evaluateExpr(String s){
		Stack<String> operators  = new Stack<String>();
		Stack<Float> integers = new Stack<Float>();
		
		StringTokenizer str = new StringTokenizer(s,delims,true);
		float result = 0;
		
		if(operators.isEmpty() && integers.size() == 1){			
			result = integers.pop();
		}
		
		while(str.hasMoreTokens()){
			String token = str.nextToken();
			if(token.equals("(")){
				result = evaluateSubExpression(str);
				integers.push(result);
			}
			else if(isScalar(token)){
				integers.push(getScalarValue(token));
			}
			else if(isNumeric(token)){
				integers.push(Float.parseFloat(token));
			}
			else if (isArray(token)) { 				
				result = evaluateArray(str, token);
				integers.push(result);
			}
			
			if(Operator(token)){
				operators.push(token);
			}
		}
		
		Stack<Float> rv = new Stack<Float>();
		Stack<String> rs = new Stack<String>();
		Stack<Float> value2 = new Stack<Float>();
		Stack<String> op2 = new Stack<String>();
		
		while (!integers.isEmpty()) {
			rv.push(integers.pop());
		}
		while (!operators.isEmpty()) {
			rs.push(operators.pop());
		}
		while (!rv.isEmpty()) {
			value2.push(rv.pop());
		}
		while (!rs.isEmpty()) {
			op2.push(rs.pop());
		}
		while (!value2.isEmpty()) {
			integers.push(value2.pop());
		}
		while (!op2.isEmpty()) {
			operators.push(op2.pop());
		}
				
		result = integers.pop();
		while(!operators.isEmpty()){
			float num = integers.pop();
			String op = operators.pop();
			if(op.equals("+") || op.equals("-")){
				value2.push(result);
				op2.push(op);
				result = num;
			}
			else if( op.equals("*") || op.equals("/")){
				 result = mathOps(result, op ,num);		
			}
		}
		value2.push(result);
		
		while(!value2.isEmpty()){
			float v = value2.pop();
			integers.push(v);
		}
		while(!op2.isEmpty()){
			String o = op2.pop();
			operators.push(o);
		}
		result = integers.pop();
		while( !operators.isEmpty()){
			float num = integers.pop();
			String op = operators.pop();
			if(op.equals("+") || op.equals("-")) {
				result = mathOps(result, op, num);
			
		}
			
		}

		return result;
	}
	
	private float evaluateArray(StringTokenizer str, String array) {//method for evaluating arrays
		float result=0;
		int count=0;
		Stack<String> operators  = new Stack<String>();
		Stack<Float> integers = new Stack<Float>();
		
		//count = str.countTokens();
		//System.out.println(" evaluateArray Token Count= " + count);

		while(str.hasMoreTokens()){
			 String token = str.nextToken();
			if(token.equals("[")){
				float temp = evaluateArray(str, array);//Calling evaluateArray recursively
				result = getArrayValue(array, (int)temp);
				integers.push(temp);
				return(result);
			}	 
			else if(token.equals("]")) {
				result = evaluateSubExpr2(integers, operators);
				return(result);
			}
			else if(isScalar(token)){
				integers.push(getScalarValue(token));
			}
			else if(isNumeric(token)){
				integers.push(Float.parseFloat(token));
			}
			else if (isArray(token)) { 			
				result = evaluateArray(str, token);	//Calling evaluateArray recursively 
				integers.push(result);
			}
			if(Operator(token)){
				operators.push(token);
			}

		}
		
		return(result);
	}
	/*
	 * method evaluates subexpressions in parentheses
	 */
	private float evaluateSubExpression(StringTokenizer str){
		float result=0;
		int count=0;
		Stack<String> operators  = new Stack<String>();
		Stack<Float> integers = new Stack<Float>();
		
		count = str.countTokens();
		while(str.hasMoreTokens()){
			 String token = str.nextToken();
			if(token.equals("(")){
				float temp = evaluateSubExpression(str);//Calling evaluateSubExpression recursively
							
				integers.push(temp);
			}	 
			else if(token.equals(")")) {
				result = evaluateSubExpr(integers, operators);
				return(result);
			}
			else if(isScalar(token)){
				integers.push(getScalarValue(token));
			}
			else if(isNumeric(token)){
				integers.push(Float.parseFloat(token));
			}
			else if (isArray(token)) { 			
				result = evaluateArray(str, token);
				integers.push(result);
			}
			
			if(Operator(token)){
				operators.push(token);
			}

		}
		
		return(result);
	}
	/*
	 * Method evaluates subexpressions in arrays
	 */
	private float evaluateSubExpr2(Stack<Float> integers, Stack<String> operators){
		float result =0;

		if(operators.isEmpty() && integers.size() == 1)
			return(integers.pop());
		
		Stack<Float> rv = new Stack<Float>();
		Stack<String> rs = new Stack<String>();
		Stack<Float> value2 = new Stack<Float>();
		Stack<String> op2 = new Stack<String>();
		//rv stack is used to reverse the values to get in right order
		while (!integers.isEmpty()) {
			rv.push(integers.pop());
		}
		while (!operators.isEmpty()) {
			rs.push(operators.pop());
		}
		while (!rv.isEmpty()) {
			value2.push(rv.pop());
		}
		while (!rs.isEmpty()) {
			op2.push(rs.pop());
		}
		while (!value2.isEmpty()) {
			integers.push(value2.pop());
		}
		while (!op2.isEmpty()) {
			operators.push(op2.pop());
		}
				
		//result = integers.pop();
		while(!operators.isEmpty()){
			float num = integers.pop();
			float num2=0;
			String op = operators.pop();
			if( op.equals("+") || op.equals("-") || op.equals("*") || op.equals("/")){
				num2 = integers.pop();
				 result = mathOps(num, op ,num2);
				value2.push(result);
				integers.push(result);
			}
		}
		
		return result;		
	}

	private float evaluateSubExpr(Stack<Float> integers, Stack<String> operators){ 
		float result =0;

		Stack<Float> rv = new Stack<Float>();
		Stack<String> rs = new Stack<String>();
		Stack<Float> value2 = new Stack<Float>();
		Stack<String> op2 = new Stack<String>();
		//rv stack is used to reverse the values to get in right order
		while (!integers.isEmpty()) {
			rv.push(integers.pop());
		}
		while (!operators.isEmpty()) {
			rs.push(operators.pop());
		}
		while (!rv.isEmpty()) {
			value2.push(rv.pop());
		}
		while (!rs.isEmpty()) {
			op2.push(rs.pop());
		}
		while (!value2.isEmpty()) {
			integers.push(value2.pop());
		}
		while (!op2.isEmpty()) {
			operators.push(op2.pop());
		}
		
		//result = integers.pop();
		while(!operators.isEmpty()){
			float num = integers.pop();
			float num2=0;
			String op = operators.pop();
			if( op.equals("+") || op.equals("-") || op.equals("*") || op.equals("/")){
				num2 = integers.pop();
				 result = mathOps(num, op ,num2);
				value2.push(result);
			}
		}
		
		while(!value2.isEmpty()){
			float v = value2.pop();
			integers.push(v);
		}
		while(!op2.isEmpty()){
			String o = op2.pop();
			operators.push(o);
		}
		result = integers.pop();
		while( !operators.isEmpty()){
			float num = integers.pop();
			String op = operators.pop();
			if(op.equals("+") || op.equals("-")) {
				result = mathOps(result, op, num);			
		}
			
		}
	
		return result;		
	}
	/*
	 * Checks for arrays 
	 */
	private boolean isArray(String s) {
		for (ArraySymbol as: arrays) { 
			if (s.equals(as.name)) { 
				return true;
			}
		}
		return false;
	}
	/*
	 * Gets scalar value
	 */
	private float getScalarValue(String s){
		for( int a = 0; a < scalars.size(); a++){
			if(s.equals(scalars.get(a).name)){
				float result = (float)scalars.get(a).value;
				return result;
			}
		}
		return 0;
	}
	

	/*
	 * Gets array value
	 */
	private float getArrayValue(String s, int index){ 
		for(ArraySymbol as: arrays){
			if(as.name.equals(s)){
				
				return (float)as.values[index];
			}
		}
		return 0;
	}
	/*
	 * Checks for scalars
	 */
	private boolean isScalar(String s){
		for(ScalarSymbol p : scalars){
			if(s.equals(p.name)){
				return true;
			}
		}
		return false;

	}
	/*
	 * Checks for operators
	 */
	private boolean Operator(String s){
		if(s.equals("+")||s.equals("-")||s.equals("*")||s.equals("/")){
			return true;
		}
		return false;
	}
	/*
	 * Performs arithmetic 
	 */
	private float mathOps(float x, String y, float z){
		float result = 0;
		if(y.equals("*")){
			result = x*z;
		}
		else if(y.equals("-")){
			result = x-z;
		}
		else if(y.equals("+")){
			result = x + z;
		}
		else{
			result = x / z;
		}
		return result;
	}


	/**
	 * Utility method, prints the symbols in the scalars list
	 */
	public void printScalars() {
		for (ScalarSymbol ss: scalars) {
			System.out.println(ss);
		}
	}

	/**
	 * Utility method, prints the symbols in the arrays list
	 */
	public void printArrays() {
		for (ArraySymbol as: arrays) {
			System.out.println(as);
		}
	}

}
