package com.swetlox_app.swetlox.dto.message;

import com.swetlox_app.swetlox.allenum.MediaType;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class MessageRequestDTO {
    private String sender;
    private String recipient;
    private String content;
    private MediaType mediaType;
    private MultipartFile media;
}
