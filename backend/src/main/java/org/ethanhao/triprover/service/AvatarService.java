package org.ethanhao.triprover.service;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
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

@Service
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

    @Autowired
    private String defaultAvatarUrl;

    private static final int MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int TARGET_SIZE = 200;

    public UserResponseDTO uploadAvatar(MultipartFile file, Long userId) {
        validateFile(file);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

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
        String avatarUrl = String.format("https://%s/%s", avatarBucketDomain, filename);
        user.setAvatar(avatarUrl);
        userRepository.save(user);

        return userMapper.toResponseDto(user);
    }

    public UserResponseDTO deleteAvatar(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getAvatar() != null && !user.getAvatar().endsWith(defaultAvatarUrl)) {
            String filename = user.getAvatar().substring(user.getAvatar().lastIndexOf('/') + 1);
            amazonS3.deleteObject(avatarBucketName, filename);
        }

        user.setAvatar(defaultAvatarUrl);
        userRepository.save(user);

        return userMapper.toResponseDto(user);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new UserOperationException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new UserOperationException("File size exceeds 5MB limit");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.matches("image/(jpeg|png|gif)")) {
            throw new UserOperationException("Invalid file type");
        }
    }

    private void validateImageDimensions(BufferedImage image) {
        if (image == null) {
            throw new UserOperationException("Invalid image file");
        }
        
        if (image.getWidth() < TARGET_SIZE || image.getHeight() < TARGET_SIZE) {
            throw new UserOperationException(
                String.format("Image dimensions must be at least %dx%d pixels", TARGET_SIZE, TARGET_SIZE)
            );
        }
    }

    private BufferedImage processImage(BufferedImage original) {
        int width = original.getWidth();
        int height = original.getHeight();
        
        // Calculate dimensions for center crop
        int x, y, size;
        
        if (width > height) {
            size = height;
            x = (width - height) / 2;
            y = 0;
        } else {
            size = width;
            x = 0;
            y = (height - width) / 2;
        }
        
        // Crop to square
        BufferedImage cropped = original.getSubimage(x, y, size, size);
        
        // Resize to target size
        BufferedImage resized = new BufferedImage(TARGET_SIZE, TARGET_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2d.drawImage(cropped, 0, 0, TARGET_SIZE, TARGET_SIZE, null);
        } finally {
            g2d.dispose();
        }
        
        return resized;
    }
} 