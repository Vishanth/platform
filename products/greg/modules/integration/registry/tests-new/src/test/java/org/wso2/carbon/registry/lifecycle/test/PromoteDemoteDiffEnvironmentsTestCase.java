/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.lifecycle.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.governance.GovernanceServiceClient;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleAdminServiceClient;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleManagementClient;
import org.wso2.carbon.automation.api.clients.governance.ListMetaDataServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.services.ArrayOfString;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.governance.list.stub.ListMetadataServiceRegistryExceptionException;
import org.wso2.carbon.governance.list.stub.beans.xsd.ServiceBean;
import org.wso2.carbon.governance.services.stub.AddServicesServiceRegistryExceptionException;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.xml.sax.InputSource;

import javax.activation.DataHandler;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Promote/Demote services in the different environment and verify audit records
 */
public class PromoteDemoteDiffEnvironmentsTestCase {

    private int userId = 2;
    private UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
    private String serviceString;
    private WSRegistryServiceClient wsRegistryServiceClient;

    private LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
    private LifeCycleManagementClient lifeCycleManagementClient;
    private GovernanceServiceClient governanceServiceClient;
    private ListMetaDataServiceClient listMetadataServiceClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;

    private static final String SERVICE_NAME = "IntergalacticService";
    private static final String LC_NAME = "DiffEnvironmentLC";
    private static final String LC_STATE0 = "Development";
    private static final String LC_STATE1 = "Testing";
    private static final String ACTION_PROMOTE = "Promote";
    private static final String ACTION_DEMOTE = "Demote";
    private static final String ACTION_TYPE = "type";
    private static final String USER = "user";
    private static final String STATE = "state";
    private static final String TARGET_STATE = "targetState";
    private static final String TYPE = "transition";
    private static final String ACTION_NAME = "name";
    private static final String auditPath =
            "/_system/governance/repository/components/org.wso2.carbon.governance/lifecycles/history/"
            + "__system_governance_trunk_services_com_abb_IntergalacticService";

    private LifecycleBean lifeCycle;
    private ServiceBean service;
    private RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();


