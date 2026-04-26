package com.senior.ecomm.catalog;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class StatusController {
  @GetMapping("/status")
  Map<String, Object> status() {
    return Map.of("service", "catalog-service", "ok", true);
  }
}

