package com.swetlox_app.swetlox.service;

import com.swetlox_app.swetlox.entity.Comment;
import com.swetlox_app.swetlox.entity.User;
import com.swetlox_app.swetlox.event.SendNotificationEvent;
import com.swetlox_app.swetlox.model.PostModel;
import com.swetlox_app.swetlox.repository.CommentRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepo commentRepo;
    @Autowired
    @Lazy
    private  PostService postService;
    @Autowired
    @Lazy
    private  UserService userService;
    private final ApplicationEventPublisher eventPublisher;


    public Comment saveComment(Comment commentDTO, User authUser){
        Comment comment = Comment.builder()
                .reelId(commentDTO.getReelId())
                .postId(commentDTO.getPostId())
                .content(commentDTO.getContent())
                .createdAt(LocalDateTime.now())
                .userId(authUser.getId())
                .userName(authUser.getUserName())
                .build();
        Comment saveComment= commentRepo.save(comment);
        if(comment.getPostId()!=null){
            PostModel postModel = postService.getPostByPostId(comment.getPostId());
            User user = userService.getUserById(postModel.getUserId());
            SendNotificationEvent sendNotificationEvent = new SendNotificationEvent(this,"comment on your post", postModel.getPostURL(), authUser.getUserName(),user.getEmail());
            eventPublisher.publishEvent(sendNotificationEvent);
        }
        return saveComment;
    }

    public void deletePostComment(String postId){
        commentRepo.deleteByPostId(postId);
    }

    public void deleteComment(String commentId,String authUserId){
        commentRepo.deleteByIdAndUserId(commentId,authUserId);
    }

    public List<Comment> getPostComment(String postId){
        return commentRepo.findByPostId(postId);
    }
}
