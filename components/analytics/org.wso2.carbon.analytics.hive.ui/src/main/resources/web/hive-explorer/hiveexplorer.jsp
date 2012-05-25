<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.analytics.hive.ui.client.HiveScriptStoreClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.apache.poi.hssf.record.formula.functions.True" %>
<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.analytics.hive.ui.i18n.Resources">
<script src="../editarea/edit_area_full.js" type="text/javascript"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>

<script type="text/javascript">

    YAHOO.util.Event.onDOMReady(function() {
        editAreaLoader.init({
                    id : "allcommands"
                    ,syntax: "sql"
                    ,start_highlight: true
                });
    });
</script>

<%
    String scriptName = "";
    String scriptContent = "";
    String cron = "";
    String mode = request.getParameter("mode");
    if (null != request.getParameter("cron")) {
        cron = request.getParameter("cron").toString();
    }
    boolean scriptNameExists = false;
    if (request.getParameter("scriptName") != null && !request.getParameter("scriptName").equals("")) {
        scriptName = request.getParameter("scriptName");
    }
    if (null != mode && mode.equalsIgnoreCase("edit")) {
        scriptNameExists = true;
    } else {
        scriptNameExists = false;
        mode = "";
    }
    String requestUrl = request.getHeader("Referer");
    boolean isFromScheduling = false;
    if (requestUrl != null && requestUrl.contains("scheduletask.jsp")) {
        isFromScheduling = true;
    }
    if (scriptNameExists && !isFromScheduling) {
        try {
            String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            HiveScriptStoreClient client = new HiveScriptStoreClient(cookie, serverURL, configContext);
            scriptContent = client.getScript(scriptName);
            cron = client.getCronExpression(scriptName);
        } catch (Exception e) {
            String errorString = e.getMessage();
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
%>
<script type="text/javascript">
    location.href = "../admin/error.jsp";
    alert('<%=errorString%>');
</script>
<%
            return;
        }
    }
    if (isFromScheduling) {
        scriptContent = request.getParameter("scriptContent");
        if (scriptContent != null && !scriptContent.equals("")) {
            String[] queries = scriptContent.split(";");
            scriptContent = "";
            for (String aquery : queries) {
                aquery = aquery.trim();
                if (!aquery.equals("")) scriptContent = scriptContent + aquery + ";" + "\n";
            }
        }
    }
%>
<script type="text/javascript">
    var cron = '<%=cron%>';
    var scriptName = '<%=scriptName%>';
    var allQueries = '';
    function executeQuery() {
        var allQueries = editAreaLoader.getValue("allcommands");
        if (allQueries != "") {
            new Ajax.Request('../hive-explorer/queryresults.jsp', {
                        method: 'post',
                        parameters: {queries:allQueries},
                        onSuccess: function(transport) {
                            var allPage = transport.responseText;
                            var divText = '<div id="returnedResults">';
                            var closeDivText = '</div>';
                            var temp = allPage.indexOf(divText, 0);
                            var startIndex = temp + divText.length;
                            var endIndex = allPage.indexOf(closeDivText, temp);
                            var queryResults = allPage.substring(startIndex, endIndex);
                            document.getElementById('hiveResult').innerHTML = queryResults;
                        },
                        onFailure: function(transport) {
                            CARBON.showErrorDialog(transport.responseText);
                        }
                    });

        } else {
            var message = "Empty query can not be executed";
            CARBON.showErrorDialog(message);
        }
    }

    function saveScript() {
        allQueries = editAreaLoader.getValue("allcommands");
        scriptName = document.getElementById('scriptName').value;
        if (allQueries != "") {
            if (scriptName != "") {
                if (cron != "") {
                    checkExistingNameAndSaveScript();
                }
                else {
                    CARBON.showConfirmationDialog("Do you want to schedule the script?", function() {
                        scheduleTask();
                    }, function() {
                        checkExistingNameAndSaveScript();
                    }, function() {

                    });
                }

            } else {
                var message = "Please enter script name to save";
                CARBON.showErrorDialog(message);
            }

        } else {
            var message = "Empty query can not be executed";
            CARBON.showErrorDialog(message);
        }
    }

    function cancelScript() {
        location.href = "../hive-explorer/listscripts.jsp";
    }

    function scheduleTask() {
        var allQueries = editAreaLoader.getValue("allcommands")
        document.getElementById('commandForm').action = "../hive-explorer/scheduletask.jsp?mode=" + '<%=mode%>&scriptContent=' + allQueries + '&cron=' + '<%=cron%>';
        document.getElementById('commandForm').submit();
    }

    function checkExistingNameAndSaveScript() {
        var mode = '<%=mode%>';
        if (mode != 'edit') {
            new Ajax.Request('../hive-explorer/ScriptNameChecker', {
                        method: 'post',
                        parameters: {scriptName:scriptName},
                        onSuccess: function(transport) {
                            var result = transport.responseText;
                            if (result.indexOf('true') != -1) {
                                var message = "The script name: " + scriptName + 'already exists in the database. Please enter a different script name.';
                                CARBON.showErrorDialog(message);
                            } else {
                                     sendRequestToSaveScript();
                            }
                        },
                        onFailure: function(transport) {
                            return true;
                        }
                    });
        } else {
            sendRequestToSaveScript();
        }
    }

    function sendRequestToSaveScript() {
        new Ajax.Request('../hive-explorer/SaveScriptProcessor', {
                    method: 'post',
                    parameters: {queries:allQueries, scriptName:scriptName,
                        cronExp:cron},
                    onSuccess: function(transport) {
                        var result = transport.responseText;
                        if (result.indexOf('Success') != -1) {
                            CARBON.showInfoDialog(result, function() {
                                location.href = "../hive-explorer/listscripts.jsp";
                            }, function() {
                                location.href = "../hive-explorer/listscripts.jsp";
                            });

                        } else {
                            CARBON.showErrorDialog(result);
                        }
                    },
                    onFailure: function(transport) {
                        CARBON.showErrorDialog(result);
                    }
                });
    }

