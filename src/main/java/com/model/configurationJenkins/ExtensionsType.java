//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.10.20 at 01:14:19 PM IST 
//


package com.model.configurationJenkins;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for extensionsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="extensionsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="org.jenkinsci.plugins.ghprb.extensions.status.GhprbSimpleStatus" type="{}org.jenkinsci.plugins.ghprb.extensions.status.GhprbSimpleStatusType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "extensionsType", propOrder = {
    "orgJenkinsciPluginsGhprbExtensionsStatusGhprbSimpleStatus"
})
public class ExtensionsType {

    @XmlElement(name = "org.jenkinsci.plugins.ghprb.extensions.status.GhprbSimpleStatus", required = true)
    protected OrgJenkinsciPluginsGhprbExtensionsStatusGhprbSimpleStatusType orgJenkinsciPluginsGhprbExtensionsStatusGhprbSimpleStatus;

    /**
     * Gets the value of the orgJenkinsciPluginsGhprbExtensionsStatusGhprbSimpleStatus property.
     * 
     * @return
     *     possible object is
     *     {@link OrgJenkinsciPluginsGhprbExtensionsStatusGhprbSimpleStatusType }
     *     
     */
    public OrgJenkinsciPluginsGhprbExtensionsStatusGhprbSimpleStatusType getOrgJenkinsciPluginsGhprbExtensionsStatusGhprbSimpleStatus() {
        return orgJenkinsciPluginsGhprbExtensionsStatusGhprbSimpleStatus;
    }

    /**
     * Sets the value of the orgJenkinsciPluginsGhprbExtensionsStatusGhprbSimpleStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link OrgJenkinsciPluginsGhprbExtensionsStatusGhprbSimpleStatusType }
     *     
     */
    public void setOrgJenkinsciPluginsGhprbExtensionsStatusGhprbSimpleStatus(OrgJenkinsciPluginsGhprbExtensionsStatusGhprbSimpleStatusType value) {
        this.orgJenkinsciPluginsGhprbExtensionsStatusGhprbSimpleStatus = value;
    }

}
