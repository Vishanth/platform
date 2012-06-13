/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.dataservices.core.engine;

import org.apache.axis2.databinding.types.NCName;
import org.wso2.carbon.dataservices.common.DBConstants;
import org.wso2.carbon.dataservices.common.DBConstants.DBSFields;
import org.wso2.carbon.dataservices.common.DBConstants.FaultCodes;
import org.wso2.carbon.dataservices.core.DBUtils;
import org.wso2.carbon.dataservices.core.DSSessionManager;
import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.boxcarring.TLParamStore;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.sql.Array;
import java.sql.SQLException;
import java.sql.Struct;
import java.util.List;
import java.util.Set;

/**
 * Represents a static entry in a Result element.
 */
public class StaticOutputElement extends OutputElement {

    /**
     * name of element/attribute
     */
    private String name;

    /**
     * param value
     */
    private String param;

    /**
     * original param value, without any modifications: toLowerCase
     */
    private String originalParam;

    /**
     * i.e. column, query-param, value
     */
    private String paramType;

    /**
     * i.e. element, attribute
     */
    private String elementType;

    /**
     * i.e. xs:string, xs:decimal, etc..
     */
    private QName xsdType;

    /**
     * user roles required to access this element
     */
    private Set<String> requiredRoles;

    /**
     * i.e. XML, RDF etc..
     */
    private int resultType;

    /**
     * i.e. VALUE, REFERENCE etc..
     */
    private int dataCategory;

    /**
     * Exports the values in this element,
     * these will be saved in a thread local storage,
     * which can be re-used later by other queries
     */
    private String export;

    /**
     * The type of value to be exported, i.e. SCALAR, ARRAY
     */
    private int exportType;

    /**
     * A flag to keep if this output element's value is a constant,
     * i.e. paramType = 'value'
     */
    private boolean hasConstantValue;

    /* If this element corresponds to a SQLArray, its name */
    private String arrayName;

    /* Represents whether this element corresponds to a User Defined Object such as UDT or
       SQLArray */
    private boolean isUserDefinedObj;

    /* If this element corresponds to a UDT then that UDT's metadata */
    private UDT udtInfo;

    /* Initial index of UDT attributes */
    public static final int UDT_ATTRIBUTE_INITIAL_INDEX = 0;


    public StaticOutputElement(DataService dataService, String name,
                               String param, String originalParam, String paramType,
                               String elementType, String namespace, QName xsdType,
                               Set<String> requiredRoles, int dataCategory, int resultType,
                               String export, int exportType, String arrayName) throws DataServiceFault {
        super(namespace);
        this.name = name;
        this.param = param;
        this.originalParam = originalParam;
        this.paramType = paramType;
        this.elementType = elementType;
        this.xsdType = xsdType;
        this.requiredRoles = requiredRoles;
        this.dataCategory = dataCategory;
        this.resultType = resultType;
        this.export = export;
        this.exportType = exportType;
        this.hasConstantValue = DBSFields.VALUE.equals(paramType);
        this.arrayName = arrayName;
        this.udtInfo = processParamForUserDefinedObjects(this.getParam());
        if (this.getArrayName() != null || this.getUDTInfo() != null) {
            this.isUserDefinedObj = true;
        }

        /* validate element/attribute name */
        if (!NCName.isValid(this.name)) {
            throw new DataServiceFault("Invalid output " + this.elementType + " name: '" +
                    this.name + "', must be an NCName.");
        }
    }

    public String getArrayName() {
        return arrayName;
    }

    public boolean hasConstantValue() {
        return hasConstantValue;
    }

    public String getExport() {
        return export;
    }

    public int getExportType() {
        return exportType;
    }

    public int getDataCategory() {
        return dataCategory;
    }

    public int getResultType() {
        return resultType;
    }

    public String getOriginalParam() {
        return originalParam;
    }

    public Set<String> getRequiredRoles() {
        return requiredRoles;
    }

    public boolean isOptional() {
        return this.getRequiredRoles() != null && this.getRequiredRoles().size() > 0;
    }

    public QName getXsdType() {
        return xsdType;
    }

    public String getName() {
        return name;
    }

    public String getParam() {
        return param;
    }

    public String getParamType() {
        return paramType;
    }

    public String getElementType() {
        return elementType;
    }

    public boolean isUserDefinedObj() {
        return isUserDefinedObj;
    }