</script>

<style type="text/css">
    .scrollable {
        border: 1px solid black;
        width: 85%;
        height: 300px;
        overflow-y: scroll;
        overflow-x: auto;
        /*clip-rect:(20px, 500px, 600px, 20px );*/
    }

    table.result {
        border-width: 2px;
        border-style: solid;
        border-color: maroon;
        background-color: white;
    }

    table.allResult {
        border-width: 1px;
        border-style: solid;
        border-color: white;
        background-color: white;
        width: 100%;
    }

</style>


<div id="middle">
    <%
        if (scriptNameExists) {
    %>
    <h2>Hive Explorer<%=" - " + scriptName%>
        <%
        } else {
        %>
        <h2>Hive Explorer</h2>
        <%
            }
        %>
    </h2>

    <div id="workArea">

        <form id="commandForm" name="commandForm" action="" method="POST">
            <table class="styledLeft">
                <thead>
                <tr>
                    <th><span style="float: left; position: relative; margin-top: 2px;">
                            <fmt:message key="hive.commands"/></span>
                    </th>
                </tr>
                </thead>
                <tbody>
                <%
                    if (!scriptNameExists) {
                %>
                <tr>
                    <td>
                        <table class="normal-nopadding">
                            <tbody>
                            <tr>
                                <td>
                                    <fmt:message key="hive.script.name"/>
                                </td>
                                <td>
                                    <input type="text" id="scriptName" name="scriptName" size="60"
                                           value="<%=scriptName%>"/>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </td>
                </tr>
                <%
                } else { %>
                <input type="hidden" value="<%=scriptName%>" name="scriptName" id="scriptName">
                <% }
                %>
                <tr>
                    <td>
                        <table class="normal-nopadding">
                            <tbody>
                            <tr>
                                <td>
                                    <textarea id="allcommands" name="allcommands" cols="150"
                                              rows="15"><%=scriptContent%>
                                    </textarea>
                                </td>
                                    <%--<td>--%>
                                    <%--<input class="button" type="button" onclick="scheduleTask()" value="Schedule"/>--%>
                                    <%--</td>--%>
                            </tr>
                            <tr>

                                <td align="right">
                                    <table class="normal-nopadding">
                                        <tbody>
                                        <tr>
                                            <td></td>
                                            <td></td>
                                            <td></td>
                                            <td></td>
                                            <td></td>
                                            <td></td>
                                            <td></td>
                                            <td></td>
                                            <td></td>
                                            <td></td>
                                            <td></td>
                                            <td></td>
                                            <td></td>
                                            <td></td>
                                            <td></td>
                                            <td></td>
                                            <td></td>
                                            <td></td>
                                            <td></td>
                                            <td></td>
                                            <td></td>
                                            <td></td>
                                            <td valign="top">
                                                <a href="javascript: scheduleTask();"><label><img
                                                        src="images/schedule_icon.png" alt="schedule_icon">Schedule
                                                    Script</label></a>
                                            </td>
                                        </tr>
                                        </tbody>
                                    </table>
                                </td>

                            </tr>
                            <tr>
                                <td>
                                    <input class="button" type="button" onclick="executeQuery()" value="Run>"/>
                                    <input class="button" type="button" onclick="saveScript()" value="Save"/>
                                    <input type="button" value="Cancel" onclick="cancelScript()"
                                           class="button"/>
                                </td>
                            </tr>

                            </tbody>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td>
                        <table class="normal-nopadding">
                            <tbody>
                            <tr>
                                <td>
                                </td>
                                <td>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td class="middle-header">
                        <fmt:message key="hive.query.results"/>
                    </td>
                </tr>
                <tr>
                    <td>

                    </td>
                </tr>
                <tr>
                    <td>
                        <div id="hiveResult" class="scrollable">
                                <%--the results goes here...--%>
                        </div>
                    </td>
                </tr>
                </tbody>
            </table>
            </td>
            </tr>
            </tbody>
            </table>

        </form>


    </div>
</div>


</fmt:bundle>
