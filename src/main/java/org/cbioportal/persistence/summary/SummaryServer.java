package org.cbioportal.persistence.summary;

import java.util.List;

public class SummaryServer {

    private String name;
    private String baseUrl;
    private List<String> studyIds;
    private List<String> supportedEndpoints;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public List<String> getStudyIds() {
        return studyIds;
    }

    public void setStudyIds(List<String> studyIds) {
        this.studyIds = studyIds;
    }

    public List<String> getSupportedEndpoints() {
        return supportedEndpoints;
    }

    public void setSupportedEndpoints(List<String> supportedEndpoints) {
        this.supportedEndpoints = supportedEndpoints;
    }
}