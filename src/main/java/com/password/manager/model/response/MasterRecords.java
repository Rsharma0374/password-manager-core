package com.password.manager.model.response;

import com.password.manager.model.FileUploadStatus;

import java.util.List;

public class MasterRecords {

    private FileUploadStatus fileUploadStatus;
    private List<?> masterList;
    private List<?> masterFailureList;

    public FileUploadStatus getFileUploadStatus() {
        return fileUploadStatus;
    }

    public void setFileUploadStatus(FileUploadStatus fileUploadStatus) {
        this.fileUploadStatus = fileUploadStatus;
    }

    public List<?> getMasterList() {
        return masterList;
    }

    public void setMasterList(List<?> masterList) {
        this.masterList = masterList;
    }

    public List<?> getMasterFailureList() {
        return masterFailureList;
    }

    public void setMasterFailureList(List<?> masterFailureList) {
        this.masterFailureList = masterFailureList;
    }
}