    public UDT getUDTInfo() {
        return udtInfo;
    }

     /**
     * Checks whether this output element corresponds to a UDT and if so an object of UDT
     * class is populated.
     *
     * @param param             Initial column name specified for the output element
     * @return                  An instance of UDT class
     * @throws DataServiceFault If any error occurs while determining the nest indices of a UDT
     */
    private UDT processParamForUserDefinedObjects(String param) throws DataServiceFault {
        String udtColumnName = DBUtils.extractUDTColumnName(param);
        if (udtColumnName != null) {
            List<Integer> indices = DBUtils.getNestedIndices(param.substring(
                    udtColumnName.length() + 1, param.length()));
           return new UDT(udtColumnName, indices);
        }
        return null;
    }

    private ParamValue getParamValue(ExternalParamCollection params) throws DataServiceFault {
        if (this.getParamType().equals(DBConstants.DBSFields.RDF_REF_URI)) {
            return new ParamValue(this.getParam());
        } else {
            ExternalParam paramObj = this.getParamObj(params);
            /* workaround for 'column', 'query-param' mix up */
            if (paramObj == null) {
                if (this.getParamType().equals(DBSFields.COLUMN)) {
                    paramObj = params.getParam(DBSFields.QUERY_PARAM, this.getParam());
                } else if (this.getParamType().equals(DBSFields.QUERY_PARAM)) {
                    paramObj = params.getParam(DBSFields.COLUMN, this.getParam());
                }
            }
            if (paramObj != null) {
                return paramObj.getValue();
            } else {
                throw new DataServiceFault(FaultCodes.INCOMPATIBLE_PARAMETERS_ERROR,
                        "Error in 'StaticOutputElement.execute', " +
                                "cannot find parameter with type:"
                                + this.getParamType() + " name:" + this.getOriginalParam());
            }
        }
    }

    /**
     * Exports the given parameter.
     *
     * @param exportName The name of the variable to store the exported value
     * @param value      The exported value
     */
    private void exportParam(String exportName, String value, int type) {
        ParamValue paramVal = TLParamStore.getParam(exportName);
        if (paramVal == null || paramVal.getValueType() != type) {
            paramVal = new ParamValue(type);
            TLParamStore.addParam(exportName, paramVal);
        }
        if (type == ParamValue.PARAM_VALUE_ARRAY) {
            paramVal.addToArrayValue(new ParamValue(value));
        } else if (type == ParamValue.PARAM_VALUE_SCALAR) {
            paramVal.setScalarValue(value);
        }
    }

    @Override
    public void executeElement(XMLStreamWriter xmlWriter, ExternalParamCollection params,
                               int queryLevel) throws DataServiceFault {
        ParamValue paramValue;
        if (this.hasConstantValue()) {
            paramValue = new ParamValue(this.getParam());
        } else {
            paramValue = this.getParamValue(params);
        }
        /* export it if told, and only if it's boxcarring */
        if (this.getExport() != null && DSSessionManager.isBoxcarring()) {
            this.exportParam(this.getExport(), paramValue.toString(), this.getExportType());
        }
        try {
            /* write element */
            if (this.getElementType().equals(DBSFields.ELEMENT)) {

                this.writeResultElement(xmlWriter, this.getName(), paramValue, this.getXsdType(),
                        this.getDataCategory(), this.getResultType(), params);

            } else if (this.getElementType().equals(DBSFields.ATTRIBUTE)) { /* write attribute */
                this.addAttribute(xmlWriter, this.getName(),
                        paramValue, this.getXsdType(), this.getResultType());
            }
        } catch (XMLStreamException e) {
            throw new DataServiceFault(e, "Error in XML generation at StaticOutputElement.execute");
        }
    }

