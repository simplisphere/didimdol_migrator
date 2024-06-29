package com.simplisphere.didimdolstandardize;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/demo")
public class Controller {
    private final DataStandardizationService dataStandardizationService;

    @PostMapping("/standardize")
    public ResponseEntity<String> standardizeData() {
        dataStandardizationService.standardizeAndSaveFromFirebird();
        return ResponseEntity.ok("Data has been standardized and stored in PostgreSQL.");
    }
}
