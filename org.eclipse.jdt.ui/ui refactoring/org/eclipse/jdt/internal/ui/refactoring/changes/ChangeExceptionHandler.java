/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

package org.eclipse.jdt.internal.ui.refactoring.changes;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.util.Assert;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.jdt.internal.corext.refactoring.base.ChangeAbortException;
import org.eclipse.jdt.internal.corext.refactoring.base.ChangeContext;
import org.eclipse.jdt.internal.corext.refactoring.base.IChange;
import org.eclipse.jdt.internal.corext.refactoring.base.IChangeExceptionHandler;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.refactoring.RefactoringMessages;

/**
 * An implementation of <code>IChangeExceptionHandler</code> which pops up a dialog
 * box asking the user if the refactoring is to be aborted without further actions or
 * if the refactoring engine should try to undo all successfully executed changes.
 */
public class ChangeExceptionHandler implements IChangeExceptionHandler {
	
	private Shell fParent;
	
	private static class RefactorErrorDialog extends ErrorDialog {
		public RefactorErrorDialog(Shell parentShell, String dialogTitle, String message, IStatus status, int displayMask) {
			super(parentShell, dialogTitle, message, status, displayMask);
		}
		protected void createButtonsForButtonBar(Composite parent) {
			super.createButtonsForButtonBar(parent);
			Button ok= getButton(IDialogConstants.OK_ID);
			ok.setText( RefactoringMessages.getString("ChangeExceptionHandler.undo")); //$NON-NLS-1$
			Button abort= createButton(parent, IDialogConstants.CANCEL_ID, RefactoringMessages.getString("ChangeExceptionHandler.abort"), true); //$NON-NLS-1$
			abort.moveBelow(ok);
			abort.setFocus();
		}
		protected Control createMessageArea (Composite parent) {
			Control result= super.createMessageArea(parent);
			new Label(parent, SWT.NONE); // filler
			Label label= new Label(parent, SWT.NONE);
			label.setText(RefactoringMessages.getString("ChangeExceptionHandler.button_explanation")); //$NON-NLS-1$
			label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			return result;
		}
	}
	
	public ChangeExceptionHandler(Shell parent) {
		Assert.isNotNull(parent);
		fParent= parent;
	}
	
	public void handle(ChangeContext context, IChange change, Exception e) {
		JavaPlugin.log(e);
		IStatus status= null;
		if (e instanceof CoreException) {
			status= ((CoreException)e).getStatus();
		} else {
			status= new Status(IStatus.ERROR, JavaPlugin.getPluginId(), IStatus.ERROR,
				e.getMessage(), e);
		}
		final ErrorDialog dialog= new RefactorErrorDialog(fParent,
			RefactoringMessages.getString("ChangeExceptionHandler.refactoring"), //$NON-NLS-1$
			RefactoringMessages.getFormattedString("ChangeExceptionHandler.unexpected_exception", new String[] {change.getName()}), //$NON-NLS-1$
			status, IStatus.OK | IStatus.INFO | IStatus.WARNING | IStatus.ERROR); 
		
		final int[] result= new int[1];	
		Runnable runnable= new Runnable() {
			public void run() {
				result[0]= dialog.open();
			}
		};
		fParent.getDisplay().syncExec(runnable);
		switch(result[0]) {
			case IDialogConstants.OK_ID:
				context.setTryToUndo();
				// Fall through
			case IDialogConstants.CANCEL_ID:
				throw new ChangeAbortException(e);
		}
	}
}
