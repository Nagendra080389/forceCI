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
 * <p>Java class for hudson.plugins.ws__cleanup.PreBuildCleanupType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="hudson.plugins.ws__cleanup.PreBuildCleanupType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="deleteDirs" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="cleanupParameter" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="externalDelete" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *       &lt;attribute name="plugin" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "hudson.plugins.ws__cleanup.PreBuildCleanupType", propOrder = {
    "deleteDirs",
    "cleanupParameter",
    "externalDelete"
})
public class HudsonPluginsWsCleanupPreBuildCleanupType {

    @XmlElement(required = true)
    protected String deleteDirs;
    @XmlElement(required = true)
    protected String cleanupParameter;
    @XmlElement(required = true)
    protected String externalDelete;
    @XmlAttribute(name = "plugin")
    protected String plugin;

    /**
     * Gets the value of the deleteDirs property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDeleteDirs() {
        return deleteDirs;
    }

    /**
     * Sets the value of the deleteDirs property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeleteDirs(String value) {
        this.deleteDirs = value;
    }

    /**
     * Gets the value of the cleanupParameter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCleanupParameter() {
        return cleanupParameter;
    }

    /**
     * Sets the value of the cleanupParameter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCleanupParameter(String value) {
        this.cleanupParameter = value;
    }

    /**
     * Gets the value of the externalDelete property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExternalDelete() {
        return externalDelete;
    }

    /**
     * Sets the value of the externalDelete property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExternalDelete(String value) {
        this.externalDelete = value;
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
