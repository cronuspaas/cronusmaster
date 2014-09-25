package com.stackscaling.agentmaster.resources.cronuspkg;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.stackscaling.agentmaster.resources.IUserDataProvider;

/**
 * cronus package DAO
 * 
 * @author binyu
 *
 */
public interface ICronusPkgData extends IUserDataProvider {

	/**
	 * get all commands back
	 * @return
	 */
	public Map<String, ICronusPkg> getAllPkgs() throws IOException;

	/**
	 * find pkg by name
	 * @param name
	 * @return
	 */
	public ICronusPkg getPkgByName(String name) throws IOException;

	/**
	 * list of pkgs matching filter(s)
	 * @param filter
	 * @param filterValue
	 * @return
	 * @throws IOException
	 */
	public List<ICronusPkg> getPkgsByFilters(Map<String, String> filters) throws IOException;

	
	/**
	 * save pkg
	 * @throws IOException
	 */
	public void save(String scriptName, InputStream dataInputStream) throws IOException;

	/**
	 * load all pkg metadata from backing storage
	 */
	public void load() throws IOException;

	/**
	 * total number of pkgs
	 * @return
	 */
	public int getPkgCount();
	
	/**
	 * input stream for download
	 * @param pkgName
	 * @return
	 */
	public InputStream getDownloadStream(String pkgName) throws IOException;

}
