package com.qbaaa.secure.auth.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qbaaa.secure.auth.shared.exception.UnSupportedFileException;
import com.qbaaa.secure.auth.shared.validation.AntiSamyValidator;
import com.qbaaa.secure.auth.shared.validation.FileUploadValidator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.owasp.validator.html.PolicyException;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

class FileUploadValidatorTest {

  private static FileUploadValidator validator;

  @BeforeAll
  static void beforeAll() throws PolicyException, IOException {
    validator = new FileUploadValidator(new AntiSamyValidator(), new ObjectMapper());
  }

  @Test
  void shouldReturnTrue() {
    try {
      // given
      var file =
          new File(
              getClass().getClassLoader().getResource("upload/importDomainTest.json").getFile());
      MockMultipartFile fileUpload =
          new MockMultipartFile(
              "file",
              "importDomainTest.json",
              MediaType.APPLICATION_JSON_VALUE,
              new FileInputStream(file));

      // when
      var validate = validator.validate(fileUpload);

      // then
      assertThat(validate).isTrue();

    } catch (Exception e) {
      Assertions.fail("ERROR: " + e.getMessage());
    }
  }

  @Test
  void shouldThrowExceptionWhenFileIsNotJson() {

    try {
      // given
      var file =
          new File(getClass().getClassLoader().getResource("upload/test.php.json").getFile());
      var fileUpload =
          new MockMultipartFile(
              "file", "test.php.json", MediaType.APPLICATION_JSON_VALUE, new FileInputStream(file));

      // when
      var throwable = catchThrowable(() -> validator.validate(fileUpload));

      // then
      assertThat(throwable).isInstanceOf(UnSupportedFileException.class);

    } catch (Exception e) {
      Assertions.fail("ERROR: " + e.getMessage());
    }
  }

  @Test
  void shouldThrowExceptionWhenExtensionIsNotSupported() {
    try {
      // given
      var file = new File(getClass().getClassLoader().getResource("upload/file_php.res").getFile());
      var fileUpload =
          new MockMultipartFile(
              "file", "file_php.res", MediaType.APPLICATION_JSON_VALUE, new FileInputStream(file));

      // when
      var throwable = catchThrowable(() -> validator.validate(fileUpload));

      // then
      assertThat(throwable).isInstanceOf(UnSupportedFileException.class);

    } catch (Exception e) {
      Assertions.fail("ERROR: " + e.getMessage());
    }
  }
}
