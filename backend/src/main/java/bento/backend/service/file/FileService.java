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
		String fullPath = uploadPath + file.getOriginalFilename();

		try {
			file.transferTo(new File(fullPath));
		} catch (Exception e) {
			throw new ValidationException("File upload failed");
		}

		return fullPath;
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
