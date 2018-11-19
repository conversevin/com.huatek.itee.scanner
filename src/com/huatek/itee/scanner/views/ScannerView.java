package com.huatek.itee.scanner.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.*;

import com.huatek.itee.scanner.engine.ContentInfo;
import com.huatek.itee.scanner.engine.Scanner;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.SWT;
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

	private TableViewer viewer;
	private Action scanConfig;
	private Action runnScanner;
	private Action doubleClickAction;

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		private Color[] bg = new Color[] { new Color(null, 255, 255, 255), new Color(null, 247, 247, 240) };
		private Color[] fore = new Color[] { new Color(null, 0, 0, 0), new Color(null, 0, 0, 0) };

		@Override
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}

		@Override
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}

		@Override
		public Image getImage(Object obj) {
			return workbench.getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}

		public Color getForeground(Object element, int columnIndex) {
			return fore[columnIndex % 2];
		}

		public Color getBackground(Object element, int columnIndex) {
			return bg[columnIndex % 2];
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(new ViewLabelProvider());
		
		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		
		createColumns(parent);
		

		// Create the help context id for the viewer's control
		workbench.getHelpSystem().setHelp(viewer.getControl(), "com.huatek.itee.scanner.viewer");
		getSite().setSelectionProvider(viewer);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void createColumns(final Composite parent) {
		String[] titles = { "sentance", "times", "class", "line" };
		int[] bounds = { 300, 30, 300, 30 };

		// 1 col
		TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0], 0);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ContentInfo info = (ContentInfo) element;
				return info.getContent();
			}
		});

		// 2 times
		col = createTableViewerColumn(titles[1], bounds[1], 1);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ContentInfo info = (ContentInfo) element;
				return String.valueOf(info.getFileInfos().size());
			}
		});

		// 3 class
		col = createTableViewerColumn(titles[2], bounds[2], 2);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ContentInfo info = (ContentInfo) element;
				return info.getFileInfos().get(0).getLocation();
			}
		});

		// 4 line
		col = createTableViewerColumn(titles[3], bounds[3], 3);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ContentInfo info = (ContentInfo) element;
				return String.valueOf(info.getFileInfos().get(0).getLineNum());
			}
		});
	}
	
	private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
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
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
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
								viewer.setInput(Scanner.getInstance().getContentInfo());
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
				IStructuredSelection selection = viewer.getStructuredSelection();
				Object obj = selection.getFirstElement();
				showMessage("Double-click detected on " + obj.toString());
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(viewer.getControl().getShell(), "Scanner", message);
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}
