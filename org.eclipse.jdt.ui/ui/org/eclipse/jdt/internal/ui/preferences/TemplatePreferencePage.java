package org.eclipse.jdt.internal.ui.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.DialogPageContextComputer;
import org.eclipse.ui.help.WorkbenchHelp;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jdt.ui.text.JavaTextTools;

import org.eclipse.jdt.internal.corext.template.ContextType;
import org.eclipse.jdt.internal.corext.template.ContextTypeRegistry;
import org.eclipse.jdt.internal.corext.template.Template;
import org.eclipse.jdt.internal.corext.template.TemplateContext;
import org.eclipse.jdt.internal.corext.template.TemplateMessages;
import org.eclipse.jdt.internal.corext.template.TemplateSet;
import org.eclipse.jdt.internal.corext.template.Templates;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.template.TemplateContentProvider;
import org.eclipse.jdt.internal.ui.text.template.TemplateLabelProvider;
import org.eclipse.jdt.internal.ui.util.SWTUtil;

public class TemplatePreferencePage	extends PreferencePage implements IWorkbenchPreferencePage {

	// preference store keys
	private static final String PREF_FORMAT_TEMPLATES= JavaUI.ID_PLUGIN + ".template.format"; //$NON-NLS-1$

	private Templates fTemplates;

	private CheckboxTableViewer fTableViewer;
	private Button fAddButton;
	private Button fEditButton;
	private Button fImportButton;
	private Button fExportButton;
	private Button fExportAllButton;
	private Button fRemoveButton;
	private Button fEnableAllButton;
	private Button fDisableAllButton;

	private SourceViewer fPatternViewer;
	private Button fFormatButton;
	
	public TemplatePreferencePage() {
		super();
		
		setPreferenceStore(JavaPlugin.getDefault().getPreferenceStore());
		setDescription(TemplateMessages.getString("TemplatePreferencePage.message")); //$NON-NLS-1$

		fTemplates= Templates.getInstance();
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite ancestor) {	
		Composite parent= new Composite(ancestor, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		parent.setLayout(layout);				

		Table table= new Table(parent, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		
		GridData data= new GridData(GridData.FILL_BOTH);
		data.widthHint= convertWidthInCharsToPixels(80);
		data.heightHint= convertHeightInCharsToPixels(10);
		table.setLayoutData(data);
				
		table.setHeaderVisible(true);
		table.setLinesVisible(true);		

		TableLayout tableLayout= new TableLayout();
		table.setLayout(tableLayout);

		TableColumn column1= new TableColumn(table, SWT.NONE);		
		column1.setText(TemplateMessages.getString("TemplatePreferencePage.column.name")); //$NON-NLS-1$

		TableColumn column2= new TableColumn(table, SWT.NONE);
		column2.setText(TemplateMessages.getString("TemplatePreferencePage.column.context")); //$NON-NLS-1$
	
		TableColumn column3= new TableColumn(table, SWT.NONE);
		column3.setText(TemplateMessages.getString("TemplatePreferencePage.column.description")); //$NON-NLS-1$
		
		tableLayout.addColumnData(new ColumnWeightData(30));
		tableLayout.addColumnData(new ColumnWeightData(20));
		tableLayout.addColumnData(new ColumnWeightData(70));

		fTableViewer= new CheckboxTableViewer(table);		
		fTableViewer.setLabelProvider(new TemplateLabelProvider());
		fTableViewer.setContentProvider(new TemplateContentProvider());

		fTableViewer.setSorter(new ViewerSorter() {
			public int compare(Viewer viewer, Object object1, Object object2) {
				if ((object1 instanceof Template) && (object2 instanceof Template)) {
					Template left= (Template) object1;
					Template right= (Template) object2;
					int result= left.getName().compareToIgnoreCase(right.getName());
					if (result != 0)
						return result;
					return left.getDescription().compareToIgnoreCase(right.getDescription());
				}
				return super.compare(viewer, object1, object2);
			}
			
			public boolean isSorterProperty(Object element, String property) {
				return true;
			}
		});
		
		fTableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent e) {
				edit();
			}
		});
		
		fTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				selectionChanged1();
			}
		});

		fTableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				Template template= (Template) event.getElement();
				template.setEnabled(event.getChecked());
			}
		});

		Composite buttons= new Composite(parent, SWT.NULL);
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		buttons.setLayout(layout);
		
		fAddButton= new Button(buttons, SWT.PUSH);
		fAddButton.setLayoutData(getButtonGridData(fAddButton));
		fAddButton.setText(TemplateMessages.getString("TemplatePreferencePage.new")); //$NON-NLS-1$
		fAddButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				add();
			}
		});

		fEditButton= new Button(buttons, SWT.PUSH);
		fEditButton.setLayoutData(getButtonGridData(fEditButton));
		fEditButton.setText(TemplateMessages.getString("TemplatePreferencePage.edit")); //$NON-NLS-1$
		fEditButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				edit();
			}
		});

		fRemoveButton= new Button(buttons, SWT.PUSH);
		fRemoveButton.setLayoutData(getButtonGridData(fRemoveButton));
		fRemoveButton.setText(TemplateMessages.getString("TemplatePreferencePage.remove")); //$NON-NLS-1$
		fRemoveButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				remove();
			}
		});
				
		createSpacer(buttons);

		fImportButton= new Button(buttons, SWT.PUSH);
		fImportButton.setLayoutData(getButtonGridData(fImportButton));
		fImportButton.setText(TemplateMessages.getString("TemplatePreferencePage.import")); //$NON-NLS-1$
		fImportButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				import_();
			}
		});

		fExportButton= new Button(buttons, SWT.PUSH);
		fExportButton.setLayoutData(getButtonGridData(fExportButton));
		fExportButton.setText(TemplateMessages.getString("TemplatePreferencePage.export")); //$NON-NLS-1$
		fExportButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				export();
			}
		});

		fExportAllButton= new Button(buttons, SWT.PUSH);
		fExportAllButton.setLayoutData(getButtonGridData(fExportAllButton));
		fExportAllButton.setText(TemplateMessages.getString("TemplatePreferencePage.export.all")); //$NON-NLS-1$
		fExportAllButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				exportAll();
			}
		});		

		createSpacer(buttons);
		
		fEnableAllButton= new Button(buttons, SWT.PUSH);
		fEnableAllButton.setLayoutData(getButtonGridData(fEnableAllButton));
		fEnableAllButton.setText(TemplateMessages.getString("TemplatePreferencePage.enable.all")); //$NON-NLS-1$
		fEnableAllButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				enableAll(true);
			}
		});

		fDisableAllButton= new Button(buttons, SWT.PUSH);
		fDisableAllButton.setLayoutData(getButtonGridData(fDisableAllButton));
		fDisableAllButton.setText(TemplateMessages.getString("TemplatePreferencePage.disable.all")); //$NON-NLS-1$
		fDisableAllButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				enableAll(false);
			}
		});

		fPatternViewer= createViewer(parent);
		
		createSpacer(parent);

		fFormatButton= new Button(parent, SWT.CHECK);
		fFormatButton.setText(TemplateMessages.getString("TemplatePreferencePage.use.code.formatter")); //$NON-NLS-1$

		fTableViewer.setInput(fTemplates);		
		fTableViewer.setAllChecked(false);
		fTableViewer.setCheckedElements(getEnabledTemplates());		

		IPreferenceStore prefs= JavaPlugin.getDefault().getPreferenceStore();
		fFormatButton.setSelection(prefs.getBoolean(PREF_FORMAT_TEMPLATES));

		updateButtons();

		WorkbenchHelp.setHelp(parent, new DialogPageContextComputer(this, IJavaHelpContextIds.TEMPLATE_PREFERENCE_PAGE));
		
		return parent;
	}
	
	private Template[] getEnabledTemplates() {
		Template[] templates= fTemplates.getTemplates();
		
		List list= new ArrayList(templates.length);
		
		for (int i= 0; i != templates.length; i++)
			if (templates[i].isEnabled())
				list.add(templates[i]);
				
		return (Template[]) list.toArray(new Template[list.size()]);
	}
	
	private SourceViewer createViewer(Composite parent) {
		Label label= new Label(parent, SWT.NONE);
		label.setText("Preview:");
		GridData data= new GridData();
		data.horizontalSpan= 2;
		label.setLayoutData(data);
		
		SourceViewer viewer= new SourceViewer(parent, null, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		JavaTextTools tools= JavaPlugin.getDefault().getJavaTextTools();
		viewer.configure(new JavaSourceViewerConfiguration(tools, null));
		viewer.setEditable(false);
		viewer.setDocument(new Document());
		viewer.getTextWidget().setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
	
		Font font= JFaceResources.getFontRegistry().get(JFaceResources.TEXT_FONT);
		viewer.getTextWidget().setFont(font);
		
		Control control= viewer.getControl();
		data= new GridData(GridData.FILL_BOTH);
		data.heightHint= convertHeightInCharsToPixels(5);
		control.setLayoutData(data);
		
		return viewer;
	}
	
	public void createSpacer(Composite parent) {
		Label spacer= new Label(parent, SWT.NONE);
		GridData data= new GridData();
		data.horizontalAlignment= GridData.FILL;
		data.verticalAlignment= GridData.BEGINNING;
		data.heightHint= 4;		
		spacer.setLayoutData(data);
	}
	
	private static GridData getButtonGridData(Button button) {
		GridData data= new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint= SWTUtil.getButtonWidthHint(button);
		data.heightHint= SWTUtil.getButtonHeigthHint(button);
	
		return data;
	}
	
	private void selectionChanged1() {		
		IStructuredSelection selection= (IStructuredSelection) fTableViewer.getSelection();

		if (selection.size() == 1) {
			Template template= (Template) selection.getFirstElement();
			fPatternViewer.getTextWidget().setText(template.getPattern());
		} else {		
			fPatternViewer.getTextWidget().setText(""); //$NON-NLS-1$
		}
		
		updateButtons();
	}
	
	private void updateButtons() {
		int selectionCount= ((IStructuredSelection) fTableViewer.getSelection()).size();
		int itemCount= fTableViewer.getTable().getItemCount();
		
		fEditButton.setEnabled(selectionCount == 1);
		fExportButton.setEnabled(selectionCount > 0);
		fRemoveButton.setEnabled(selectionCount > 0 && selectionCount <= itemCount);
		fEnableAllButton.setEnabled(itemCount > 0);
		fDisableAllButton.setEnabled(itemCount > 0);
	}
	
	private void add() {		
		
		Template template= new Template();

		ContextTypeRegistry registry=ContextTypeRegistry.getInstance();
		Iterator iterator= registry.iterator();
		String contextTypeName= (String) iterator.next();
		template.setContext(contextTypeName); //$NON-NLS-1$

		EditTemplateDialog dialog= new EditTemplateDialog(getShell(), template, false);
		if (dialog.open() == dialog.OK) {
			fTemplates.add(template);
			fTableViewer.refresh();
			fTableViewer.setChecked(template, template.isEnabled());
			fTableViewer.setSelection(new StructuredSelection(template));			
		}
	}

	private void edit() {
		IStructuredSelection selection= (IStructuredSelection) fTableViewer.getSelection();

		Object[] objects= selection.toArray();		
		if ((objects == null) || (objects.length != 1))
			return;
		
		Template template= (Template) selection.getFirstElement();
		edit(template);
	}

	private void edit(Template template) {
		EditTemplateDialog dialog= new EditTemplateDialog(getShell(), template, true);
		if (dialog.open() == dialog.OK) {
			fTableViewer.refresh(template);
			fTableViewer.setChecked(template, template.isEnabled());
			fTableViewer.setSelection(new StructuredSelection(template));			
		}
	}
		
	private void import_() {
		FileDialog dialog= new FileDialog(getShell());
		dialog.setText(TemplateMessages.getString("TemplatePreferencePage.import.title")); //$NON-NLS-1$
		dialog.setFilterExtensions(new String[] {TemplateMessages.getString("TemplatePreferencePage.import.extension")}); //$NON-NLS-1$
		String path= dialog.open();
		
		if (path == null)
			return;
		
		try {
			fTemplates.addFromFile(new File(path));
			
			fTableViewer.refresh();
			fTableViewer.setAllChecked(false);
			fTableViewer.setCheckedElements(getEnabledTemplates());

		} catch (CoreException e) {			
			openReadErrorDialog(e);
		}
	}
	
	private void exportAll() {
		export(fTemplates);	
	}

	private void export() {
		IStructuredSelection selection= (IStructuredSelection) fTableViewer.getSelection();
		Object[] templates= selection.toArray();
		
		TemplateSet templateSet= new TemplateSet();
		for (int i= 0; i != templates.length; i++)
			templateSet.add((Template) templates[i]);
		
		export(templateSet);
	}
	
	private void export(TemplateSet templateSet) {
		FileDialog dialog= new FileDialog(getShell(), SWT.SAVE);
		dialog.setText(TemplateMessages.getFormattedString("TemplatePreferencePage.export.title", new Integer(templateSet.getTemplates().length))); //$NON-NLS-1$
		dialog.setFilterExtensions(new String[] {TemplateMessages.getString("TemplatePreferencePage.export.extension")}); //$NON-NLS-1$
		dialog.setFileName(TemplateMessages.getString("TemplatePreferencePage.export.filename")); //$NON-NLS-1$
		String path= dialog.open();
		
		if (path == null)
			return;
		
		try {
			templateSet.saveToFile(new File(path));			
		} catch (CoreException e) {			
			JavaPlugin.log(e);
			openWriteErrorDialog(e);
		}		
	}
	
	
	private void remove() {
		IStructuredSelection selection= (IStructuredSelection) fTableViewer.getSelection();

		Iterator elements= selection.iterator();
		while (elements.hasNext()) {
			Template template= (Template) elements.next();
			fTemplates.remove(template);
		}

		fTableViewer.refresh();
	}
	
	private void enableAll(boolean enable) {
		Template[] templates= fTemplates.getTemplates();
		for (int i= 0; i != templates.length; i++)
			templates[i].setEnabled(enable);		
			
		fTableViewer.setAllChecked(enable);
	}
	
	/*
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {}

	/*
	 * @see Control#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible)
			setTitle(TemplateMessages.getString("TemplatePreferencePage.title")); //$NON-NLS-1$
	}
	
	/*
	 * @see PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		IPreferenceStore prefs= JavaPlugin.getDefault().getPreferenceStore();
		fFormatButton.setSelection(prefs.getDefaultBoolean(PREF_FORMAT_TEMPLATES));

		try {
			fTemplates.restoreDefaults();
		} catch (CoreException e) {
			JavaPlugin.log(e);
			openReadErrorDialog(e);
		}
		
		// refresh
		fTableViewer.refresh();
		fTableViewer.setAllChecked(false);
		fTableViewer.setCheckedElements(getEnabledTemplates());		
	}

	/*
	 * @see PreferencePage#performOk()
	 */	
	public boolean performOk() {
		IPreferenceStore prefs= JavaPlugin.getDefault().getPreferenceStore();
		prefs.setValue(PREF_FORMAT_TEMPLATES, fFormatButton.getSelection());

		try {
			fTemplates.save();
		} catch (CoreException e) {
			JavaPlugin.log(e);
			openWriteErrorDialog(e);
		}
		
		return super.performOk();
	}	
	
	/*
	 * @see PreferencePage#performCancel()
	 */
	public boolean performCancel() {
		try {
			fTemplates.reset();			
		} catch (CoreException e) {
			JavaPlugin.log(e);
			openReadErrorDialog(e);
		}

		return super.performCancel();
	}
	
	/**
	 * Initializes the default values of this page in the preference bundle.
	 * Will be called on startup of the JavaPlugin
	 */
	public static void initDefaults(IPreferenceStore prefs) {
		prefs.setDefault(PREF_FORMAT_TEMPLATES, true);
	}

	public static boolean useCodeFormatter() {
		IPreferenceStore prefs= JavaPlugin.getDefault().getPreferenceStore();
		return prefs.getBoolean(PREF_FORMAT_TEMPLATES);
	}

	private void openReadErrorDialog(CoreException e) {
		ErrorDialog.openError(getShell(),
			TemplateMessages.getString("TemplatePreferencePage.error.read.title"), //$NON-NLS-1$
			null, e.getStatus());
	}
	
	private void openWriteErrorDialog(CoreException e) {
		ErrorDialog.openError(getShell(),
			TemplateMessages.getString("TemplatePreferencePage.error.write.title"), //$NON-NLS-1$
			null, e.getStatus());		
	}
		
}
