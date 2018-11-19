package com.huatek.itee.scanner.engine;

import java.util.ArrayList;

public class ContentInfo {
	
	private String content;

	private ArrayList<FileInfo> fileInfos;
	
	public ContentInfo(String content, ArrayList<FileInfo> fileInfos) {
		super();
		this.content = content;
		this.fileInfos = fileInfos;
	}

	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public ArrayList<FileInfo> getFileInfos() {
		return fileInfos;
	}
	
	public void setFileInfos(ArrayList<FileInfo> fileInfos) {
		this.fileInfos = fileInfos;
	}
}
