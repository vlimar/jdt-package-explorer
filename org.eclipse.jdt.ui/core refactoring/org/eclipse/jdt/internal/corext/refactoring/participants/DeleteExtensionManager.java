/*******************************************************************************
 * Copyright (c) 2003 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.corext.refactoring.participants;

import org.eclipse.core.runtime.CoreException;

public class DeleteExtensionManager {
	
	private static final String PROCESSOR_EXT_POINT= "deleteProcessors"; //$NON-NLS-1$
	private static final String PARTICIPANT_EXT_POINT= "deleteParticipants"; //$NON-NLS-1$
	
	private static ExtensionManager fInstance= new ExtensionManager("Delete", PROCESSOR_EXT_POINT, PARTICIPANT_EXT_POINT); //$NON-NLS-1$
	
	public static IDeleteProcessor getProcessor(Object element) throws CoreException {
		return (IDeleteProcessor)fInstance.getProcessor(element);
	}
	
	public static IDeleteParticipant[] getParticipants(IRefactoringProcessor processor) throws CoreException {
		return getParticipants(processor, processor.getDerivedElements());		
	}		

	public static IDeleteParticipant[] getParticipants(IRefactoringProcessor processor, Object[] elements) throws CoreException {
		IRefactoringParticipant[] participants= fInstance.getParticipants(processor, elements);
		IDeleteParticipant[] result= new IDeleteParticipant[participants.length];
		System.arraycopy(participants, 0, result, 0, participants.length);
		return result;
	}
}
