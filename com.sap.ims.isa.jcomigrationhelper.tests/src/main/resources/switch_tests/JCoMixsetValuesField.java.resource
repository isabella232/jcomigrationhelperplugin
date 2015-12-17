package com.sap.ims.isa.tests.jcomigration.switching;

import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;

public class JCoMixsetValuesField {

	private JCoParameterList importParams;
	private com.sap.conn.jco.JCoParameterList importParamsFqn;

	public void simpleCall(JCoDestination destination) throws Exception {
		try {

			// now get repository infos about the function
			JCoFunction variantReadData = destination.getRepository().getFunctionTemplate("SOME_FM").getFunction();

			// getting import parameter
			importParams = variantReadData.getImportParameterList();

			// setting the id of the attribute set
			importParams.setValue("value_1", "NAME_1");
			importParams.setValue("value_2", "NAME_2");

			// call the function
			variantReadData.execute(destination);

			// get the output parameter
			JCoParameterList exportParams = variantReadData.getExportParameterList();

			String returnCode = exportParams.getString("RETURNCODE");
			System.out.println(returnCode);

		} catch (JCoException ex) {
			ex.printStackTrace();
		}
	}

	public void simpleCallFqn(JCoDestination destination) throws Exception {
		JCoFunction variantReadData = destination.getRepository().getFunctionTemplate("SOME_FM").getFunction();
		// getting import parameter
		importParamsFqn = variantReadData.getImportParameterList();

		// setting the id of the attribute set
		importParamsFqn.setValue("value_1", "NAME_1");
		importParamsFqn.setValue("value_2", "NAME_2");
	}
}
