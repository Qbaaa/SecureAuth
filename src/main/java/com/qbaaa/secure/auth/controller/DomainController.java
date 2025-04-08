package com.qbaaa.secure.auth.controller;

import com.qbaaa.secure.auth.exception.DomainImportException;
import com.qbaaa.secure.auth.service.DomainImportService;
import com.qbaaa.secure.auth.validation.FileUploadValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/domains")
@RequiredArgsConstructor
@Tag(name = "Domain API")
public class DomainController {

  private final DomainImportService domainImportService;
  private final FileUploadValidator fileUploadValidator;

  @Operation(
      summary = "Imports a domain based on the uploaded file",
      security = @SecurityRequirement(name = "Authorization"))
  @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  public ResponseEntity<Void> importDomain(@RequestPart(value = "file") MultipartFile fileUpload) {

    if (!fileUploadValidator.validate(fileUpload)) {
      throw new DomainImportException("Validation error");
    }
    domainImportService.importFileDomain(fileUpload);
    return ResponseEntity.ok().build();
  }
}
