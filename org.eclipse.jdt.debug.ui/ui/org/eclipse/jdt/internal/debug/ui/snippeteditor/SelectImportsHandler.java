/*******************************************************************************
 * Copyright (c) Dec 31, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui.snippeteditor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Default handler for the Set Imports action in the snippet editor used in 
 * the scrapbook page editor
 * 
 * @since 3.6.200
 */
public class SelectImportsHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart part = HandlerUtil.getActiveEditor(event);
		if(part instanceof JavaSnippetEditor) {
			JavaSnippetEditor editor = (JavaSnippetEditor) part;
			if (!editor.isInJavaProject()) {
				editor.reportNotInJavaProjectError();
			}
			else {
				String[] imports = editor.getImports();
				Dialog dialog = new SelectImportsDialog(editor, imports);
				dialog.open();
			}
		}
		return null;
	}
}