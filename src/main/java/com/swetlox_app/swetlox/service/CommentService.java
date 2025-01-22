package com.swetlox_app.swetlox.service;

import com.swetlox_app.swetlox.allenum.EntityType;
import com.swetlox_app.swetlox.allenum.NotificationType;
import com.swetlox_app.swetlox.dto.comment.CommentRequestDto;
import com.swetlox_app.swetlox.dto.comment.CommentResponseDto;
import com.swetlox_app.swetlox.dto.notification.InteractionNotificationDto;
import com.swetlox_app.swetlox.dto.notification.NotificationDto;
import com.swetlox_app.swetlox.dto.user.UserDto;
import com.swetlox_app.swetlox.entity.Comment;
import com.swetlox_app.swetlox.entity.Post;
import com.swetlox_app.swetlox.entity.Story;
import com.swetlox_app.swetlox.entity.User;
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

@Service
@RequiredArgsConstructor
public class CommentService {



    private final CommentRepo commentRepo;
    private final StoryService storyService;
    private final NotificationService notificationService;
    @Autowired
    @Lazy
    private  PostService postService;
    @Autowired
    @Lazy
    private  UserService userService;
    @Value("${default.capacity.page.size}")
    private int DEFAULT_CAPACITY_FOR_PAGE_SIZE;
    public static final String COMMENT_ON_POST = "comment on your post";
    public static final String COMMENT_ON_STORY = "comment on your story";



    public void saveComment(CommentRequestDto commentRequestDto, User authUser){
        Comment comment = Comment.builder()
                .entityId(commentRequestDto.getEntityId())
                .entityType(commentRequestDto.getEntityType())
                .content(commentRequestDto.getCommentContent())
                .userId(authUser.getId())
                .build();
        Comment saveComment= commentRepo.save(comment);
        sendNotification(saveComment,authUser);
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
        return new PageImpl<>(commentResponseDtoList,pageRequest,commentPage.getTotalPages());
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
                notificationService.sendNotification(new InteractionNotificationDto(postId,userDto,postUser.getEmail(),COMMENT_ON_POST,NotificationType.POST,post.getPostURL()));
            }
            case REEL -> {
                //pending
            }
            case STORY -> {
                String storyId = comment.getEntityId();
                Story story = storyService.getStoryById(storyId);
                User storyUser = userService.getUserById(story.getUserId());
                notificationService.sendNotification(new InteractionNotificationDto(storyId,userDto,storyUser.getEmail(),COMMENT_ON_STORY,NotificationType.STORY,"not support"));
            }
            default -> throw new RuntimeException("not support");
        }
    }
}
