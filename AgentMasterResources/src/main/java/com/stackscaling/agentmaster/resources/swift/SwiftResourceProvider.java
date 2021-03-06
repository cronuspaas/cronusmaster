package com.stackscaling.agentmaster.resources.swift;

import org.javaswift.joss.client.factory.AccountFactory;
import org.javaswift.joss.model.Account;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import com.stackscaling.agentmaster.resources.utils.VarUtils;

/**
 * openstack swift resources
 *
 * @author binyu
 *
 */
public class SwiftResourceProvider {

	/*
	 * agentmaster.userDataDao.swift.tenantId=your_tenant_id
	 * agentmaster.userDataDao.swift.tenantName=your_tenant_name
	 * agentmaster.userDataDao.swift.username=your_username
	 * agentmaster.userDataDao.swift.password=your_password
	 * agentmaster.userDataDao.swift.authenticationUrl=authn_url
	 */

	// you do not have to pass both tenant ID and tenant name; passing either
	// tenant ID or tenant name is sufficient.
//	static final String tenantId = new Play().configuration
//			.getProperty("agentmaster.userDataDao.swift.tenantId");
//	static final String tenantName = new Play().configuration
//			.getProperty("agentmaster.userDataDao.swift.tenantName");
//	static final String username = new Play().configuration
//			.getProperty("agentmaster.userDataDao.swift.username");
//	static final String password = new Play().configuration
//			.getProperty("agentmaster.userDataDao.swift.password");
//	static final String authenticationUrl = new Play().configuration
//			.getProperty("agentmaster.userDataDao.swift.authenticationUrl");

	private static Account account;

	/**
	 * swift client
	 *
	 * @return
	 */
	public @Bean @Scope("singleton") Account swiftClient() {
		Account account = new AccountFactory().setUsername(VarUtils.username)
				.setPassword(VarUtils.password).setAuthUrl(VarUtils.authenticationUrl)
				.setTenantId(VarUtils.tenantId).setTenantName(VarUtils.tenantName)
				.createAccount();
		return account;
	}

	/**
	 * get swift client
	 *
	 * @return
	 */
	public synchronized static Account getSwiftClient() {
		if (account == null) {
			account = new AccountFactory().setUsername(VarUtils.username)
					.setPassword(VarUtils.password).setAuthUrl(VarUtils.authenticationUrl)
					.setTenantId(VarUtils.tenantId).setTenantName(VarUtils.tenantName)
					.createAccount();
		}
		return account;

	}
}
