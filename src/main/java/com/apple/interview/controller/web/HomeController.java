package com.apple.interview.controller.web;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author zhongyu
 */
@Controller
public class HomeController {

    @GetMapping(value = "/")
    public String homePage(Model model) {
        model.addAttribute("uploadId", IdWorker.getIdStr());
        return "home";
    }
}
