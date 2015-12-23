package com.sap.ims.isa.tests.jcomigration.switching;

import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;


public class JCoMixsetValues {

    public static void simpleCall(JCoDestination destination) throws Exception {
		try {

			// now get repository infos about the function
			JCoFunction variantReadData = destination.getRepository().getFunctionTemplate("SOME_FM").getFunction();

			// getting import parameter
			JCoParameterList importParams = variantReadData.getImportParameterList();

			// setting the id of the attribute set
			importParams.setValue("value_1", "NAME_1");
			importParams.setValue(// some comment
			"value_2",
					"NAME_2"
					);
			JCoParameterList importParams2 = variantReadData.getImportParameterList();
			
			// setting the id of the attribute set
			importParams2.setValue("value_1", "NAME_1");
			importParams2.setValue("value_2", "NAME_2");

			JCoParameterList exportParams = variantReadData.getExportParameterList();
			
			// setting the id of the attribute set
			exportParams.setValue("value_1", "NAME_1");
			exportParams.setValue("value_2", "NAME_2");

			variantReadData.getImportParameterList().setValue("value_1", "NAME_1");
			
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
}
