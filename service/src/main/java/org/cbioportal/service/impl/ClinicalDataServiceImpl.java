package org.cbioportal.service.impl;

import org.cbioportal.model.*;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.ClinicalDataRepository;
import org.cbioportal.service.*;
import org.cbioportal.service.exception.*;
import org.cbioportal.service.util.ClinicalAttributeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.cbioportal.utils.Encoding.calculateBase64;

@Service
public class ClinicalDataServiceImpl implements ClinicalDataService {

    @Autowired
    private ClinicalDataRepository clinicalDataRepository;
    @Autowired
    private StudyService studyService;
    @Autowired
    private PatientService patientService;
    @Autowired
    private SampleService sampleService;
    @Autowired
    private ClinicalAttributeService clinicalAttributeService;
    @Autowired
    private ClinicalAttributeUtil clinicalAttributeUtil ;

    @Override
    public List<ClinicalData> getAllClinicalDataOfSampleInStudy(String studyId, String sampleId, String attributeId, 
                                                                String projection, Integer pageSize, Integer pageNumber,
                                                                String sortBy, String direction)
        throws SampleNotFoundException, StudyNotFoundException {

        sampleService.getSampleInStudy(studyId, sampleId);
        
        return clinicalDataRepository.getAllClinicalDataOfSampleInStudy(studyId, sampleId, attributeId, projection,
                pageSize, pageNumber, sortBy, direction);
    }

    @Override
    public BaseMeta getMetaSampleClinicalData(String studyId, String sampleId, String attributeId)
        throws SampleNotFoundException, StudyNotFoundException {

        sampleService.getSampleInStudy(studyId, sampleId);

        return clinicalDataRepository.getMetaSampleClinicalData(studyId, sampleId, attributeId);
    }

    @Override
    public List<ClinicalData> getAllClinicalDataOfPatientInStudy(String studyId, String patientId, String attributeId, 
                                                                 String projection, Integer pageSize, 
                                                                 Integer pageNumber, String sortBy, String direction)
        throws PatientNotFoundException, StudyNotFoundException {
        
        patientService.getPatientInStudy(studyId, patientId);

        return clinicalDataRepository.getAllClinicalDataOfPatientInStudy(studyId, patientId, attributeId, projection,
                pageSize, pageNumber, sortBy, direction);
    }

    @Override
    public BaseMeta getMetaPatientClinicalData(String studyId, String patientId, String attributeId)
        throws PatientNotFoundException, StudyNotFoundException {

        patientService.getPatientInStudy(studyId, patientId);

        return clinicalDataRepository.getMetaPatientClinicalData(studyId, patientId, attributeId);
    }

    @Override
    public List<ClinicalData> getAllClinicalDataInStudy(String studyId, String attributeId, String clinicalDataType, 
                                                        String projection, Integer pageSize, Integer pageNumber,
                                                        String sortBy, String direction) throws StudyNotFoundException {
        
        studyService.getStudy(studyId);

        return clinicalDataRepository.getAllClinicalDataInStudy(studyId, attributeId, clinicalDataType, projection,
                pageSize, pageNumber, sortBy, direction);
    }

    @Override
    public BaseMeta getMetaAllClinicalData(String studyId, String attributeId, String clinicalDataType) 
        throws StudyNotFoundException {

        studyService.getStudy(studyId);
        
        return clinicalDataRepository.getMetaAllClinicalData(studyId, attributeId, clinicalDataType);
    }

    @Override
    public List<ClinicalData> fetchAllClinicalDataInStudy(String studyId, List<String> ids, List<String> attributeIds,
                                                          String clinicalDataType, String projection) 
        throws StudyNotFoundException {

        studyService.getStudy(studyId);
        
        return clinicalDataRepository.fetchAllClinicalDataInStudy(studyId, ids, attributeIds, clinicalDataType, 
            projection);
    }

    @Override
    public BaseMeta fetchMetaClinicalDataInStudy(String studyId, List<String> ids, List<String> attributeIds,
                                                 String clinicalDataType) throws StudyNotFoundException {

        studyService.getStudy(studyId);
        
        return clinicalDataRepository.fetchMetaClinicalDataInStudy(studyId, ids, attributeIds, clinicalDataType);
    }

    @Override
    public List<ClinicalData> fetchClinicalData(List<String> studyIds, List<String> ids, List<String> attributeIds,
                                                String clinicalDataType, String projection) {
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        return clinicalDataRepository.fetchClinicalData(studyIds, ids, attributeIds, clinicalDataType, projection);
    }

    @Override
    public BaseMeta fetchMetaClinicalData(List<String> studyIds, List<String> ids, List<String> attributeIds, 
                                          String clinicalDataType) {

        return clinicalDataRepository.fetchMetaClinicalData(studyIds, ids, attributeIds, clinicalDataType);
    }

