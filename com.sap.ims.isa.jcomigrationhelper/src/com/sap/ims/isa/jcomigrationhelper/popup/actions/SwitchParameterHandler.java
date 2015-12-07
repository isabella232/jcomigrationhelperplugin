package com.sap.ims.isa.jcomigrationhelper.popup.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class SwitchParameterHandler extends AbstractHandler {

    public SwitchParameterHandler() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public Object execute(ExecutionEvent paramExecutionEvent) throws ExecutionException {
        new SwitchParametersAction().run(null);
        return null;
    }

}
