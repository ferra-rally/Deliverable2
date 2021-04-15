package it.deliverable2;

import java.time.ZonedDateTime;
import java.util.List;

public class Issue {
    private String name;
    private String injectVersion;
    private String fixVersion;
    private ZonedDateTime resolutionDate;
    private List<RepoFile> affects;

    public Issue(String name, List<String> affected, List<String> fixed, ZonedDateTime resolutionDate) {
        this.name = name;
        this.resolutionDate = resolutionDate;
        this.fixVersion = "";

        if(!fixed.isEmpty()) {
            this.fixVersion = fixed.get(0);

            for(int i = 1; i < fixed.size(); i++) {
                if(!Utils.compareVersionString(this.fixVersion, fixed.get(i))) this.fixVersion = fixed.get(i);
            }
        }

        if(!affected.isEmpty()) {
            this.injectVersion = affected.get(0);

            for(int i = 1; i < affected.size(); i++) {
                if(Utils.compareVersionString(this.injectVersion, affected.get(i))) this.injectVersion = affected.get(i);
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

    public String getInjectVersion() {
        return injectVersion;
    }

    public void setInjectVersion(String injectVersion) {
        this.injectVersion = injectVersion;
    }

    public String getFixVersion() {
        return fixVersion;
    }

    public void setFixVersion(String fixVersion) {
        this.fixVersion = fixVersion;
    }

    public ZonedDateTime getResolutionDate() {
        return resolutionDate;
    }

    public void setResolutionDate(ZonedDateTime resolutionDate) {
        this.resolutionDate = resolutionDate;
    }
}
