package com.swetlox_app.swetlox.dto;

import com.swetlox_app.swetlox.allenum.MediaType;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class MessageDto {
    private MediaType mediaType;
    private String sender;
    private String recipient;
    private String content;
    private MultipartFile multipartFile;
}
