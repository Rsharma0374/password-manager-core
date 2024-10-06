package com.password.manager.utility;

import com.password.manager.constant.FieldSeparator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StopWatch;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    private static String fileUploadDirectory ;


    @Value("${passmanager.tmp.dir}")
    public void setFileUploadDirectory(String fileUploadDirectory ){
        this.fileUploadDirectory = fileUploadDirectory;
    }


    public static File saveFileToStagingDirectory(MultipartFile file) {

        String newFilenameBase = UUID.randomUUID().toString();

        String originalFileExtension = file.getOriginalFilename().substring(
                file.getOriginalFilename().lastIndexOf(FieldSeparator.DOT)
        );

        String newFilename = newFilenameBase + originalFileExtension;

        String storageDirectory = fileUploadDirectory;

        File newFile = new File(storageDirectory + FieldSeparator.FORWARD_SLASH + newFilename);

        try {

            if(!newFile.exists())
            {
                File parentFile = newFile.getParentFile();

                if(!parentFile.isDirectory()) {
                    parentFile.mkdirs();
                }

            }
            if(logger.isDebugEnabled()){
                logger.debug("Uploading {} ", file);
            }
            file.transferTo(newFile);

        } catch (IOException e) {
            logger.error("Exception Occurred while transferring file {} to staging area {}",
                    storageDirectory + FieldSeparator.FORWARD_SLASH + newFilename, newFile.getParentFile());
        }
        return newFile;
    }
}
