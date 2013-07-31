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

import static org.wso2.carbon.mediation.library.connectors.jira.JiraConstants.CONTEXT_BASIC_ISSUE;

import java.util.HashMap;
import java.util.Map;

import org.apache.synapse.MessageContext;
import org.wso2.carbon.mediation.library.connectors.jira.util.JiraMediatorUtil;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.CimProject;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;

public class JiraCreateIssueMediator extends JiraMediator {
	private String issueFieldsJson;

	@Override
	public boolean mediate(MessageContext synCtx) {
		// TODO Passing null to getCreateIssueMetadata needs to be changed
		JiraRestClient client = JiraMediatorUtil.getClient(getUri(), getUsername(), getPassword());
		Iterable<CimProject> cimProjectList =
		                                      client.getIssueClient().getCreateIssueMetadata(null)
		                                            .claim();
		Map<String, Object> fields = new HashMap<String, Object>();
		JiraMediatorUtil.getPojoFromJson(getIssueFieldsJson(), fields);
		BasicIssue basicIssue = null;
		if (fields != null && !fields.isEmpty()) {
			IssueInput newIssue = new IssueInput(null);
			basicIssue = client.getIssueClient().createIssue(newIssue).claim();
		}

		synCtx.setProperty(CONTEXT_BASIC_ISSUE, basicIssue);
		return true;
	}

	public String getIssueFieldsJson() {
		return issueFieldsJson;
	}

	public void setIssueFieldsJson(String issueFieldsJson) {
		this.issueFieldsJson = issueFieldsJson;
	}

}
