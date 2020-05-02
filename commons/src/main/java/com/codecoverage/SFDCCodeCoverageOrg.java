package com.codecoverage;

import com.model.SFDCConnectionDetails;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class SFDCCodeCoverageOrg implements Serializable {

    @Id
    private String id;
    @Indexed
    private String scheduledJobId;
    private List<SFDCCodeCoverageDetails> lstSfdcCodeCoverageDetailsTests;
    private List<SFDCCodeCoverageDetails> lstSfdcCodeCoverageDetails;
    private Double orgCoverage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getScheduledJobId() {
        return scheduledJobId;
    }

    public void setScheduledJobId(String scheduledJobId) {
        this.scheduledJobId = scheduledJobId;
    }

    public List<SFDCCodeCoverageDetails> getLstSfdcCodeCoverageDetails() {
        return lstSfdcCodeCoverageDetails;
    }

    public void setLstSfdcCodeCoverageDetails(List<SFDCCodeCoverageDetails> lstSfdcCodeCoverageDetails) {
        this.lstSfdcCodeCoverageDetails = lstSfdcCodeCoverageDetails;
    }

    public Double getOrgCoverage() {
        return orgCoverage;
    }

    public void setOrgCoverage(Double orgCoverage) {
        this.orgCoverage = orgCoverage;
    }

    public List<SFDCCodeCoverageDetails> getLstSfdcCodeCoverageDetailsTests() {
        return lstSfdcCodeCoverageDetailsTests;
    }

    public void setLstSfdcCodeCoverageDetailsTests(List<SFDCCodeCoverageDetails> lstSfdcCodeCoverageDetailsTests) {
        this.lstSfdcCodeCoverageDetailsTests = lstSfdcCodeCoverageDetailsTests;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SFDCCodeCoverageOrg that = (SFDCCodeCoverageOrg) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(scheduledJobId, that.scheduledJobId) &&
                Objects.equals(lstSfdcCodeCoverageDetailsTests, that.lstSfdcCodeCoverageDetailsTests) &&
                Objects.equals(lstSfdcCodeCoverageDetails, that.lstSfdcCodeCoverageDetails) &&
                Objects.equals(orgCoverage, that.orgCoverage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, scheduledJobId, lstSfdcCodeCoverageDetailsTests, lstSfdcCodeCoverageDetails, orgCoverage);
    }
}
