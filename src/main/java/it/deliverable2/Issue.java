package it.deliverable2;

import java.time.ZonedDateTime;
import java.util.List;

public class Issue {
    private String name;
    private Release injectVersion;
    private Release fixVersion;
    private ZonedDateTime resolutionDate;
    private List<RepoFile> affects;

    public Issue(String name, List<Release> affected, List<Release> fixed, ZonedDateTime resolutionDate) {
        this.name = name;
        this.resolutionDate = resolutionDate;

        if(!fixed.isEmpty()) {
            this.fixVersion = fixed.get(0);

            for(int i = 1; i < fixed.size(); i++) {
                if(this.fixVersion.compareTo(fixed.get(i)) < 0) this.fixVersion = fixed.get(i);
            }
        }

        if(!affected.isEmpty()) {
            this.injectVersion = affected.get(0);

            for(int i = 1; i < affected.size(); i++) {
                if(this.injectVersion.compareTo(affected.get(i)) > 0) this.injectVersion = affected.get(i);
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<RepoFile> getAffects() {
        return affects;
    }

    public void setAffects(List<RepoFile> affects) {
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
}
