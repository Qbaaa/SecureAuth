package com.qbaaa.secure.auth.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qbaaa.secure.auth.exception.InputInvalidException;
import com.qbaaa.secure.auth.exception.UnSupportedFileException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileUploadValidator {

    private final AntiSamyValidator antiSamyValidator;
    private final ObjectMapper objectMapper;

    private static final List<String> ALLOWED_EXTENSIONS = List.of(
            ".json"
    );

    private static final List<String> ALLOWED_MIME_TYPES = List.of(
            "application/json"
    );


    public boolean validate(MultipartFile file) {

        try {
            final var fileNameUpload = file.getOriginalFilename();
            final var contentType = file.getContentType();
            final var bytes = file.getBytes();

            if (StringUtils.isBlank(fileNameUpload)) {
                throw new InputInvalidException("File name is required");
            }
            final var fileName = Paths.get(fileNameUpload).getFileName().toString();

            if (file.isEmpty()) {
                throw new InputInvalidException("File cannot be empty");
            }

            var validExtension = ALLOWED_EXTENSIONS.stream().anyMatch(fileName::endsWith);
            if (!validExtension) {
                throw new UnSupportedFileException("Allowed extension for file " + Arrays.toString(ALLOWED_EXTENSIONS.toArray()));
            }

            if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
                throw new UnSupportedFileException("Content type MIME" + contentType + " not supported");
            }

            if (!isValidFileJson(bytes)) {
                throw new UnSupportedFileException("File is not valid JSON");
            }

            if (!antiSamyValidator.validate(new String(bytes, StandardCharsets.UTF_8))) {
                throw new InputInvalidException("File contains forbidden characters");
            }

            return true;
        } catch (IOException e) {
            log.warn(e.getMessage());
            return false;
        }

    }

    private boolean isValidFileJson(byte[] fileToCheck) {
        try {
            objectMapper.readTree(fileToCheck);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean isSupportedFileFormat(byte[] fileToCheck) {
        final var tika = new Tika();
        final var mediaType = MediaType.parse(tika.detect(fileToCheck));

        return ALLOWED_MIME_TYPES.contains(mediaType.toString());
    }

}
