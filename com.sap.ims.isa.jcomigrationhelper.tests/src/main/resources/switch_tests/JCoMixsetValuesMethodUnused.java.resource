package com.sap.ims.isa.tests.jcomigration.switching;

import com.sap.conn.jco.JCoParameterList;

public class JCoMixsetValuesMethodUnused {

    public void simpleCall(JCoParameterList importParams) throws Exception {

        // getting import parameter
        // setting the id of the attribute set
        Object value = importParams.getValue("NAME");
        System.out.println(value);
    }
}
