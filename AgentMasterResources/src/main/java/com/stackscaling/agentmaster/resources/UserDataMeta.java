package com.stackscaling.agentmaster.resources;

import java.util.Comparator;
import java.util.Date;

/**
 * metadata for a piece of user data
 * 
 * @author binyu
 *
 */
public class UserDataMeta {
	
	private String name;
	private long size;
	private Date lastModified;
	
	public UserDataMeta() {}
	
	public UserDataMeta(String name, long size, Date lastModified) {
		this.name = name;
		this.size = size;
		this.lastModified = lastModified;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	
	public static class UserDataMetaComparator implements Comparator<UserDataMeta> {

		@Override
		public int compare(UserDataMeta o1, UserDataMeta o2) {
			if (o1==null || o2==null || o1.name==null || o2.name==null) {
				throw new IllegalArgumentException("user data meta cannot be empty");
			}
			return o1.name.compareTo(o2.name);
		}
		
	}

}