    private ExternalParam getParamObj(ExternalParamCollection params) throws DataServiceFault {
        ExternalParam exParam = params.getParam(this.getParamType(), this.getParam());
        if (exParam != null) {
            /* Returns an external parameter object corresponds to a SCALAR output value */
            return exParam;
        }
        if (this.isUserDefinedObj()) {
            ParamValue processedParamValue;
            exParam = params.getParam(this.getParamType(), this.getUDTInfo().getUDTColumnName());

            /* Retrieves the value of a User Defined Object */
            ParamValue value = exParam.getValue();
            if (DBUtils.isUDT(value)) {
                try {
                    /* Retrieves value of the desired UDT attribute */
                    processedParamValue = getUDTAttributeValue(this.getUDTInfo(), value,
                            StaticOutputElement.UDT_ATTRIBUTE_INITIAL_INDEX);

                    return new ExternalParam(this.getParam(), processedParamValue,
                            this.getParamType());
                } catch (SQLException e) {
                    throw new DataServiceFault(e, "Unable to retrieve UDT attribute value " +
                            "referred by '" + this.getParam() + "'");
                }
            }
            if (DBUtils.isSQLArray(value)) {
                processedParamValue = new ParamValue(ParamValue.PARAM_VALUE_ARRAY);
                this.getExParamFromArray(processedParamValue, value);

                return new ExternalParam(this.getParam(), processedParamValue,
                        this.getParamType());
            }
        }
        return exParam;
    }

    /**
     * Extracts out an External parameter object representing an Array type ParamValue object.
     *
     * @param processedParamValue   Processed parameter value
     * @param rawParamValue         Un processed parameter value
     * @throws DataServiceFault     Throws when the process is confronted with issues while processing
     *                              the UDT attributes.
     */
    private void getExParamFromArray(ParamValue processedParamValue,
                                     ParamValue rawParamValue) throws DataServiceFault {
        for (ParamValue value : rawParamValue.getArrayValue()) {
            if (DBUtils.isUDT(value)) {
                try {
                    processedParamValue.getArrayValue().add(getUDTAttributeValue(this.getUDTInfo(),
                            value, StaticOutputElement.UDT_ATTRIBUTE_INITIAL_INDEX));
                } catch (SQLException e) {
                    throw new DataServiceFault(e, "Unable to retrieve UDT attribute value " +
                            "referred by '" + this.getParam() + "'");
                }
            } else if (DBUtils.isSQLArray(value)) {
                this.getExParamFromArray(processedParamValue, value);
            } else {
                processedParamValue.getArrayValue().add(value);
            }
        }
    }


    /**
     * This method traverse through the specified indices and recursively retrieves the value of
     * the UDT attribute.
     *
     * @param udtInfo UDT metadata container
     * @param value   Value of the UDT attribute.
     * @param i       Index to keep track of the number of items process in the index list.
     * @return Final value of the desired UDT attribute
     * @throws SQLException     SQLException
     * @throws DataServiceFault DataServiceFault.
     */
    private static ParamValue getUDTAttributeValue(UDT udtInfo, ParamValue value,
                                                   int i) throws SQLException, DataServiceFault {
        List<Integer> indices = udtInfo.getIndices();
        if (DBUtils.isUDT(value)) {
            try {
                Object tempValue = value.getUdt().getAttributes()[indices.get(i)];
                if (tempValue instanceof Struct) {
                    value = new ParamValue((Struct) tempValue);
                } else if (tempValue instanceof Array) {
                    value = DBUtils.processSQLArray((Array) tempValue,
                            new ParamValue(ParamValue.PARAM_VALUE_ARRAY));
                } else {
                    value = new ParamValue(String.valueOf(tempValue));
                }
            } catch (Exception e) {
                throw new DataServiceFault("Unable to retrieve UDT attribute value referred by " +
                        "the given index");
            }
        } else if (DBUtils.isSQLArray(value)) {
            ParamValue processedParamValue = new ParamValue(ParamValue.PARAM_VALUE_ARRAY);
            for (ParamValue param : value.getArrayValue()) {
                if (DBUtils.isUDT(param)) {
                    processedParamValue.getArrayValue().add(new ParamValue(
                            String.valueOf(param.getUdt().getAttributes()[indices.get(i)])));
                } else {
                    processedParamValue.getArrayValue().add(param);
                }
            }
            value = processedParamValue;
        } else {
            return value;
        }
        i++;
        if (i < indices.size()) {
            return getUDTAttributeValue(udtInfo, value, i);
        }
        return value;
    }

    public boolean equals(Object o) {
        return (o instanceof StaticOutputElement) &&
                (((StaticOutputElement) o).getName().equals(this.getName()));
    }

    /* Acts as a container for UDT related metadata */
    private class UDT {

        private String udtColumnName;

        private List<Integer> indices;

        public UDT (String udtColumnName, List<Integer> indices) {
            this.udtColumnName = udtColumnName;
            this.indices = indices;
        }

        public String getUDTColumnName() {
            return udtColumnName;
        }

        public List<Integer> getIndices() {
            return indices;
        }

    }


}
