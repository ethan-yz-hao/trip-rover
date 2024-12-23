package org.ethanhao.triprover.service;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.ethanhao.triprover.domain.User;
import org.ethanhao.triprover.dto.user.UserResponseDTO;
import org.ethanhao.triprover.exception.UserOperationException;
import org.ethanhao.triprover.mapper.UserMapper;
import org.ethanhao.triprover.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AvatarService {
    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private String avatarBucketName;

    @Autowired
    private String avatarBucketDomain;

    private static final int MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int TARGET_SIZE = 500;
    private static final String DEFAULT_AVATAR = "default-avatar.png";

    public UserResponseDTO uploadAvatar(MultipartFile file, Long userId) {
        validateFile(file);

        BufferedImage originalImage;
        try {
            originalImage = ImageIO.read(file.getInputStream());
            validateImageDimensions(originalImage);
        } catch (IOException e) {
            throw new UserOperationException("Failed to read image file");
        }

        // Crop and resize image
        BufferedImage processedImage = processImage(originalImage);

        // Convert to byte array
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(processedImage, "png", os);
        } catch (IOException e) {
            throw new UserOperationException("Failed to write image file");
        }
        byte[] imageBytes = os.toByteArray();

        // Generate unique filename
        String filename = String.format("avatar_%d_%s.png", userId, UUID.randomUUID().toString());

        // Upload to S3
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/png");
        metadata.setContentLength(imageBytes.length);

        amazonS3.putObject(avatarBucketName, filename, new ByteArrayInputStream(imageBytes), metadata);

        // Update user's avatar URL
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        String avatarUrl = String.format("%s/%s", avatarBucketDomain, filename);
        user.setAvatar(avatarUrl);
        userRepository.save(user);

        return userMapper.toResponseDto(user);
    }

    public UserResponseDTO deleteAvatar(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getAvatar() != null && !user.getAvatar().endsWith(DEFAULT_AVATAR)) {
            String filename = user.getAvatar().substring(user.getAvatar().lastIndexOf('/') + 1);
            amazonS3.deleteObject(avatarBucketName, filename);
        }

        user.setAvatar(String.format("%s/%s", avatarBucketDomain, DEFAULT_AVATAR));
        userRepository.save(user);

        return userMapper.toResponseDto(user);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 5MB limit");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.matches("image/(jpeg|png|gif)")) {
            throw new IllegalArgumentException("Invalid file type");
        }
    }

    private void validateImageDimensions(BufferedImage image) {
        if (image.getWidth() < 200 || image.getHeight() < 200) {
            throw new IllegalArgumentException("Image dimensions too small");
        }
        if (image.getWidth() > 2000 || image.getHeight() > 2000) {
            throw new IllegalArgumentException("Image dimensions too large");
        }
    }

    private BufferedImage processImage(BufferedImage original) {
        int width = original.getWidth();
        int height = original.getHeight();
        
        // Calculate dimensions for center crop
        int size = Math.min(width, height);
        int x = (width - size) / 2;
        int y = (height - size) / 2;
        
        // Crop to square
        BufferedImage cropped = original.getSubimage(x, y, size, size);
        
        // Resize to target size
        BufferedImage resized = new BufferedImage(TARGET_SIZE, TARGET_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.drawImage(cropped, 0, 0, TARGET_SIZE, TARGET_SIZE, null);
        g.dispose();
        
        return resized;
    }
} 