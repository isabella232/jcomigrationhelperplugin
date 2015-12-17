package com.sap.ims.isa.tests.jcomigration.switching;

import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;


public class JCoMixsetValuesFieldUnused {

    private JCoParameterList importParams; 
    
    public void simpleCall(JCoDestination destination) throws Exception {
		try {

			// now get repository infos about the function
			JCoFunction variantReadData = destination.getRepository().getFunctionTemplate("SOME_FM").getFunction();

			// getting import parameter
			importParams = variantReadData.getImportParameterList();
			
			Object value = importParams.getValue("NAME");
	        System.out.println(value);
	        
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
