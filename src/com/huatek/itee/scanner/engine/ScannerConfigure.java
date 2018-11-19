package com.huatek.itee.scanner.engine;

import java.util.ArrayList;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

public class ScannerConfigure {

	private ArrayList<IProject> projects;

	private ArrayList<String> fileTypes;

	public ScannerConfigure() {
		super();
		projects = new ArrayList<IProject>();
		fileTypes = new ArrayList<String>();
		initializeConfig();
	}

	private void initializeConfig() {
		for (IProject proj : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			projects.add(proj);
		}
		fileTypes.add("java");
	}

	public ArrayList<IProject> getProjects() {
		return projects;
	}

	public void setProjects(ArrayList<IProject> projects) {
		this.projects = projects;
	}

	public ArrayList<String> getFileTypes() {
		return fileTypes;
	}

	public void setFileTypes(ArrayList<String> fileTypes) {
		this.fileTypes = fileTypes;
	}

	public void update() {
		projects.clear();
		for (IProject proj : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			projects.add(proj);
		}
	}

	public boolean isTypeMatch(String name) {
		for (String type : fileTypes) {
			if (name.endsWith(type)) {
				return true;
			}
		}
		return false;
	}
}
