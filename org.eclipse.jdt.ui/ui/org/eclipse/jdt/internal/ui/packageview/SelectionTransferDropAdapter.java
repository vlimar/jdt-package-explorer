/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.ui.packageview;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.ui.texteditor.IDocumentProvider;

import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.internal.corext.refactoring.Assert;
import org.eclipse.jdt.internal.corext.refactoring.reorg.CopyRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.reorg.MoveRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.reorg.ReorgRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.text.ITextBufferChangeCreator;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.actions.StructuredSelectionProvider;
import org.eclipse.jdt.internal.ui.dnd.JdtViewerDropAdapter;
import org.eclipse.jdt.internal.ui.dnd.LocalSelectionTransfer;
import org.eclipse.jdt.internal.ui.dnd.TransferDropTargetListener;
import org.eclipse.jdt.internal.ui.refactoring.changes.DocumentTextBufferChangeCreator;
import org.eclipse.jdt.internal.ui.reorg.CopyAction;
import org.eclipse.jdt.internal.ui.reorg.MoveAction;
import org.eclipse.jdt.internal.ui.util.ExceptionHandler;


public class SelectionTransferDropAdapter extends JdtViewerDropAdapter implements TransferDropTargetListener {

	private List fElements;
	private MoveRefactoring fMoveRefactoring;
	private int fCanMoveElements;
	private CopyRefactoring fCopyRefactoring;
	private int fCanCopyElements;

	public SelectionTransferDropAdapter(AbstractTreeViewer viewer) {
		super(viewer, DND.FEEDBACK_SCROLL | DND.FEEDBACK_EXPAND);
	}

	//---- TransferDropTargetListener interface ---------------------------------------
	
	public Transfer getTransfer() {
		return LocalSelectionTransfer.getInstance();
	}

	//---- Actual DND -----------------------------------------------------------------
	
	public void dragEnter(DropTargetEvent event) {
		clear();
		super.dragEnter(event);
	}
	
	public void dragLeave(DropTargetEvent event) {
		clear();
		super.dragLeave(event);
	}
	
	private void clear() {
		fElements= null;
		fMoveRefactoring= null;
		fCanMoveElements= 0;
		fCopyRefactoring= null;
		fCanCopyElements= 0;
	}
	
	public void validateDrop(Object target, DropTargetEvent event, int operation) {
		event.detail= DND.DROP_NONE;
		if (fElements == null) {
			ISelection s= LocalSelectionTransfer.getInstance().getSelection();
			if (!(s instanceof IStructuredSelection))
				return;
			fElements= ((IStructuredSelection)s).toList();
		}	

		boolean success= false;

		try {
			if (operation == DND.DROP_COPY) {
				success= handleValidateCopy(target, event);
			} else if (operation == DND.DROP_MOVE) {
				success= handleValidateMove(target, event);
			}
		} catch (JavaModelException e){
			ExceptionHandler.handle(e, "Drag'n'drop", "Unexpected exception. See log for details.");
			success= false;
		}	
		
		if (success)
			event.detail= operation;
	}	

	public void drop(Object target, DropTargetEvent event) {
		try{
			if (event.detail == DND.DROP_MOVE) {
				handleDropMove(target, event);
			} else if (event.detail == DND.DROP_COPY) {
				handleDropCopy(target, event);
			}
			
			// The drag source listener must not perform any operation
			// since this drop adapter did the remove of the source even
			// if we moved something.
			event.detail= DND.DROP_NONE;
		} catch (JavaModelException e){
			ExceptionHandler.handle(e, "Drag'n'drop", "Unexpected exception. See log for details.");
		}	
	}
	
	private boolean handleValidateMove(Object target, DropTargetEvent event) throws JavaModelException{
		if (fMoveRefactoring == null){
			IDocumentProvider documentProvider= JavaPlugin.getDefault().getCompilationUnitDocumentProvider();
			ITextBufferChangeCreator changeCreator= new DocumentTextBufferChangeCreator(documentProvider);
			fMoveRefactoring= new MoveRefactoring(fElements, changeCreator);
		}	
		
		if (!canMoveElements())
			return false;	
		
		return fMoveRefactoring.isValidDestination(target);
	}
	
