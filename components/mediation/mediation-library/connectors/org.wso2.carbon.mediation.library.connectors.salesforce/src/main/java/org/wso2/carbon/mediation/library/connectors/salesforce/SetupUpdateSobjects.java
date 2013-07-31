package org.wso2.carbon.mediation.library.connectors.salesforce;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.mediation.library.connectors.core.AbstractConnector;

public class SetupUpdateSobjects extends AbstractConnector {
	 public void connect(MessageContext synCtx) {

	    	SynapseLog synLog = getLog(synCtx);

	        if (synLog.isTraceOrDebugEnabled()) {
	            synLog.traceOrDebug("Start : Salesforce update SObjects mediator");

	            if (synLog.isTraceTraceEnabled()) {
	                synLog.traceTrace("Message : " + synCtx.getEnvelope());
	            }
	        }    	
	    	
	        SalesforceUtil salesforceUtil = SalesforceUtil.getSalesforceUtil();
	        salesforceUtil.addSobjects("update", SalesforceUtil.SALESFORCE_SOBJECTS, synCtx, synLog, null);	      	       

	        if (synLog.isTraceOrDebugEnabled()) {
	        	synLog.traceOrDebug("End : Salesforce update SObjects mediator");

	            if (synLog.isTraceTraceEnabled()) {
	                synLog.traceTrace("Message : " + synCtx.getEnvelope());
	            }
	        } 
	    }
}
