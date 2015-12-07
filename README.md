# JCo Migration Helper Plugin
The plugin JCo 3 Migration Helper Plugin is an eclipse/NetWeaver Studio plugin which helps developers to adapt the API changes from the Java Connector.

## Feature list

* Switch Parameters of the setValue calls
* Find all locations where the setValue calls must be switched
* Migrate JCo 2 imports/variable declarations/casts and much more with one click 

## Prerequisites

* Java 8
* Eclipse 3.5 (Galileo) or NWDS

# Installation

Use the update site from **MISSING NOT AVAILABLE YET** to install the plugin.

# How to use it

## Find locations where the switch is required

Right click on the Java Project/Package/File and choose JCo Migration Helper --> Generate Markers

The plugin will go through your Java Classes and search for all variables using the setValue calls of JCo and mark them as a **Migration Candidate**. Open the Markers view to see the list of generated markers.

## Switch Parameters of the setValue calls

Right click on the variable and choose **Switch Parameters**.

The plugin will search for all occurrences of the variable, where the setValue method is invoked and switch the parameters. The marker will be deleted additionally after a successful switch.

## Migrate JCo 2 artifacts

Right click on the Java Project/Package/File and choose JCo Migration Helper --> Organize JCo Imports

It will go through your Java Classes and

* search for Old JCo 2 Class-Imports (com.sap.mw.jco\*) and change them to (com.sap.conn.jco.\*)
* Migrate variable and field declarations
* Migrate method parameters and return statements
* Migrate exceptions (catch clauses and throws statements)
* Migrate casts

A class which could look like this:

```java
package com.sap.ims.isa.tests.jcomigration.imports;

import com.sap.mw.jco.JCO;


public class OldJCoAllInOne {

    private com.sap.mw.jco.JCO.Function variantReadData;
    private JCO.Table table;

    public static void simpleCall(com.sap.mw.jco.JCO.Function func, JCO.Table table) throws Exception {
		try {

			JCO.Function funcInst = new JCO.Function();
			
			// now get repository infos about the function
			JCO.Function variantReadData = connection.getJCoFunction("CRM_ISA_PCAT_VARIANT_DATA_GET");

			// getting import parameter
			com.sap.mw.jco.JCO.ParameterList importParams = variantReadData.getImportParameterList();

			// setting the id of the attribute set
			importParams.setValue("valueCatalog", "CATALOG");
			importParams.setValue("valueVariant", "VARIANT");

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
```
would look after migration like this:
```java

```