package com.apple.interview.controller.web;

import com.apple.interview.service.UploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author xuzhonyu
 */
@RestController
@RequestMapping("/upload")
public class UploadFileController {

    private static final Logger logger = LoggerFactory.getLogger(UploadFileController.class);
    @Autowired
    private UploadService uploadService;

    @PostMapping("/uploadDemandOrder")
    public boolean uploadDemandOrder(@RequestParam("uploadId") String uploadId, MultipartFile file) throws Exception {
        boolean result = uploadService.uploadDemandOrder(uploadId, file);
        return result;
    }

    @PostMapping("/uploadSupply")
    public boolean uploadSupply(@RequestParam("uploadId") String uploadId, MultipartFile file) throws Exception {
        boolean result = uploadService.uploadSupply(uploadId, file);
        return result;
    }

    @PostMapping("/uploadSourcingRule")
    public boolean uploadSourcingRule(@RequestParam("uploadId") String uploadId, MultipartFile file) throws Exception {
        boolean result = uploadService.uploadSourcingRule(uploadId, file);
        return result;
    }
}
