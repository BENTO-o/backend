package bento.backend.service.file;

import bento.backend.exception.ValidationException;

import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import org.json.simple.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class FileService {

	@Value("${file.upload.path}")
	private String uploadPath;

	// 파일 업로드
	public String uploadFile(MultipartFile file) {
		if (file.isEmpty()) {
			throw new ValidationException("File is empty");
		}

		// 폴더 확인 및 생성
		File directory = new File(uploadPath);
		if (!directory.exists()) {
			directory.mkdirs();
			System.out.println("Directory created: " + uploadPath);
		}

		String fileName = generateFileName(file.getOriginalFilename());
		String fullPath = uploadPath + fileName;

		try {
			file.transferTo(new File(fullPath));
		} catch (Exception e) {
			throw new ValidationException("File upload failed");
		}

		return fileName;
	}

	// 파일 이름 생성
	private String generateFileName(String originalFileName) {
		String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		String newFileName = timestamp + "_" + originalFileName;

		return newFileName;
	}

	// 파일 다운로드
	public ResponseEntity<Resource> downloadFile(String fileName) {
		try {
			// 파일 경로 생성
			Path filePath = Paths.get(uploadPath).resolve(fileName).normalize();
			Resource resource = new UrlResource(filePath.toUri());

			// 파일이 존재하고 읽을 수 있는지 확인
			if (!resource.exists() || !resource.isReadable()) {
				throw new ValidationException("File not found or unreadable");
			}

			// 파일 반환
			return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
				.body(resource);
		} catch (Exception e) {
			throw new ValidationException("File download failed");
		}
	}
}
