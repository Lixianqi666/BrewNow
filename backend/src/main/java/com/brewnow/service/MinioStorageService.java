package com.brewnow.service;

import org.springframework.web.multipart.MultipartFile;

public interface MinioStorageService {

    String uploadImage(MultipartFile file, String folder, String fileName);

    String uploadBytes(byte[] bytes, String contentType, String folder, String fileName);
}
