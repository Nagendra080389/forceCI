//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.10.20 at 01:14:19 PM IST 
//


package com.model.configurationJenkins;

import javax.xml.bind.annotation.*;


/**
 * <p>Java class for hudson.tasks.AntType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="hudson.tasks.AntType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="targets" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="antName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="buildFile" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="properties" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *       &lt;attribute name="plugin" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "hudson.tasks.AntType", propOrder = {
        "targets",
        "antName",
        "buildFile",
        "properties"
})
public class HudsonTasksAntType {

    @XmlElement(required = true)
    protected String targets;
    @XmlElement(required = true)
    protected String antName;
    @XmlElement(required = true)
    protected String buildFile;
    @XmlElement(required = true)
    protected String properties;
    @XmlAttribute(name = "plugin")
    protected String plugin;

    /**
     * Gets the value of the targets property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getTargets() {
        return targets;
    }

    /**
     * Sets the value of the targets property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTargets(String value) {
        this.targets = value;
    }

    /**
     * Gets the value of the antName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getAntName() {
        return antName;
    }

    /**
     * Sets the value of the antName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAntName(String value) {
        this.antName = value;
    }

    /**
     * Gets the value of the buildFile property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getBuildFile() {
        return buildFile;
    }

    /**
     * Sets the value of the buildFile property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setBuildFile(String value) {
        this.buildFile = value;
    }

    /**
     * Gets the value of the properties property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getProperties() {
        return properties;
    }

    /**
     * Sets the value of the properties property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setProperties(String value) {
        this.properties = value;
    }

    /**
     * Gets the value of the plugin property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPlugin() {
        return plugin;
    }

    /**
     * Sets the value of the plugin property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPlugin(String value) {
        this.plugin = value;
    }

}
