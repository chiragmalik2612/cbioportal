package org.cbioportal.web.columnar.util;

import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.Sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClinicalDataViolinPlotUtil {
    public static List<ClinicalData> filterNonEmptyClinicalData(List<ClinicalData> clinicalData) {
        return clinicalData
            .stream()
            .filter(data -> !data.getAttrValue().isEmpty())
            .toList();
    }
    
    public static List<ClinicalData> convertPatientClinicalDataToSampleClinicalData(
        List<ClinicalData> patientClinicalDataList,
        List<Sample> filteredSamples
    ) {
        List<ClinicalData> sampleClinicalDataList = new ArrayList<>();

        Map<String, Map<String, List<Sample>>> patientToSamples = filteredSamples
            .stream()
            .collect(Collectors.groupingBy(
                s -> s.getCancerStudyIdentifier() + "_" + s.getPatientStableId(),
                Collectors.groupingBy(Sample::getCancerStudyIdentifier)
            ));

        // put all clinical data into sample form
        for (ClinicalData d: patientClinicalDataList) {
            List<Sample> samplesForPatient = patientToSamples.get(d.getPatientId()).get(d.getStudyId());
            if (samplesForPatient != null) {
                for (Sample s: samplesForPatient) {
                    ClinicalData newData = new ClinicalData();

                    newData.setInternalId(s.getInternalId());
                    newData.setAttrId(d.getAttrId());
                    newData.setPatientId(d.getPatientId());
                    newData.setStudyId(d.getStudyId());
                    newData.setAttrValue(d.getAttrValue());
                    newData.setSampleId(s.getCancerStudyIdentifier() + "_" + s.getStableId());

                    sampleClinicalDataList.add(newData);
                }
            } else {
                // TODO ignoring for now rather than throwing an error
                // patient has no samples - this shouldn't happen and could affect the integrity
                //  of the data analysis
                // return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return sampleClinicalDataList;
    }
}