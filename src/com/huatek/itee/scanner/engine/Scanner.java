package com.huatek.itee.scanner.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

public class Scanner {

	private static Scanner INSTANCE = null;

	/**
	 * refreshing exclusive rule
	 */
	public static class ExclusiveScanJobRule implements ISchedulingRule {

		@Override
		public boolean contains(ISchedulingRule rule) {
			return rule == ExclusiveScanJobRule.this;
		}

		@Override
		public boolean isConflicting(ISchedulingRule rule) {
			return rule instanceof ExclusiveScanJobRule;
		}
	}

	private Job scanJob = null;

	private ScannerConfigure sConfig;

	private ArrayList<ContentInfo> contentInfo;

	public Scanner() {
		super();
		sConfig = new ScannerConfigure();
		contentInfo = new ArrayList<ContentInfo>();
		addScanJob();
	}

	private void addScanJob() {
		scanJob = new Job("Scanner job running") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				execute();
				return Status.OK_STATUS;
			}
		};
		scanJob.setRule(new ExclusiveScanJobRule());
	}

	/**
	 * @return Returns the INSTANCE.
	 */
	public static synchronized Scanner getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new Scanner();
		}
		return INSTANCE;
	}

	public boolean execute() {
		boolean success = true;
		sConfig.update();
		contentInfo.clear();
		if (sConfig.getProjects() != null) {
			if (sConfig.getProjects().isEmpty()) {
				System.out.println("no project in configuration");
			} else {
				for (IProject proj : sConfig.getProjects()) {
					String path = proj.getLocation().toOSString() + "/src/";
					traverseFolder(path);
				}
			}
		} else {
			System.out.println("not specified any project");
		}
		// TODO execution body
		return success;
	}

	public void traverseFolder(String path) {
		File dir = new File(path);
		if (dir.exists()) {
			File[] files = dir.listFiles();
			if (files.length == 0) {
				return;
			}
			for (File file : files) {
				if (file.isDirectory()) {
					traverseFolder(file.getAbsolutePath());
				} else {
					if (sConfig.isTypeMatch(file.getName())) {
						System.out.println("processing java: " + file.getAbsolutePath());
						readFileByLines(file);
					}
				}
			}
		} else {
			System.out.println("file dosen't exist");
		}
	}

	public void readFileByLines(File file) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			int line = 1;
			while ((tempString = reader.readLine()) != null) {
				if (FileInfo.isValidLine(tempString)) {
					System.out.println("== line " + line + ": " + tempString);
					FileInfo info = new FileInfo(line, file.getCanonicalPath());
					ArrayList<FileInfo> list = getFileInfo(tempString);
					if (list == null) {
						ArrayList<FileInfo> conts = new ArrayList<FileInfo>();
						conts.add(info);
						contentInfo.add(new ContentInfo(tempString, conts));
					} else {
						list.add(info);
					}
				}
				line++;
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	private ArrayList<FileInfo> getFileInfo(String content) {
		for (ContentInfo info : contentInfo) {
			if (info.getContent().equals(content)) {
				return info.getFileInfos();
			}
		}
		return null;
	}

	public ScannerConfigure getsConfig() {
		return sConfig;
	}

	public void setsConfig(ScannerConfigure sConfig) {
		this.sConfig = sConfig;
	}

	public Job getScanJob() {
		return scanJob;
	}

	public ArrayList<ContentInfo> getContentInfo() {
		return contentInfo;
	}
}
