package com.password.manager.service;

import com.password.manager.response.BaseResponse;

import java.io.File;

public interface FileUploadService {
    BaseResponse uploadApiAuthenticationMaster(File file, String product);
}
