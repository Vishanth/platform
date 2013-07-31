/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mediation.library.connectors.jira;

import static org.wso2.carbon.mediation.library.connectors.jira.JiraConstants.CONTEXT_USER;

import org.apache.synapse.MessageContext;
import org.wso2.carbon.mediation.library.connectors.jira.util.JiraMediatorUtil;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.User;

public class JiraGetUserMediator extends JiraMediator {

	private String requiredUsername;


	@Override
	public boolean mediate(MessageContext synCtx) {
        JiraRestClient client = JiraMediatorUtil.getClient(getUri(), getUsername(), getPassword());
        User issue = client.getUserClient().getUser(getRequiredUsername()).claim();
        synCtx.setProperty(CONTEXT_USER, issue);
        return true;
	}

	public String getRequiredUsername() {
		return requiredUsername;
	}

	public void setRequiredUsername(String requiredUsername) {
		this.requiredUsername = requiredUsername;
	}

}
