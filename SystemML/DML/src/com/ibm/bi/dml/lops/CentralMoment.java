package com.ibm.bi.dml.lops;

import com.ibm.bi.dml.lops.LopProperties.ExecLocation;
import com.ibm.bi.dml.lops.LopProperties.ExecType;
import com.ibm.bi.dml.lops.compile.JobType;
import com.ibm.bi.dml.parser.Expression.*;


/**
 * Lop to perform cross product operation
 * 
 * @author statiko
 */
public class CentralMoment extends Lops {

	/**
	 * Constructor to perform central moment.
	 * input1 <- data (weighted or unweighted)
	 * input2 <- order (integer: 0, 2, 3, or 4)
	 */

	private void init(Lops input1, Lops input2, Lops input3, ExecType et) {
		this.addInput(input1);
		this.addInput(input2);
		input1.addOutput(this);
		input2.addOutput(this);
		
		boolean breaksAlignment = false;
		boolean aligner = false;
		boolean definesMRJob = false;
		if ( et == ExecType.MR ) {
			definesMRJob = true;
			lps.addCompatibility(JobType.CM_COV);
			this.lps.setProperties(et, ExecLocation.MapAndReduce, breaksAlignment, aligner, definesMRJob);
		}
		else {
			// when executing in CP, this lop takes an optional 3rd input (Weights)
			if ( input3 != null ) {
				this.addInput(input3);
				input3.addOutput(this);
			}
			lps.addCompatibility(JobType.INVALID);
			this.lps.setProperties(et, ExecLocation.ControlProgram, breaksAlignment, aligner, definesMRJob);
		}
	}
	
	public CentralMoment(Lops input1, Lops input2, DataType dt, ValueType vt) {
		this(input1, input2, null, dt, vt, ExecType.MR);
	}

	public CentralMoment(Lops input1, Lops input2, DataType dt, ValueType vt, ExecType et) {
		this(input1, input2, null, dt, vt, et);
	}

	public CentralMoment(Lops input1, Lops input2, Lops input3, DataType dt, ValueType vt) {
		this(input1, input2, input3, dt, vt, ExecType.MR);
	}

	public CentralMoment(Lops input1, Lops input2, Lops input3, DataType dt, ValueType vt, ExecType et) {
		super(Lops.Type.CentralMoment, dt, vt);
		init(input1, input2, input3, et);
	}

	@Override
	public String toString() {

		return "Operation = CentralMoment";
	}

	@Override
	public String getInstructions(String input1, String input2, String input3, String output) {
		String opString = new String(getExecType() + Lops.OPERAND_DELIMITOR);
		opString += "cm";

		String inst = new String("");
		// value type for "order" is INT
		inst += opString + OPERAND_DELIMITOR 
				+ input1 + DATATYPE_PREFIX + this.getInputs().get(0).get_dataType() + VALUETYPE_PREFIX + this.getInputs().get(0).get_valueType() + OPERAND_DELIMITOR
				+ input2 + DATATYPE_PREFIX + this.getInputs().get(1).get_dataType() + VALUETYPE_PREFIX + this.getInputs().get(1).get_valueType() + OPERAND_DELIMITOR
				+ input3 + DATATYPE_PREFIX + DataType.SCALAR + VALUETYPE_PREFIX + ValueType.INT + OPERAND_DELIMITOR
				+ output + DATATYPE_PREFIX + this.get_dataType() + VALUETYPE_PREFIX + this.get_valueType();
		return inst;
	}
	
	@Override
	public String getInstructions(String input1, String input2, String output) {
		String opString = new String(getExecType() + Lops.OPERAND_DELIMITOR);
		opString += "cm";

		String inst = new String("");
		// value type for "order" is INT
		inst += opString + OPERAND_DELIMITOR 
				+ input1 + DATATYPE_PREFIX + this.getInputs().get(0).get_dataType() + VALUETYPE_PREFIX + this.getInputs().get(0).get_valueType() + OPERAND_DELIMITOR
				+ input2 + DATATYPE_PREFIX + DataType.SCALAR + VALUETYPE_PREFIX + ValueType.INT + OPERAND_DELIMITOR
				+ output + DATATYPE_PREFIX + this.get_dataType() + VALUETYPE_PREFIX + this.get_valueType();
		return inst;
	}
	
	@Override
	public String getInstructions(int input_index, int output_index) {
		
		// get label for scalar input -- the "order" for central moment.
		String order = this.getInputs().get(1).getOutputParameters().getLabel();
		/*
		 * if it is a literal, copy val, else surround with the label with
		 * ## symbols. these will be replaced at runtime.
		 */
		if(this.getInputs().get(1).getExecLocation() == ExecLocation.Data && 
				((Data)this.getInputs().get(1)).isLiteral())
			; // order = order;
		else
			order = "##" + order + "##";
		
		return getInstructions(input_index+"", order, output_index+"");
	}

}