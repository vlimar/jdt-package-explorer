/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.ui.wizards.dialogfields;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jdt.internal.ui.wizards.swt.MGridData;

/**
 * Dialog field describing a separator.
 */
public class Separator extends DialogField {
	
	private Label fSeparator;
	private int fStyle;
	
	public Separator() {
		this(SWT.NONE);
	}	
	
	/**
	 * @param style of the separator. See <code>Label</code> for possible
	 * styles.
	 */
	public Separator(int style) {
		super();
		fStyle= style;
	}
			
	// ------- layout helpers

	/**
	 * Creates the separator and fills it in a MGridLayout.
	 * @param height The heigth of the separator
	 */		
	public Control[] doFillIntoGrid(Composite parent, int nColumns, int height) {
		assertEnoughColumns(nColumns);
		
		Control separator= getSeparator(parent);
		separator.setLayoutData(gridDataForSeperator(nColumns, height));
		
		return new Control[] { separator };
	}

	/*
	 * @see DialogField#doFillIntoGrid
	 */	
	public Control[] doFillIntoGrid(Composite parent, int nColumns) {
		return doFillIntoGrid(parent, nColumns, 4);
	}

	/*
	 * @see DialogField#getNumberOfControls
	 */	
	public int getNumberOfControls() {
		return 1;	
	}
	
	protected static MGridData gridDataForSeperator(int span, int height) {
		MGridData gd= new MGridData();
		gd.horizontalAlignment= gd.FILL;
		gd.verticalAlignment= gd.BEGINNING;
		gd.heightHint= height;		
		gd.horizontalSpan= span;
		return gd;
	}
	
	// ------- ui creation	

	/**
	 * Creates or returns the created separator.
	 * @param parent The parent composite or <code>null</code> if the widget has
	 * already been created.
	 */	
	public Control getSeparator(Composite parent) {
		if (fSeparator == null) {
			assertCompositeNotNull(parent);
			fSeparator= new Label(parent, fStyle);
		}	
		return fSeparator;
	}

}