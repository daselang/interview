package com.apple.interview.controller.web;

import com.apple.interview.service.ResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * @author zhongyu
 */
@Controller
@RequestMapping("/result")
public class ResultController {
    @Autowired
    private ResultService resultService;

    @GetMapping(value = "/showResultPage")
    public String homePage(@RequestParam("uploadId") String uploadId, Model model) throws Exception {
        boolean dataExists = resultService.checkUploadIdExists(uploadId);
        //if data are not all uploaded,redirect to the 404 not found page.
        if (!dataExists) {
            return "/error/404";
        }
        resultService.generateOrderExecutionPlan(uploadId);
        model.addAttribute("uploadId", uploadId);
        return "result";
    }

    @GetMapping(value = "/showPageData")
    @ResponseBody
    public Map<String, Object> showPageData(@RequestParam("uploadId") String uploadId) {
        Map<String, Object> result = resultService.generatePageData(uploadId);
        return result;
    }
}
