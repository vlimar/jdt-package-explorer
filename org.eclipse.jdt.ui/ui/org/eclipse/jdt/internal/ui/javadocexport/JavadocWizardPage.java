/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.ui.javadocexport;

import java.io.File;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jdt.ui.wizards.NewElementWizardPage;

public abstract class JavadocWizardPage extends NewElementWizardPage {

	protected JavadocWizardPage(String pageName) {
		super(pageName);
		setTitle(JavadocExportMessages.getString("JavadocWizardPage.javadocwizardpage.description")); //$NON-NLS-1$
	}

	protected Button createButton(Composite composite, int style, String message, GridData gd) {
		Button button= new Button(composite, style);
		button.setText(message);
		button.setLayoutData(gd);
		return button;
	}

	protected GridLayout createGridLayout(int columns) {
		GridLayout gl= new GridLayout();
		gl.numColumns= columns;
		return gl;
	}

	protected GridData createGridData(int flag, int hspan, int vspan, int indent) {
		GridData gd= new GridData(flag);
		gd.horizontalIndent= indent;
		gd.horizontalSpan= hspan;
		gd.verticalSpan= vspan;
		return gd;
	}

	protected GridData createGridData(int flag, int hspan, int indent) {
		GridData gd= new GridData(flag);
		gd.horizontalIndent= indent;
		gd.horizontalSpan= hspan;
		return gd;
	}

	protected GridData createGridData(int hspan) {
		GridData gd= new GridData();
		gd.horizontalSpan= hspan;
		return gd;
	}

	protected Label createLabel(Composite composite, int style, String message, GridData gd) {
		Label label= new Label(composite, style);
		label.setText(message);
		label.setLayoutData(gd);
		return label;
	}

	protected Text createText(Composite composite, int style, String message, GridData gd) {
		Text text= new Text(composite, style);
		if (message != null)
			text.setText(message);
		text.setLayoutData(gd);
		return text;
	}

	protected void handleFileBrowseButtonPressed(Text text, String[] extensions, String title) {
		FileDialog dialog= new FileDialog(text.getShell());
		dialog.setText(title);
		dialog.setFilterExtensions(extensions);
		String dirName= text.getText();
		if (!dirName.equals("")) { //$NON-NLS-1$
			File path= new File(dirName);
			if (path.exists())
				dialog.setFilterPath(dirName);

		}
		String selectedDirectory= dialog.open();
		if (selectedDirectory != null)
			text.setText(selectedDirectory);
	}

	protected String handleFolderBrowseButtonPressed(String text, Shell shell, String title, String message) {
		
		DirectoryDialog dialog= new DirectoryDialog(shell);
		dialog.setFilterPath(text);
		dialog.setText(title);
		dialog.setMessage(message);
		String res= dialog.open();
		if (res != null) {
			File file= new File(res);
			if (file.exists() && file.isDirectory())
				return res;
		}
		return text;
	}

	protected static class EnableSelectionAdapter extends SelectionAdapter {
		private Control[] fEnable;
		private Control[] fDisable;
		private boolean single;

		protected EnableSelectionAdapter(Control[] enable, Control[] disable) {
			super();
			fEnable= enable;
			fDisable= disable;
		}

		public void widgetSelected(SelectionEvent e) {
			for (int i= 0; i < fEnable.length; i++) {
				((Control) fEnable[i]).setEnabled(true);
			}
			for (int i= 0; i < fDisable.length; i++) {
				((Control) fDisable[i]).setEnabled(false);
			}
			validate();
		}
		//copied from  WizardNewProjectCreationPage
		public void validate() {
		}

	} //end class EnableSelectionAdapter

	protected static class ToggleSelectionAdapter extends SelectionAdapter {
		Control[] controls;

		protected ToggleSelectionAdapter(Control[] controls) {
			this.controls= controls;
		}

		public void widgetSelected(SelectionEvent e) {

			for (int i= 0; i < controls.length; i++) {
				Control control= controls[i];
				control.setEnabled(!control.getEnabled());
			}
			validate();
		}

		public void validate() {
		}

	} //end class ToggleSelection Adapter

}