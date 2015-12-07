package com.sap.ims.isa.tests.jcomigration.imports;

import com.sap.mw.jco.JCO;


public class OldJCoAllInOne {

    private com.sap.mw.jco.JCO.Function variantReadData;
    private JCO.Table table;

    public static void simpleCall(com.sap.mw.jco.JCO.Function func, JCO.Table table) throws Exception {
		try {

			JCO.Function funcInst = new JCO.Function();
			
			// now get repository infos about the function
			JCO.Function variantReadData = connection.getJCoFunction("SOME_FM");

			// getting import parameter
			com.sap.mw.jco.JCO.ParameterList importParams = variantReadData.getImportParameterList();

			// setting the id of the attribute set
			importParams.setValue("value_1", "NAME_1");
			importParams.setValue("value_2", "NAME_2");

			// call the function
			connection.execute(variantReadData);

			// get the output parameter
			JCO.ParameterList exportParams = variantReadData.getExportParameterList();

			String returnCode = exportParams.getString("RETURNCODE");

		} catch (JCO.Exception | JCO.Exception ex) {
			ex.printStacktrace();
		}
		
		try {
		    throw new JCO.Exception();
		} catch (com.sap.mw.jco.JCO.Exception ex) {
		    ex.printStacktrace();
		}
	}
    public JCO.ParameterList simpleCall(JCO.Function func) throws JCO.Exception {
        return (JCO.ParameterList) null;
    }
    public com.sap.mw.jco.JCO.ParameterList simpleCallFqn(com.sap.mw.jco.JCO.Function func) throws JCO.Exception {
    	return (com.sap.mw.jco.JCO.ParameterList) null;
    }
    public com.sap.mw.jco.JCO.ParameterList simpleCallNoException(com.sap.mw.jco.JCO.Function func) {
    	return (com.sap.mw.jco.JCO.ParameterList) null;
    }
}