	@Override
	public List<ClinicalDataCountItem> fetchClinicalDataCounts(List<String> studyIds, List<String> sampleIds,
            List<String> attributeIds) {

        if (attributeIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<ClinicalAttribute> clinicalAttributes = clinicalAttributeService
                .getClinicalAttributesByStudyIdsAndAttributeIds(studyIds, attributeIds);

        List<String> sampleAttributeIds = new ArrayList<>();
        List<String> patientAttributeIds = new ArrayList<>();
        // patient attributes which are also sample attributes in other studies
        List<String> conflictingPatientAttributeIds = new ArrayList<>();

        clinicalAttributeUtil.extractCategorizedClinicalAttributes(clinicalAttributes, sampleAttributeIds,
                patientAttributeIds, conflictingPatientAttributeIds);

        List<ClinicalDataCount> clinicalDataCounts = new ArrayList<ClinicalDataCount>();
        if (!sampleAttributeIds.isEmpty()) {
            clinicalDataCounts.addAll(clinicalDataRepository.fetchClinicalDataCounts(studyIds, sampleIds,
                    sampleAttributeIds, "SAMPLE", "SUMMARY"));
        }

        if (!patientAttributeIds.isEmpty()) {
            clinicalDataCounts.addAll(clinicalDataRepository.fetchClinicalDataCounts(studyIds, sampleIds,
                    patientAttributeIds, "PATIENT", "SUMMARY"));
        }

        if (!conflictingPatientAttributeIds.isEmpty()) {
            clinicalDataCounts.addAll(clinicalDataRepository.fetchClinicalDataCounts(studyIds, sampleIds,
                    conflictingPatientAttributeIds, "PATIENT", "DETAILED"));
        }

        sampleAttributeIds.addAll(conflictingPatientAttributeIds);

        clinicalDataCounts = clinicalDataCounts
                .stream().filter(c -> !c.getValue().toUpperCase().equals("NA")
                        && !c.getValue().toUpperCase().equals("NAN") && !c.getValue().toUpperCase().equals("N/A"))
                .collect(Collectors.toList());

        Map<String, List<ClinicalDataCount>> clinicalDataCountMap = clinicalDataCounts.stream()
                .collect(Collectors.groupingBy(ClinicalDataCount::getAttributeId));

        List<Patient> patients = new ArrayList<Patient>();
        if (!patientAttributeIds.isEmpty()) {
            patients.addAll(patientService.getPatientsOfSamples(studyIds, sampleIds));
        }

        HashSet<String> uniqueAttributeIds = new HashSet<>(attributeIds);

        return uniqueAttributeIds.stream().map(attributeId -> {

            int naCount = 0;
            int totalCount = 0;
            List<ClinicalDataCount> counts = clinicalDataCountMap.getOrDefault(attributeId, new ArrayList<>());

            if (conflictingPatientAttributeIds.contains(attributeId)) {
                // if its a conflicting attribute then sum all counts
                counts = counts.stream().collect(Collectors.toMap(ClinicalDataCount::getValue, Function.identity(),
                        (clinicalDataCount1, clinicalDataCount2) -> {
                            clinicalDataCount1.setCount(clinicalDataCount1.getCount() + clinicalDataCount2.getCount());
                            return clinicalDataCount1;
                        })).values().stream().collect(Collectors.toList());
            }

            if (!counts.isEmpty()) {
                totalCount = counts.stream().mapToInt(ClinicalDataCount::getCount).sum();
            }

            if (sampleAttributeIds.contains(attributeId)) {
                naCount = sampleIds.size() - totalCount;
            } else {
                naCount = patients.size() - totalCount;
            }

            if (naCount > 0) {
                ClinicalDataCount clinicalDataCount = new ClinicalDataCount();
                clinicalDataCount.setAttributeId(attributeId);
                clinicalDataCount.setValue("NA");
                clinicalDataCount.setCount(naCount);
                counts.add(clinicalDataCount);
            }

            ClinicalDataCountItem clinicalDataCountItem = new ClinicalDataCountItem();
            clinicalDataCountItem.setAttributeId(attributeId);
            clinicalDataCountItem.setCounts(counts);
            return clinicalDataCountItem;

        }).collect(Collectors.toList());
    }

    @Override
    public List<ClinicalData> getPatientClinicalDataDetailedToSample(List<String> studyIds, List<String> patientIds,
            List<String> attributeIds) {
        return clinicalDataRepository.getPatientClinicalDataDetailedToSample(studyIds, patientIds, attributeIds);
    }

    @Override
    public SampleClinicalDataCollection fetchSampleClinicalTable(List<String> studyIds, List<String> sampleIds, Integer pageSize, Integer pageNumber, String searchTerm, String sortBy, String direction) {

        SampleClinicalDataCollection sampleClinicalDataCollection = new SampleClinicalDataCollection();
        if (studyIds == null || studyIds.isEmpty() || sampleIds == null || sampleIds.isEmpty()) {
            return sampleClinicalDataCollection;
        }
        
        List<Integer> visibleSampleInternalIds = clinicalDataRepository.getVisibleSampleInternalIdsForClinicalTable(
            studyIds, sampleIds,
            pageSize, pageNumber,
            searchTerm, sortBy, direction
        );

        List<ClinicalData> clinicalData = clinicalDataRepository.getSampleAndPatientClinicalDataBySampleInternalIds(
            visibleSampleInternalIds
        );

        sampleClinicalDataCollection.setByUniqueSampleKey(clinicalData.stream().collect(Collectors.groupingBy(clinicalDatum ->
            calculateBase64(clinicalDatum.getSampleId(), clinicalDatum.getStudyId())
        )));
        
        return sampleClinicalDataCollection;
    }
    
    /*
        Aggregate ClinicalData objects into a single Map. Keys are clinical attribute
        identifiers and the values are respective clinical attribute values. ClinicalData is 
        assumed to be of the same sample. Sample, patient and study identifiers are
        added to the output. Names for sample, patient and study identifiers
    */
    private Map<String, String> aggregateSampleClinicalData(List<ClinicalData> clinicalData) {
        if (clinicalData.isEmpty()) {
            return new HashMap<>();
        }
        Map<String, String> returnData = Map.of(
            "sampleId", clinicalData.get(0).getSampleId(),
            "patientId", clinicalData.get(0).getPatientId(),
            "studyId", clinicalData.get(0).getStudyId()
        );
        clinicalData.forEach(clinicalDatum -> returnData.put(clinicalDatum.getAttrId(), clinicalDatum.getAttrValue()));
        return returnData;
    }

}