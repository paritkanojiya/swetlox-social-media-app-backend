package com.swetlox_app.swetlox.service;

import com.swetlox_app.swetlox.allenum.EntityType;
import com.swetlox_app.swetlox.allenum.NotificationType;
import com.swetlox_app.swetlox.dto.comment.CommentRequestDto;
import com.swetlox_app.swetlox.dto.comment.CommentResponseDto;
import com.swetlox_app.swetlox.dto.notification.InteractionNotificationDto;
import com.swetlox_app.swetlox.dto.notification.NotificationDto;
import com.swetlox_app.swetlox.dto.user.UserDto;
import com.swetlox_app.swetlox.entity.*;
import com.swetlox_app.swetlox.event.SendNotificationEvent;
import com.swetlox_app.swetlox.repository.CommentRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {



    private final CommentRepo commentRepo;

    private final StoryService storyService;
    private final NotificationService notificationService;
    private final UserPreferenceService userPreferenceService;
    @Autowired
    @Lazy
    private  PostService postService;
    @Autowired
    @Lazy
    private ReelsService reelsService;
    @Autowired
    @Lazy
    private  UserService userService;
    @Value("${default.capacity.page.size}")
    private int DEFAULT_CAPACITY_FOR_PAGE_SIZE;
    public static final String COMMENT_ON_POST = "comment on your post";
    private static final String COMMENT_ON_REEL="comment on your reel";
    public static final String COMMENT_ON_STORY = "comment on your story";



    public void saveComment(CommentRequestDto commentRequestDto, User authUser,User entityUser){
        Comment comment = Comment.builder()
                .entityId(commentRequestDto.getEntityId())
                .entityType(commentRequestDto.getEntityType())
                .content(commentRequestDto.getCommentContent())
                .userId(authUser.getId())
                .build();
        Comment saveComment= commentRepo.save(comment);
        boolean isLikeCommentNotficatioOn = userPreferenceService.isOnLikeCommentNotification(entityUser.getId());
        if(isLikeCommentNotficatioOn){
            sendNotification(saveComment,authUser);
        }
    }

    public void deletePostComment(String postId){
        commentRepo.deleteByEntityId(postId);
    }

    public void deleteComment(String commentId,String authUserId){
        commentRepo.deleteByIdAndUserId(commentId,authUserId);
    }

    public Page<CommentResponseDto> getCommentByEntityId(String entityId,Integer pageNum){
        PageRequest pageRequest = PageRequest.of(pageNum, DEFAULT_CAPACITY_FOR_PAGE_SIZE, Sort.Direction.DESC, "createdAt");
        Page<Comment> commentPage = commentRepo.findByEntityId(entityId, pageRequest);
        List<CommentResponseDto> commentResponseDtoList = commentPage.map(this::entityToCommentResponseDto).toList();
        return new PageImpl<>(commentResponseDtoList,pageRequest,commentPage.getTotalElements());
    }

    public List<CommentResponseDto> getCommentListByEntityId(String entityId){
        List<Comment> commentList = commentRepo.findByEntityId(entityId);
        return commentList.stream().map(this::entityToCommentResponseDto).toList();
    }

    public List<Comment> getPostComment(String postId){
        return commentRepo.findByEntityId(postId);
    }
    
    private CommentResponseDto entityToCommentResponseDto(Comment comment){
        String userId = comment.getUserId();
        UserDto userDto = userService.getUserDtoById(userId);
        return CommentResponseDto.builder()
                .id(comment.getId())
                .sender(userDto)
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    @Async(value = "taskExecutor")
    public void sendNotification(Comment comment,User authUser){
        
        EntityType entityType = comment.getEntityType();
        UserDto userDto = userService.getUserDtoByUser(authUser);

        switch (entityType){
            case POST -> {
                String postId = comment.getEntityId();
                Post post = postService.getPostById(postId);
                User postUser = userService.getUserById(post.getUserId());
                notificationService.sendNotification(new InteractionNotificationDto(UUID.randomUUID().toString(),userDto,postUser.getEmail(),COMMENT_ON_POST,NotificationType.POST,post.getPostURL()));
            }
            case REEL -> {
                String reelId = comment.getEntityId();
                Reels reel = reelsService.getReelById(reelId);
                User reelUser = userService.getUserById(reel.getUserId());
                notificationService.sendNotification(new InteractionNotificationDto(UUID.randomUUID().toString(),userDto,reelUser.getEmail(),COMMENT_ON_REEL,NotificationType.POST,"not support"));
            }
            case STORY -> {
                String storyId = comment.getEntityId();
                Story story = storyService.getStoryById(storyId);
                User storyUser = userService.getUserById(story.getUserId());
                notificationService.sendNotification(new InteractionNotificationDto(UUID.randomUUID().toString(),userDto,storyUser.getEmail(),COMMENT_ON_STORY,NotificationType.STORY,"not support"));
            }
            default -> throw new RuntimeException("not support");
        }
    }

    public Integer getCommentCountByUserId(String userId){
        return commentRepo.countByUserId(userId);
    }

    public void deleteAllCommentByUserId(String id) {
        commentRepo.deleteByUserId(id);
    }
}
