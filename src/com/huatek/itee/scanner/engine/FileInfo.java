package com.huatek.itee.scanner.engine;

public class FileInfo {

	private int lineNum;

	private String location;

	public FileInfo(int lineNum, String location) {
		super();
		this.lineNum = lineNum;
		this.location = location;
	}

	public int getLineNum() {
		return lineNum;
	}

	public void setLineNum(int lineNum) {
		this.lineNum = lineNum;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public static boolean isValidLine(String line) {
		if (line.trim().startsWith("//") || line.trim().startsWith("/**") || line.trim().startsWith("*/")
				|| line.trim().startsWith("*") || line.trim().startsWith("import") || line.trim().startsWith("@") || line.trim().startsWith("package")) {
			return false;
		}
		if (line.trim().length() > 1) {
			return true;
		}
		return false;
	}
}
