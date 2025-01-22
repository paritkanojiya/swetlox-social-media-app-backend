package com.swetlox_app.swetlox.dto.message;

import lombok.Builder;
import lombok.Getter;

@Builder
public class VideoMedia implements Media{

    @Getter
    private final String videoURL;
    private final String caption;

    public VideoMedia(String videoURL, String caption) {
        this.videoURL = videoURL;
        this.caption = caption;
    }

    @Override
    public String getContent() {
        return caption;
    }

}
