package com.swetlox_app.swetlox.dto.message;

import lombok.Builder;
import lombok.Getter;

@Builder
public class ImageMedia implements Media{
    @Getter
    private final String imageURL;
    private final String caption;

    public ImageMedia(String imageURL, String caption) {
        this.imageURL = imageURL;
        this.caption = caption;
    }

    @Override
    public String getContent() {
        return caption;
    }

}
