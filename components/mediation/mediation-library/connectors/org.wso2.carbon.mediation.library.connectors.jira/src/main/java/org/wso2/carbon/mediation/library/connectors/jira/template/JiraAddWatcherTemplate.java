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

package org.wso2.carbon.mediation.library.connectors.jira.template;

import static org.wso2.carbon.mediation.library.connectors.jira.JiraConstants.FUNC_WATCHERS_URI;
import static org.wso2.carbon.mediation.library.connectors.jira.JiraConstants.FUNC_WATCHER_USERNAME;
import static org.wso2.carbon.mediation.library.connectors.jira.template.JiraTemplateUtil.fillAuthParams;
import static org.wso2.carbon.mediation.library.connectors.jira.template.JiraTemplateUtil.lookupFunctionParam;

import org.apache.synapse.MessageContext;
import org.wso2.carbon.mediation.library.connectors.jira.issue.JiraAddWatcherMediator;

public class JiraAddWatcherTemplate extends JiraAddWatcherMediator {

	@Override
	public boolean mediate(MessageContext synCtx) {
		fillAuthParams(synCtx, this);
		setWatchersUri(lookupFunctionParam(synCtx, FUNC_WATCHERS_URI));
		setWatcherUsername(lookupFunctionParam(synCtx, FUNC_WATCHER_USERNAME));

		return super.mediate(synCtx);
	}
}
