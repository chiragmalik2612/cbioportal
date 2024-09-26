package org.cbioportal.service;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalDataBin;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.web.parameter.DataBinMethod;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.StudyViewFilter;

import java.util.List;

public interface EnclaveService {

    List<ClinicalDataCountItem> fetchEnclaveClinicalDataCounts(
        List<String> attributes,
        StudyViewFilter studyViewFilter
    );
    
    List<ClinicalDataBin> fetchEnclaveClinicalDataBinCounts(
        DataBinMethod dataBinMethod,
        List<String> attributes,
        StudyViewFilter studyViewFilter
    );

    List<ClinicalAttribute> fetchEnclaveClinicalAttributes(
        Projection projection
    );
}