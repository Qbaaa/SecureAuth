package com.qbaaa.secure.auth.controller;

import com.qbaaa.secure.auth.dto.DomainTransferDto;
import com.qbaaa.secure.auth.service.DomainImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/domain")
@RequiredArgsConstructor
public class DomainController {

    private final DomainImportService domainImportService;

    @PostMapping("import")
    public ResponseEntity<Void> importDomain(@RequestBody DomainTransferDto request) {

        domainImportService.importDomain(request);
        return ResponseEntity.ok().build();
    }
}
