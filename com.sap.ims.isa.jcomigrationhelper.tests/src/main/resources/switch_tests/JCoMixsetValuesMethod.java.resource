package com.sap.ims.isa.tests.jcomigration.switching;

import com.sap.conn.jco.JCoParameterList;

public class JCoMixsetValuesMethod {

    public JCoParameterList simpleCall(JCoParameterList importParams) throws Exception {

        // getting import parameter
        // setting the id of the attribute set
        importParams.setValue("value_1", "NAME_1");
        importParams.setValue("value_2", "NAME_2");

        return importParams;
    }
    
    public JCoParameterList simpleCall(com.sap.conn.jco.JCoParameterList importMethodParams) throws Exception {

        // getting import parameter
        // setting the id of the attribute set
        importMethodParams.setValue("value_1", "NAME_1");
        importMethodParams.setValue("value_2", "NAME_2");

        return importParams;
    }
}
