package it.deliverable2;

import java.time.ZonedDateTime;
import java.util.List;

public class Issue {
    private String name;
    private Release injectVersion;
    private Release fixVersion;
    private Release openingVersion;
    private ZonedDateTime resolutionDate;
    private List<CommitFile> affects;

    public Issue(String name, Release injectVersion, Release fixVersion, Release openingVersion, ZonedDateTime resolutionDate) {
        this.name = name;
        this.resolutionDate = resolutionDate;
        this.fixVersion = fixVersion;
        this.injectVersion = injectVersion;
        this.openingVersion = openingVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CommitFile> getAffects() {
        return affects;
    }

    public void setAffects(List<CommitFile> affects) {
        this.affects = affects;
    }

    public Release getInjectVersion() {
        return injectVersion;
    }

    public void setInjectVersion(Release injectVersion) {
        this.injectVersion = injectVersion;
    }

    public Release getFixVersion() {
        return fixVersion;
    }

    public void setFixVersion(Release fixVersion) {
        this.fixVersion = fixVersion;
    }

    public ZonedDateTime getResolutionDate() {
        return resolutionDate;
    }

    public void setResolutionDate(ZonedDateTime resolutionDate) {
        this.resolutionDate = resolutionDate;
    }

    public Release getOpeningVersion() {
        return openingVersion;
    }

    public void setOpeningVersion(Release openingVersion) {
        this.openingVersion = openingVersion;
    }
}
