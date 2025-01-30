package com.swetlox_app.swetlox.controller;

import com.swetlox_app.swetlox.allenum.EntityType;
import com.swetlox_app.swetlox.dto.post.PostResponseDto;
import com.swetlox_app.swetlox.dto.share.ShareResponseDto;
import com.swetlox_app.swetlox.service.PostService;
import com.swetlox_app.swetlox.service.ShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.parser.Entity;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/share")
public class ShareController {

    private final ShareService shareService;

    @GetMapping("/{entityType}/{entityId}")
    public ResponseEntity<ShareResponseDto> getPost(@PathVariable("entityId") String entityId, @PathVariable("entityType") EntityType entityType, @RequestHeader(required = false,name = "Authorization") String token){

        boolean isAuth= token != null;
        ShareResponseDto shareResponseDto = shareService.getEntityByIdAndEntityType(entityId, entityType, isAuth, token);
        return ResponseEntity.ok(shareResponseDto);
    }
}