    /**
     * @throws RemoteException
     * @throws LoginAuthenticationExceptionException
     *
     * @throws RegistryException
     */
    @BeforeClass(alwaysRun = true)
    public void init() throws RemoteException, LoginAuthenticationExceptionException,
                              RegistryException {
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();

        lifeCycleAdminServiceClient =
                new LifeCycleAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                                environment.getGreg().getSessionCookie());
        governanceServiceClient =
                new GovernanceServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                            environment.getGreg().getSessionCookie());
        listMetadataServiceClient =
                new ListMetaDataServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                              environment.getGreg().getSessionCookie());
        lifeCycleManagementClient =
                new LifeCycleManagementClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                              environment.getGreg().getSessionCookie());

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                                               environment.getGreg().getSessionCookie());
        wsRegistryServiceClient =
                registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);

    }

    /**
     * @throws XMLStreamException
     * @throws IOException
     * @throws AddServicesServiceRegistryExceptionException
     *
     * @throws ListMetadataServiceRegistryExceptionException
     *
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Create a service")
    public void testCreateService() throws XMLStreamException, IOException,
                                           AddServicesServiceRegistryExceptionException,
                                           ListMetadataServiceRegistryExceptionException,
                                           ResourceAdminServiceExceptionException {

        String servicePath =
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                File.separator + "GREG" + File.separator + "services" +
                File.separator + "intergalacticService.metadata.xml";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + servicePath));
        String mediaType = "application/vnd.wso2-service+xml";
        String description = "This is a test service";
        resourceAdminServiceClient.addResource(
                "/_system/governance/service2", mediaType, description, dataHandler);

        ResourceData[] data =  resourceAdminServiceClient.getResource("/_system/governance/trunk/services/com/abb/IntergalacticService");
        
        assertNotNull(data, "Service not found");

    }

    /**
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test(groups = "wso2.greg", description = "Create new life cycle", dependsOnMethods = "testCreateService")
    public void testCreateNewLifeCycle() throws LifeCycleManagementServiceExceptionException,
                                                IOException, InterruptedException {
        String resourcePath =
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                File.separator + "GREG" + File.separator + "lifecycle" +
                File.separator + "EnvironmentChangeLC.xml";
        String lifeCycleContent = FileManager.readFile(resourcePath);
        lifeCycleManagementClient.addLifeCycle(lifeCycleContent);
        String[] lifeCycles = lifeCycleManagementClient.getLifecycleList();

        boolean lcStatus = false;
        for (String lc : lifeCycles) {
            if (lc.equalsIgnoreCase(LC_NAME)) {
                lcStatus = true;
                break;
            }
        }
        assertTrue(lcStatus, "LifeCycle not found");

    }

    /**
     * @throws RegistryException
     * @throws RemoteException
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws ListMetadataServiceRegistryExceptionException
     *
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add lifecycle to a service", dependsOnMethods = "testCreateNewLifeCycle")
    public void testAddLcToService() throws RegistryException, RemoteException,
                                            CustomLifecyclesChecklistAdminServiceExceptionException,
                                            ListMetadataServiceRegistryExceptionException,
                                            ResourceAdminServiceExceptionException {

        service = listMetadataServiceClient.listServices(null);
        for (String services : service.getPath()) {
            if (services.contains("IntergalacticService")) {
                serviceString = services;
                break;
            }
        }
        wsRegistryServiceClient.associateAspect("/_system/governance" + serviceString, LC_NAME);
        lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean("/_system/governance" +
                                                                 serviceString);

        Property[] properties = lifeCycle.getLifecycleProperties();

        boolean lcStatus = false;
        for (Property prop : properties) {
            if (prop.getKey().contains(LC_NAME)) {
                lcStatus = true;
                break;
            }
        }
        assertTrue(lcStatus, "LifeCycle not added to service");
    }

    /**
     * @throws RemoteException
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws RegistryExceptionException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Promote from Development to Testing", dependsOnMethods = "testAddLcToService")
    public void testPromoteToTesting() throws RemoteException,
                                              CustomLifecyclesChecklistAdminServiceExceptionException,
                                              LifeCycleManagementServiceExceptionException,
                                              RegistryExceptionException,
                                              ResourceAdminServiceExceptionException {

        ArrayOfString[] parameters = new ArrayOfString[5];
        String[] dependencyList =
                lifeCycleAdminServiceClient.getAllDependencies("/_system/governance" +
                                                               serviceString);
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{dependencyList[0], "1.0.0"});
        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{dependencyList[1], "1.0.0"});
        parameters[2] = new ArrayOfString();
        parameters[2].setArray(new String[]{dependencyList[2], "1.0.0"});
        parameters[3] = new ArrayOfString();
        parameters[3].setArray(new String[]{dependencyList[3], "1.0.0"});
        parameters[4] = new ArrayOfString();
        parameters[4].setArray(new String[]{"preserveOriginal", "false"});
        lifeCycleAdminServiceClient.invokeAspectWithParams("/_system/governance" + serviceString,
                                                           LC_NAME, ACTION_PROMOTE, null,
                                                           parameters);
        service = listMetadataServiceClient.listServices(null);
        for (String services : service.getPath()) {
            if (services.contains("IntergalacticService") && services.contains("/testing/")) {
                serviceString = services;
            }
        }
        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean("/_system/governance" +
                                                             serviceString);

        for (Property prop : lifeCycle.getLifecycleProperties()) {
            if (("registry.lifecycle." + LC_NAME + ".state").equalsIgnoreCase(prop.getKey())) {
                assertNotNull(prop.getValues(), "State Value Not Found");
                assertTrue(prop.getValues()[0].equalsIgnoreCase(LC_STATE1),
                           "Not promoted to Testing");
            }
        }

    }

    /**
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Verify Audit records", dependsOnMethods = "testPromoteToTesting")
    public void testVerifyAuditTestPromote() throws Exception {

        String EXECUTOR =
                "org.wso2.carbon.governance.registry.extensions.executors.ServiceVersionExecutor";
        String[] info =
                {
                        "/_system/governance/branches/testing/schemas/org/bar/purchasing/1.0.0/purchasing.xsd created",
                        "/_system/governance/branches/testing/wsdls/com/foo/1.0.0/IntergalacticService.wsdl created",
                        "/_system/governance/branches/testing/endpoints/localhost/axis2/services/1.0.0/ep-BizService created",
                        "/_system/governance/branches/testing/services/com/abb/1.0.0/IntergalacticService created"};
        String INFO = "info";

        assertEquals(getAuditRecords(auditPath, 0, USER, 0), userInfo.getUserNameWithoutDomain(),
                     "User is not testuser1");
        assertEquals(getAuditRecords(auditPath, 0, STATE, 0), LC_STATE0,
                     "State before transition is not Development");
        assertEquals(getAuditRecords(auditPath, 0, TARGET_STATE, 0), LC_STATE1,
                     "State after transition is not Testing");
        assertEquals(getAuditRecords(auditPath, 0, ACTION_TYPE, 1), TYPE,
                     "Action is not transition");
        assertEquals(getAuditRecords(auditPath, 0, ACTION_NAME, 1), ACTION_PROMOTE,
                     "Transition is not PROMOTE");
        assertEquals(getAuditRecords(auditPath, 0, ACTION_NAME, 2), EXECUTOR, "Executor not listed");

        boolean[] infoState = {false, false, false, false};
        for (int i = 0; i < 4; i++) {

            for (String record : info) {
                if (record.equalsIgnoreCase(getAuditRecords(auditPath, i, INFO, 3))) {
                    infoState[i] = true;
                }
            }
            assertTrue(infoState[i], "Incorrect audit log");
        }

    }

    /**
     * @throws RemoteException
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws RegistryExceptionException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Demote from Production", dependsOnMethods = "testVerifyAuditTestPromote")
    public void testDemoteFromTesting() throws RemoteException,
                                               CustomLifecyclesChecklistAdminServiceExceptionException,
                                               LifeCycleManagementServiceExceptionException,
                                               RegistryExceptionException,
                                               ResourceAdminServiceExceptionException {

        ArrayOfString[] parameters = new ArrayOfString[1];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{"preserveOriginal", "false"});
        lifeCycleAdminServiceClient.invokeAspectWithParams("/_system/governance" + serviceString,
                                                           LC_NAME, ACTION_DEMOTE, null, parameters);
        service = listMetadataServiceClient.listServices(null);
        for (String services : service.getPath()) {
            if (services.contains("/abb/1.0.0")) {
                serviceString = services;
            }
        }
        lifeCycle =
                lifeCycleAdminServiceClient.getLifecycleBean("/_system/governance" +
                                                             serviceString);

        for (Property prop : lifeCycle.getLifecycleProperties()) {
            if (("registry.lifecycle." + LC_NAME + ".state").equalsIgnoreCase(prop.getKey())) {
                assertNotNull(prop.getValues(), "State Value Not Found");
                assertTrue(prop.getValues()[0].equalsIgnoreCase(LC_STATE0),
                           "LifeCycle not demoted to Development");
            }
        }

    }

    /**
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Verify Audit records", dependsOnMethods = "testDemoteFromTesting")
    public void testVerifyAuditTestDemote() throws Exception {

        String EXECUTOR =
                "org.wso2.carbon.governance.registry.extensions.executors.ServiceVersionExecutor";
        String[] info =
                {"/_system/governance/trunk/services/com/abb/1.0.0 created",
                 "/_system/governance/trunk/wsdls/com/foo/1.0.0 created",
                 "/_system/governance/trunk/endpoints/localhost/axis2/services/1.0.0 created"};

        String INFO = "info";

        assertEquals(getAuditRecords(auditPath, 0, USER, 0), userInfo.getUserNameWithoutDomain(),
                     "User is not testuser1");
        assertEquals(getAuditRecords(auditPath, 0, STATE, 0), LC_STATE1,
                     "State before transition is not Testing");
        assertEquals(getAuditRecords(auditPath, 0, TARGET_STATE, 0), LC_STATE0,
                     "State after transition is not Development");
        assertEquals(getAuditRecords(auditPath, 0, ACTION_TYPE, 1), TYPE,
                     "Action is not transition");
        assertEquals(getAuditRecords(auditPath, 0, ACTION_NAME, 1), ACTION_DEMOTE,
                     "Transition is not PROMOTE");
        assertEquals(getAuditRecords(auditPath, 0, ACTION_NAME, 2), EXECUTOR, "Executor not listed");

        boolean[] infoState = {false, false, false, false};
        for (int i = 0; i < 3; i++) {

            for (String record : info) {
                if (record.equalsIgnoreCase(getAuditRecords(auditPath, i, INFO, 3))) {
                    infoState[i] = true;
                }
            }
            assertTrue(infoState[i], "Incorrect audit log");
        }
    }

    /**
     * @param auditPath the location of the audit record
     * @param node      the index of the node of which info is read
     * @param item      the tag of which info is read
     * @param level     the level of the node in the tag hierarchy
     * @return info in the specified node
     * @throws Exception
     */
    public String getAuditRecords(String auditPath, int node, String item, int level)
            throws Exception {

        byte[] string = wsRegistryServiceClient.getContent(auditPath);
        String xml = new String(string);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xml)));
        Element rootElement = document.getDocumentElement();
        rootElement.getFirstChild().getNodeValue();
        NodeList nd = rootElement.getChildNodes();

        if (level == 0) {
            return nd.item(0).getAttributes().getNamedItem(item).getNodeValue();
        } else if (level == 1) {
            return nd.item(0).getFirstChild().getAttributes().getNamedItem(item).getNodeValue();
        } else if (level == 2) {
            return nd.item(0).getFirstChild().getFirstChild().getFirstChild().getAttributes()
                    .getNamedItem(item).getNodeValue();
        } else {
            NodeList infoNodes =
                    nd.item(0).getFirstChild().getFirstChild().getFirstChild()
                            .getFirstChild().getFirstChild().getChildNodes();
            return infoNodes.item(node).getTextContent();
        }
    }


    @AfterClass(alwaysRun = true)
    public void clear() throws Exception {
        String servicePathToDelete = "/_system/governance/" + serviceString;
        if (wsRegistryServiceClient.resourceExists(servicePathToDelete)) {
            resourceAdminServiceClient.deleteResource(servicePathToDelete);
        }
        String schemaPathToDelete = "/_system/governance/trunk/schemas/org/bar/purchasing/purchasing.xsd";
        if (wsRegistryServiceClient.resourceExists(schemaPathToDelete)) {
            resourceAdminServiceClient.deleteResource(schemaPathToDelete);
        }
        schemaPathToDelete = "/_system/governance/branches/testing/schemas/org/bar/purchasing/1.0.0/purchasing.xsd";
        if (wsRegistryServiceClient.resourceExists(schemaPathToDelete)) {
            resourceAdminServiceClient.deleteResource(schemaPathToDelete);
        }
        String wsdlPathToDelete = "/_system/governance/trunk/wsdls/com/foo/IntergalacticService.wsdl";
        if (wsRegistryServiceClient.resourceExists(wsdlPathToDelete)) {
            resourceAdminServiceClient.deleteResource(wsdlPathToDelete);
        }
        wsdlPathToDelete = "/_system/governance/branches/testing/wsdls/com/foo/1.0.0/IntergalacticService.wsdl";
        if (wsRegistryServiceClient.resourceExists(wsdlPathToDelete)) {
            resourceAdminServiceClient.deleteResource(wsdlPathToDelete);
        }
        wsdlPathToDelete = "/_system/governance/trunk/wsdls/com/foo/1.0.0";
        if (wsRegistryServiceClient.resourceExists(wsdlPathToDelete)) {
            resourceAdminServiceClient.deleteResource(wsdlPathToDelete);
        }
        lifeCycleManagementClient.deleteLifeCycle(LC_NAME);

        governanceServiceClient = null;
        wsRegistryServiceClient = null;
        lifeCycleAdminServiceClient = null;
        lifeCycleManagementClient = null;
        listMetadataServiceClient = null;
        resourceAdminServiceClient = null;
    }

}
