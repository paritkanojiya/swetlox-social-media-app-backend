package com.swetlox_app.swetlox.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.swetlox_app.swetlox.allenum.MediaType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudService {

    private final Cloudinary cloudinaryTemplate;

    public Map upload(MultipartFile file, MediaType mediaType) throws IOException {
        if(mediaType.equals(MediaType.IMAGE)){
            return uploadPost(file);
        }else if(mediaType.equals(MediaType.VIDEO)){
            return uploadReals(file);
        }else if(mediaType.equals(MediaType.VOICE)){
            return uploadAudio(file);
        }
        throw new RuntimeException("provided media is not supported");
    }

    private Map uploadPost(MultipartFile file) throws IOException {
        return cloudinaryTemplate.uploader().upload(file.getBytes(), Collections.emptyMap());
    }

    private Map uploadReals(MultipartFile file) throws IOException {
        return cloudinaryTemplate.uploader()
                .upload(file.getBytes(), ObjectUtils.asMap("resource_type", "video"));
    }

    private Map uploadAudio(MultipartFile file) throws IOException {
        return cloudinaryTemplate.uploader().upload(file.getBytes(),ObjectUtils.asMap("resource_type", "auto"));
    }

}
