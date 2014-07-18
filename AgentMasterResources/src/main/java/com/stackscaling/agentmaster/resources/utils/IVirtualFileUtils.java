package com.stackscaling.agentmaster.resources.utils;

import java.io.File;

/**
 * allow to work with relative path
 * 
 * @author binyu
 *
 */
public interface IVirtualFileUtils {

	/**
	 * real file from relative path
	 * @param relativePath
	 * @return
	 */
    public File getRealFileFromRelativePath(String relativePath);
    
    /**
     * read file content to string
     * @param relativePath
     * @return
     */
	public String readFileToString(String relativePath);    
    
}
