package com.huatek.itee.scanner.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.*;

import com.huatek.itee.scanner.engine.ContentInfo;
import com.huatek.itee.scanner.engine.FileInfo;
import com.huatek.itee.scanner.engine.Scanner;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;

import javax.inject.Inject;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class ScannerView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.huatek.itee.scanner.views.ScannerView";

	@Inject
	IWorkbench workbench;

	private TableViewer lViewer;
	private TableViewer rViewer;
	private Action scanConfig;
	private Action runnScanner;
	private Action doubleClickAction;
	private Composite fParent;
	private PageBook fPagebook;
	private SashForm fSplitter;

	private Color[] bg = new Color[] { new Color(null, 255, 255, 255), new Color(null, 247, 247, 240) };
	private Color[] fore = new Color[] { new Color(null, 0, 0, 0), new Color(null, 0, 0, 0) };

	class ContLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider {

		private Object current = null;
		private int currentColor = 0;

		@Override
		public String getColumnText(Object obj, int index) {
			if (index == 0) {
				ContentInfo info = (ContentInfo) obj;
				return info.getContent(true);
			} else if (index == 1) {
				ContentInfo info = (ContentInfo) obj;
				return String.valueOf(info.getFileInfos().size());
			}
			return getText(obj);
		}

		@Override
		public Image getColumnImage(Object obj, int index) {
			if (index == 0) {
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
			}
			return null;
		}

		@Override
		public Color getForeground(Object element, int columnIndex) {
//			return fore[columnIndex % 2];
			return fore[currentColor];
		}

		@Override
		public Color getBackground(Object element, int columnIndex) {
//			return bg[columnIndex % 2];
			if (current != element) {
				currentColor = 1 - currentColor;
				current = element;
			}
			return bg[currentColor];
		}
	}

	class DetailLabelProvider extends LabelProvider implements ITableLabelProvider {

		@Override
		public String getColumnText(Object obj, int index) {
			if (index == 0) {
				FileInfo info = (FileInfo) obj;
				return info.getFileName();
			} else if (index == 1) {
				FileInfo info = (FileInfo) obj;
				return String.valueOf(info.getLineNum());
			}
			return getText(obj);
		}

		@Override
		public Image getColumnImage(Object obj, int index) {
			if (index == 0) {
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_FORWARD);
			}
			return null;
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		fParent = parent;
		addResizeListener(parent);
		fPagebook = new PageBook(parent, 0);
		fPagebook.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		fSplitter = new SashForm(fPagebook, SWT.HORIZONTAL | SWT.NULL);
		fSplitter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//		fSplitter.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_RED));

		fPagebook.showPage(fSplitter);

		lViewer = new TableViewer(fSplitter, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		createLeftColumns();
		Table table = lViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		lViewer.setContentProvider(ArrayContentProvider.getInstance());
		lViewer.setLabelProvider(new ContLabelProvider());

		rViewer = new TableViewer(fSplitter, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		createRightColumns();
		Table rTable = rViewer.getTable();
		rTable.setHeaderVisible(true);
		rTable.setLinesVisible(true);
		rViewer.setContentProvider(ArrayContentProvider.getInstance());
		rViewer.setLabelProvider(new DetailLabelProvider());

		// Create the help context id for the viewer's control
		workbench.getHelpSystem().setHelp(lViewer.getControl(), "com.huatek.itee.scanner.viewer");
		workbench.getHelpSystem().setHelp(rViewer.getControl(), "com.huatek.itee.scanner.detailViewer");
		getSite().setSelectionProvider(lViewer);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		hookSelectionChangeAction();
		contributeToActionBars();
	}

	private void addResizeListener(Composite parent) {
		parent.addControlListener(new ControlListener() {
			@Override
			public void controlMoved(ControlEvent e) {
				// TODO
			}

			@Override
			public void controlResized(ControlEvent e) {
//				computeOrientation();
			}
		});
	}

	private void createLeftColumns() {
		String[] titles = { "sentence", "times" };
		int[] bounds = { 500, 30 };

		// 1 col
		TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0], 0);
		col.setLabelProvider(new ColumnLabelProvider());

		// 2 times
		col = createTableViewerColumn(titles[1], bounds[1], 1);
		col.setLabelProvider(new ColumnLabelProvider());
	}

	private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(lViewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}

	private void createRightColumns() {
		String[] titles = { "class", "line" };
		int[] bounds = { 400, 30 };

		// 1 file name
		TableViewerColumn col = createDetailColumn(titles[0], bounds[0], 0);
		col.setLabelProvider(new ColumnLabelProvider());
//			@Override
//			public String getText(Object element) {
//				FileInfo info = (FileInfo) element;
//				return info.getFileName();
//			}

		// 2 line
		col = createDetailColumn(titles[1], bounds[1], 1);
		col.setLabelProvider(new ColumnLabelProvider());
	}

	private TableViewerColumn createDetailColumn(String title, int bound, final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(rViewer, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				ScannerView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(lViewer.getControl());
		lViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, lViewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(scanConfig);
		manager.add(new Separator());
		manager.add(runnScanner);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(scanConfig);
		manager.add(runnScanner);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(scanConfig);
		manager.add(runnScanner);
	}

	private void makeActions() {
		scanConfig = new Action() {
			@Override
			public void run() {
				showMessage("Action 1 executed");
			}
		};
		scanConfig.setText("Config Scanner");
		scanConfig.setToolTipText("configuration for scanner");
		scanConfig.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

		runnScanner = new Action() {
			@Override
			public void run() {
				Scanner.getInstance().getScanJob().schedule();
				Scanner.getInstance().getScanJob().addJobChangeListener(new JobChangeAdapter() {
					@Override
					public void done(IJobChangeEvent event) {
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								if (lViewer.getControl().isDisposed()) {
									return;
								}
								lViewer.setInput(Scanner.getInstance().getContentInfo());
							}
						});
					}
				});
			}
		};
		runnScanner.setText("Run Scanner");
		runnScanner.setToolTipText("execute scanner");
		runnScanner.setImageDescriptor(workbench.getSharedImages().getImageDescriptor(ISharedImages.IMG_ELCL_SYNCED));
		doubleClickAction = new Action() {
			@Override
			public void run() {
				IStructuredSelection selection = lViewer.getStructuredSelection();
				Object obj = selection.getFirstElement();
				showMessage("Double-click detected on " + obj.toString());
			}
		};
	}

	private void hookDoubleClickAction() {
		lViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	private void hookSelectionChangeAction() {
		lViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (lViewer.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection sel = (IStructuredSelection) lViewer.getSelection();
					if (sel.getFirstElement() instanceof ContentInfo) {
						ContentInfo ci = (ContentInfo) sel.getFirstElement();
						if (rViewer.getControl().isDisposed()) {
							return;
						}
						rViewer.setInput(ci.getFileInfos());
					}
				}
			}
		});
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(lViewer.getControl().getShell(), "Scanner", message);
	}

	@Override
	public void setFocus() {
		fPagebook.setFocus();
//        lViewer.getControl().setFocus();
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		if (runnScanner != null) {
			runnScanner = null;
		}
		if (doubleClickAction != null) {
			doubleClickAction = null;
		}
		if (scanConfig != null) {
			scanConfig = null;
		}
		super.dispose();
	}
}
