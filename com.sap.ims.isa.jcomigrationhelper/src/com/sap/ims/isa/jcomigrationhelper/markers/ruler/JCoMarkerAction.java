package com.sap.ims.isa.jcomigrationhelper.markers.ruler;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

import com.sap.ims.isa.jcomigrationhelper.i18n.Messages;

public class JCoMarkerAction extends AbstractRulerActionDelegate {

    @Override
    protected IAction createAction(ITextEditor textEditor, IVerticalRulerInfo verticalRulerInfo) {
        
        return new RemoveJCoMarkerAction(Messages.getBundleForConstructedKeys(), "marker_remove_", textEditor, verticalRulerInfo); //$NON-NLS-1$
    }

}
