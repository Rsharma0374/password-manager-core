package com.password.manager.service.impl;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;
import com.password.manager.configuration.MasterMappingConfiguration;
import com.password.manager.constant.Constants;
import com.password.manager.constant.ErrorCode;
import com.password.manager.dao.MongoService;
import com.password.manager.model.FileUploadStatus;
import com.password.manager.model.master.ApiRoleAuthenticationMasterFields;
import com.password.manager.model.master.ApiRoleAuthorisationMaster;
import com.password.manager.model.response.MasterRecords;
import com.password.manager.response.BaseResponse;
import com.password.manager.response.MasterValidationStatus;
import com.password.manager.service.FileUploadService;
import com.password.manager.utility.Utility;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Service
public class FileUploadServiceImpl implements FileUploadService {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadServiceImpl.class);

    @Autowired
    MongoService mongoService;

    MasterValidationStatus masterValidationStatus;

    @Override
    public BaseResponse uploadApiAuthenticationMaster(File file, String product) {
        logger.debug("Starting api authentication master upload.");

        FileUploadStatus fileUploadStatus = new FileUploadStatus();
        try {
            MasterMappingConfiguration masterMappingConfiguration = mongoService.getMasterMappingConfiguration(product, Constants.API_AUTHENTICATION_MASTER);

            if (masterMappingConfiguration != null) {
                masterValidationStatus = columnMappingCheck(file, masterMappingConfiguration);

                if (masterValidationStatus.isValid()) {
                    HashMap<String, String> columnMapping = new HashMap();
                    MasterRecords masterRecords;
                    for (ApiRoleAuthenticationMasterFields field : ApiRoleAuthenticationMasterFields.values()) {
                        columnMapping.put(field.name(), field.getValue());
                    }

                    if(!columnMapping.isEmpty()){
                        List<ApiRoleAuthorisationMaster> apiRoleAuthorisationMasters = transformCsvToList(file, columnMapping, ApiRoleAuthorisationMaster.class, fileUploadStatus, masterMappingConfiguration);

                        if (!CollectionUtils.isEmpty(apiRoleAuthorisationMasters)) {
                            apiRoleAuthorisationMasters.forEach( apiRoleAuthorisationMaster -> {
                                apiRoleAuthorisationMaster.setProduct(product);
                                apiRoleAuthorisationMaster.setActive(true);;
                            });
                        }

                        boolean dbStatus = mongoService.insertApiRoleAuthorisationMaster(apiRoleAuthorisationMasters, product);
                        masterRecords = new MasterRecords();
                        masterRecords.setMasterList(apiRoleAuthorisationMasters);
                        fileUploadStatus = setRecordStatus(dbStatus, masterRecords, fileUploadStatus);
                    }
                }else {
                    fileUploadStatus.setErrorDesc(String.format(Constants.WRONG_FILE_CONTENT, Constants.API_AUTHENTICATION_MASTER, masterValidationStatus.getMissingHeader()));
                }
            } else {
                fileUploadStatus.setErrorDesc(Constants.MASTER_MAPPING_CONFIGURATION_NOT_PRESENT);
                fileUploadStatus.setStatus(Constants.ERROR);
            }

        } catch (Exception e) {
            logger.error("Exception occurred while uploading api authentication master with probable cause- ", e);
            fileUploadStatus.setErrorDesc("Runtime exception with cause" + e.getMessage());
            fileUploadStatus.setStatus(Constants.ERROR);
        }
        return Utility.getBaseResponse(HttpStatus.OK, fileUploadStatus);
    }

    public MasterValidationStatus columnMappingCheck(File file, MasterMappingConfiguration masterMappingConfiguration) {
        MasterValidationStatus masterValidationStatus = new MasterValidationStatus();
        boolean valid;
        CSVReader headerReader;
        List<String> missingHeader = new ArrayList<>();
        try {
            headerReader = new CSVReader(new FileReader(file));
            String[] header = headerReader.readNext();
            List<String> headerlist = new ArrayList<>();
            for (String s : header) {
                headerlist.add(s.toUpperCase().trim());
            }
            Map<String, String> columnMapping = masterMappingConfiguration.getColumnMapping();
            List<String> columnlist = new ArrayList<>();
            String[] map = columnMapping.keySet().toArray(new String[0]);
            for (String s : map) {
                columnlist.add(s.toUpperCase().trim());
            }
            List<String> columnlisttemp = new ArrayList<>();
            columnlisttemp.addAll(columnlist);
            columnlisttemp.retainAll(headerlist);
            columnlist.removeAll(columnlisttemp);
            missingHeader.addAll(columnlist);
            valid = missingHeader.isEmpty();

            masterValidationStatus.setMissingHeader(missingHeader);
            masterValidationStatus.setValid(valid);

        } catch (Exception e) {
            logger.error("Error in columnMappingCheck" + e);
        }
        return masterValidationStatus;
    }

    private List transformCsvToList(File file, HashMap<String, String> destinationMapping, Class classname, FileUploadStatus fileUploadStatus,
                                    MasterMappingConfiguration masterMappingConfiguration) throws IOException {

        try(CSVReader headerReader=new CSVReader(new FileReader(file));
            CSVReader fileReader = new CSVReader(new FileReader(file))) {
            CsvToBean csv = new CsvToBean();

            String[] header = headerReader.readNext();
            List headerlist = Arrays.asList(header);

            Object[] key = destinationMapping.keySet().toArray();
            List keyList = Arrays.asList(key);

            List<String> matchFields = new ArrayList<String>(keyList);
            List<String> aditionalFields = new ArrayList<String>(headerlist);
            matchFields.retainAll(headerlist);
            aditionalFields.removeAll(keyList);
            List<String> missingFields = new ArrayList<String>(keyList);
            missingFields.removeAll(matchFields);
            fileUploadStatus.setAdditionalFields(String.valueOf(aditionalFields));
            logger.debug("matching fields in coloum and csv file" + matchFields);
            logger.debug("additional fields is present in csv file" + aditionalFields);
            logger.debug("missing fields in csv file which are present in bean file" + missingFields);
            Map<String, String> columnMappingMap = masterMappingConfiguration.getColumnMapping();
            Map<String, String> sourceColumnToBeanMapping = new HashMap<>();
            columnMappingMap.keySet().forEach(sourceColumnName -> {
                String destinationColumnName = columnMappingMap.get(sourceColumnName);
                if (destinationMapping.containsKey(destinationColumnName)) {
                    String beanName = destinationMapping.get(destinationColumnName);
                    sourceColumnToBeanMapping.put(sourceColumnName, beanName);
                }else{
                    logger.error("Missing column:{}",destinationColumnName);
                }
            });
            if (columnMappingMap.size() == sourceColumnToBeanMapping.size()) {
                HeaderColumnNameTranslateMappingStrategy strategy = new HeaderColumnNameTranslateMappingStrategy();
                strategy.setType(classname);
                strategy.setColumnMapping(sourceColumnToBeanMapping);
                //Parse the file
                return csv.parse(strategy, fileReader);
            } else {
                fileUploadStatus.setStatus(Constants.FAILED);
                fileUploadStatus.setErrorDesc(ErrorCode.COLUMN_MAPPING_IS_INCORRECT);
                return null;
            }
        }catch (Exception e){
            logger.error("Error occurred due to ",e);
            fileUploadStatus.setStatus(Constants.ERROR);
            if(e.getCause() != null && e.getCause().toString().contains(ErrorCode.NUMBER_FORMAT_EXCEPTION_STRING))
                fileUploadStatus.setErrorDesc(ErrorCode.COLUMN_VALUE_SHOULD_NOT_BE_EMPTY);
            else if(e.getCause() != null && e.getCause().toString().contains(ErrorCode.NUMBER_FORMAT_EXCEPTION_FOR_INPUT_STRING))
                fileUploadStatus.setErrorDesc(ErrorCode.COLUMN_VALUE_SHOULD_BE_NUMERIC);
            else  fileUploadStatus.setErrorDesc(ErrorCode.RUNTIME_EXCEPTION + e.getMessage());
            return null;
        }

    }

    private FileUploadStatus setRecordStatus(boolean dbStatus, MasterRecords masterRecords, FileUploadStatus fileUploadStatus) {
        // FileUploadStatus fileUploadStatus=new FileUploadStatus();
        if (dbStatus) {
            if (!CollectionUtils.isEmpty(masterRecords.getMasterFailureList())){
                fileUploadStatus.setStatus(Constants.PARTIAL_SUCCESS);
            }else {
                fileUploadStatus.setStatus(Constants.SUCCESS);
            }
            fileUploadStatus.setNumberOfRecords(masterRecords.getMasterList().size());
            fileUploadStatus.setNumberOfRecordSuccess(masterRecords.getMasterList().size());
        } else {
            fileUploadStatus.setStatus(Constants.ERROR);
            fileUploadStatus.setErrorDesc(Constants.CORRUPT_RECORD);
        }
        masterRecords.setFileUploadStatus(fileUploadStatus);
        if (masterRecords.getMasterList() != null && !masterRecords.getMasterList().isEmpty() && StringUtils.equals(masterRecords.getFileUploadStatus().getStatus(), Constants.SUCCESS)) {
            fileUploadStatus = getDbFileUploadStatus(dbStatus, masterRecords);
        } else {
            fileUploadStatus = masterRecords.getFileUploadStatus();
        }
        return fileUploadStatus;
    }

    private FileUploadStatus getDbFileUploadStatus(boolean dbStatus, MasterRecords masterRecords) {
        FileUploadStatus fileUploadStatus;
        if (dbStatus) {
            logger.info("Database operation success");
            fileUploadStatus = masterRecords.getFileUploadStatus();

        } else {
            logger.error("Database operation failure");
            fileUploadStatus = new FileUploadStatus();
            fileUploadStatus.setErrorDesc("Database operation failure");
            fileUploadStatus.setStatus(Constants.ERROR);
        }
        return fileUploadStatus;
    }
}
