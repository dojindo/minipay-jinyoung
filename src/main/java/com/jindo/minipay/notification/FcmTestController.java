package com.jindo.minipay.notification;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FcmTestController {

  @GetMapping("/test")
  public String test() {
    return "index.html";
  }
}
