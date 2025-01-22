package com.swetlox_app.swetlox.dto.message;

import lombok.Builder;

@Builder
public class TextMedia  implements Media {

    private final String caption;

    public TextMedia(String caption) {
        this.caption = caption;
    }

    @Override
    public String getContent() {
        return caption;
    }


}
