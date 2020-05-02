package com.codecoverage;

import org.springframework.data.annotation.CreatedDate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class SFDCCodeCoverageDetails implements Serializable, Comparable<SFDCCodeCoverageDetails> {

    private String nameOfClassOrTrigger;
    private Integer linesUncovered;
    private Integer linesCovered;
    private List<SFDCCodeCoverage> sfdcCodeCoverageList = new ArrayList<>();

    public String getNameOfClassOrTrigger() {
        return nameOfClassOrTrigger;
    }

    public void setNameOfClassOrTrigger(String nameOfClassOrTrigger) {
        this.nameOfClassOrTrigger = nameOfClassOrTrigger;
    }

    public Integer getLinesUncovered() {
        return linesUncovered;
    }

    public void setLinesUncovered(Integer linesUncovered) {
        this.linesUncovered = linesUncovered;
    }

    public Integer getLinesCovered() {
        return linesCovered;
    }

    public void setLinesCovered(Integer linesCovered) {
        this.linesCovered = linesCovered;
    }

    public List<SFDCCodeCoverage> getSfdcCodeCoverageList() {
        return sfdcCodeCoverageList;
    }

    public void setSfdcCodeCoverageList(List<SFDCCodeCoverage> sfdcCodeCoverageList) {
        this.sfdcCodeCoverageList = sfdcCodeCoverageList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SFDCCodeCoverageDetails that = (SFDCCodeCoverageDetails) o;
        return Objects.equals(nameOfClassOrTrigger, that.nameOfClassOrTrigger) &&
                Objects.equals(linesUncovered, that.linesUncovered) &&
                Objects.equals(linesCovered, that.linesCovered) &&
                Objects.equals(sfdcCodeCoverageList, that.sfdcCodeCoverageList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nameOfClassOrTrigger, linesUncovered, linesCovered, sfdcCodeCoverageList);
    }

    @Override
    public int compareTo(SFDCCodeCoverageDetails sfdcCodeCoverageDetails) {
        return this.getNameOfClassOrTrigger().compareTo(sfdcCodeCoverageDetails.getNameOfClassOrTrigger());
    }
}
