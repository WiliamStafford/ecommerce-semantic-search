package com.ecommerce.common.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Upload mọi loại file ảnh lên Cloudinary theo folder chỉ định
     * @param file Tệp tin nhận từ Controller
     * @param folderName Tên thư mục lưu trữ trên Cloudinary (Ví dụ: "fruit_fresh/returns")
     * @String Đường dẫn URL tuyệt đối dẫn tới ảnh công khai
     */
    public String uploadImage(MultipartFile file, String folderName) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("folder", folderName));

            String secureUrl = uploadResult.get("secure_url").toString();
            log.info(">>>> [CLOUDINARY] Upload file thành công! URL: {}", secureUrl);
            return secureUrl;
        } catch (IOException e) {
            log.error(">>>> [CLOUDINARY] Lỗi  khi đẩy file lên hệ thống mây: {}", e.getMessage());
            throw new RuntimeException("Tải tệp tin lên máy chủ Cloudinary thất bại!");
        }
    }
}