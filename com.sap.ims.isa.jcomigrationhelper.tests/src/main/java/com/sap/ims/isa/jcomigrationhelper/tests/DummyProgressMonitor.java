package com.sap.ims.isa.jcomigrationhelper.tests;

import org.eclipse.core.runtime.IProgressMonitor;

public class DummyProgressMonitor implements IProgressMonitor {
    @Override
    public void worked(int arg0) {

    }

    @Override
    public void subTask(String arg0) {

    }

    @Override
    public void setTaskName(String arg0) {

    }

    @Override
    public void setCanceled(boolean arg0) {}

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public void internalWorked(double arg0) {

    }

    @Override
    public void done() {

    }

    @Override
    public void beginTask(String arg0, int arg1) {

    }
}