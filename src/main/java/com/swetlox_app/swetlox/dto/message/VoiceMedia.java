package com.swetlox_app.swetlox.dto.message;

import lombok.Builder;
import lombok.Getter;

@Builder
public class VoiceMedia implements Media{

    @Getter
    private final String voiceURL;
    private final String caption;

    public VoiceMedia(String voiceURL, String caption) {
        this.voiceURL = voiceURL;
        this.caption = caption;
    }

    @Override
    public String getContent() {
        return caption;
    }
}
