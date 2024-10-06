package com.password.manager.model;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class FileUploadStatus {

    private long numberOfRecords;
    private long numberOfRecordFailed;
    private long numberOfRecordSuccess;
    private String errorDesc;
    private String status;
    private String additionalFields;
}