	private boolean canMoveElements() {
		if (fCanMoveElements == 0) {
			fCanMoveElements= 2;
			if (! canActivate(fMoveRefactoring))
				fCanMoveElements= 1;
		}
		return fCanMoveElements == 2;
	}
	
	private boolean canActivate(ReorgRefactoring ref){
		try{
			return ref.checkActivation(new NullProgressMonitor()).isOK();
		} catch(JavaModelException e){
			ExceptionHandler.handle(e, "Drag'n'drop", "Unexpected exception. See log for details.");
			return false;
		}
	}
	
	private void handleDropMove(final Object target, DropTargetEvent event) throws JavaModelException{
		new DragNDropMoveAction(StructuredSelectionProvider.createFrom(getViewer()), target).run();
	}
	
	private boolean handleValidateCopy(Object target, DropTargetEvent event) throws JavaModelException{
		if (fCopyRefactoring == null)
			fCopyRefactoring= new CopyRefactoring(fElements);
		
		if (!canCopyElements())
			return false;	

		return fCopyRefactoring.isValidDestination(target);
	}
			
	private boolean canCopyElements() {
		if (fCanCopyElements == 0) {
			fCanCopyElements= 2;
			if (!canActivate(fCopyRefactoring))
				fCanCopyElements= 1;
		}
		return fCanCopyElements == 2;
	}		
	
	private void handleDropCopy(final Object target, DropTargetEvent event) throws JavaModelException{
		CopyAction action= new CopyAction("#COPY", StructuredSelectionProvider.createFrom(getViewer())){
			protected Object selectDestination(ReorgRefactoring ref) {
				return target;
			}
		};
		action.run();
	}
	
	//--
	private static class DragNDropMoveAction extends MoveAction{
		private Object fTarget;
		private static final int PREVIEW_ID= IDialogConstants.CLIENT_ID + 1;
		
		public DragNDropMoveAction(StructuredSelectionProvider provider, Object target){
			super("#MOVE", provider);
			Assert.isNotNull(target);
			fTarget= target;
		}
		
		protected Object selectDestination(ReorgRefactoring ref) {
			return fTarget;
		}
		
		protected boolean isOkToProceed(ReorgRefactoring refactoring) throws JavaModelException{
			if (!super.isOkToProceed(refactoring))
				return false;
			return askIfUpdateReferences((MoveRefactoring)refactoring);
		}
		
		//returns false iff canceled or error
		private boolean askIfUpdateReferences(MoveRefactoring ref) throws JavaModelException{
			if (! ref.canUpdateReferences()){
				setShowPreview(false);
				return true;
			}	
			switch (askIfUpdateReferences()){
				case IDialogConstants.CANCEL_ID:
				   setShowPreview(false);
					return false;
				case IDialogConstants.NO_ID:
					ref.setUpdateReferences(false);
					setShowPreview(false);
					return true;	
				case IDialogConstants.YES_ID:
					ref.setUpdateReferences(true);
					setShowPreview(false);
					return true;
				case PREVIEW_ID:		 
					ref.setUpdateReferences(true);
					setShowPreview(true);
					return true;
				default: 
					Assert.isTrue(false); //not expected to get here
					return false;
			}
		}
		
		private static int askIfUpdateReferences(){
			Shell shell= JavaPlugin.getActiveWorkbenchShell().getShell();
			String title= "Move";
			String preview= "Pre&view";
			String question= "Do you want to update references to the moved element(s)? Press '" + preview + "' to see the preview of the reference updates.";
			
			String[] labels= new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL,
																 preview,  IDialogConstants.CANCEL_LABEL };
			final MessageDialog dialog = new MessageDialog(shell, title, null, question, MessageDialog.QUESTION,	labels, 2); //preview is default
			shell.getDisplay().syncExec(new Runnable() {
				public void run() {
					dialog.open();
				}
			});
			int result = dialog.getReturnCode();
			if (result == 0)
				return IDialogConstants.YES_ID;
			if (result == 1)
				return IDialogConstants.NO_ID;
			if (result == 2)
				return PREVIEW_ID;
			return IDialogConstants.CANCEL_ID;
		}
	}
}