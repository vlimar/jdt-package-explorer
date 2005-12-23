/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.model;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.compare.CompareUI;

/**
 * Viewer which displays an overview of a refactoring descriptor input.
 * 
 * @since 3.2
 */
public final class RefactoringDescriptorCompareViewer extends RefactoringDescriptorViewer {

	/**
	 * Creates a new refactoring descriptor compare viewer.
	 * 
	 * @param parent
	 *            the parent control
	 * @param style
	 *            the style
	 */
	public RefactoringDescriptorCompareViewer(final Composite parent, final int style) {
		super(parent, style);
		fBrowser.setData(CompareUI.COMPARE_VIEWER_TITLE, ModelMessages.RefactoringDescriptorCompareInput_pending_refactoring);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setInput(final Object input) {
		if (input instanceof RefactoringDescriptorCompareInput)
			super.setInput(((RefactoringDescriptorCompareInput) input).getDescriptor());
		super.setInput(input);
	}
}