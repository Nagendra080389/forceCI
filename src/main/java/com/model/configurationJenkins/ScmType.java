//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.10.20 at 01:14:19 PM IST 
//


package com.model.configurationJenkins;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for scmType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="scmType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="configVersion" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="userRemoteConfigs" type="{}userRemoteConfigsType"/>
 *         &lt;element name="branches" type="{}branchesType"/>
 *         &lt;element name="doGenerateSubmoduleConfigurations" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="submoduleCfg" type="{}submoduleCfgType"/>
 *         &lt;element name="extensions" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *       &lt;attribute name="class" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="plugin" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "scmType", propOrder = {
    "configVersion",
    "userRemoteConfigs",
    "branches",
    "doGenerateSubmoduleConfigurations",
    "submoduleCfg",
    "extensions"
})
public class ScmType {

    @XmlElement(required = true)
    protected String configVersion;
    @XmlElement(required = true)
    protected UserRemoteConfigsType userRemoteConfigs;
    @XmlElement(required = true)
    protected BranchesType branches;
    @XmlElement(required = true)
    protected String doGenerateSubmoduleConfigurations;
    @XmlElement(required = true)
    protected SubmoduleCfgType submoduleCfg;
    @XmlElement(required = true)
    protected String extensions;
    @XmlAttribute(name = "class")
    protected String clazz;
    @XmlAttribute(name = "plugin")
    protected String plugin;

    /**
     * Gets the value of the configVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConfigVersion() {
        return configVersion;
    }

    /**
     * Sets the value of the configVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConfigVersion(String value) {
        this.configVersion = value;
    }

    /**
     * Gets the value of the userRemoteConfigs property.
     * 
     * @return
     *     possible object is
     *     {@link UserRemoteConfigsType }
     *     
     */
    public UserRemoteConfigsType getUserRemoteConfigs() {
        return userRemoteConfigs;
    }

    /**
     * Sets the value of the userRemoteConfigs property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserRemoteConfigsType }
     *     
     */
    public void setUserRemoteConfigs(UserRemoteConfigsType value) {
        this.userRemoteConfigs = value;
    }

    /**
     * Gets the value of the branches property.
     * 
     * @return
     *     possible object is
     *     {@link BranchesType }
     *     
     */
    public BranchesType getBranches() {
        return branches;
    }

    /**
     * Sets the value of the branches property.
     * 
     * @param value
     *     allowed object is
     *     {@link BranchesType }
     *     
     */
    public void setBranches(BranchesType value) {
        this.branches = value;
    }

    /**
     * Gets the value of the doGenerateSubmoduleConfigurations property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDoGenerateSubmoduleConfigurations() {
        return doGenerateSubmoduleConfigurations;
    }

    /**
     * Sets the value of the doGenerateSubmoduleConfigurations property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDoGenerateSubmoduleConfigurations(String value) {
        this.doGenerateSubmoduleConfigurations = value;
    }

    /**
     * Gets the value of the submoduleCfg property.
     * 
     * @return
     *     possible object is
     *     {@link SubmoduleCfgType }
     *     
     */
    public SubmoduleCfgType getSubmoduleCfg() {
        return submoduleCfg;
    }

    /**
     * Sets the value of the submoduleCfg property.
     * 
     * @param value
     *     allowed object is
     *     {@link SubmoduleCfgType }
     *     
     */
    public void setSubmoduleCfg(SubmoduleCfgType value) {
        this.submoduleCfg = value;
    }

    /**
     * Gets the value of the extensions property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExtensions() {
        return extensions;
    }

    /**
     * Sets the value of the extensions property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExtensions(String value) {
        this.extensions = value;
    }

    /**
     * Gets the value of the clazz property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClazz() {
        return clazz;
    }

    /**
     * Sets the value of the clazz property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClazz(String value) {
        this.clazz = value;
    }

    /**
     * Gets the value of the plugin property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPlugin() {
        return plugin;
    }

    /**
     * Sets the value of the plugin property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPlugin(String value) {
        this.plugin = value;
    }

}
