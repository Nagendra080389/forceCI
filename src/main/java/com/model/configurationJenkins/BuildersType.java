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
 * <p>Java class for buildersType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="buildersType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="hudson.tasks.Ant" type="{}hudson.tasks.AntType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "buildersType", propOrder = {
        "hudsonTasksAnt"
})
public class BuildersType {

    @XmlElement(name = "hudson.tasks.Ant", required = true)
    protected HudsonTasksAntType hudsonTasksAnt;

    /**
     * Gets the value of the hudsonTasksAnt property.
     *
     * @return possible object is
     * {@link HudsonTasksAntType }
     */
    public HudsonTasksAntType getHudsonTasksAnt() {
        return hudsonTasksAnt;
    }

    /**
     * Sets the value of the hudsonTasksAnt property.
     *
     * @param value allowed object is
     *              {@link HudsonTasksAntType }
     */
    public void setHudsonTasksAnt(HudsonTasksAntType value) {
        this.hudsonTasksAnt = value;
    }

}
