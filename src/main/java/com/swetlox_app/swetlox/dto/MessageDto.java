package com.swetlox_app.swetlox.dto;

import lombok.Data;

@Data
public class MessageDto {
    private String sender;
    private String recipient;
    private String content;
}
