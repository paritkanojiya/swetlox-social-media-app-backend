package com.swetlox_app.swetlox.dto.story;

import com.swetlox_app.swetlox.allenum.MediaType;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryRequestDto {
    @NotNull(message = "File is required")
    private MultipartFile file;
    @NotNull(message = "Media type is required")
    private MediaType mediaType;
}
