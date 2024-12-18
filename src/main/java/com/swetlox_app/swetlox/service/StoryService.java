package com.swetlox_app.swetlox.service;

import com.cloudinary.Cloudinary;
import com.swetlox_app.swetlox.dto.UserDto;
import com.swetlox_app.swetlox.entity.Story;
import com.swetlox_app.swetlox.entity.User;
import com.swetlox_app.swetlox.model.StoryModel;
import com.swetlox_app.swetlox.repository.StoryRepo;
import jakarta.mail.Multipart;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoryService {

    private final StoryRepo storyRepo;
    private final Cloudinary cloudinaryTemplate;
    private final MongoTemplate mongoTemplate;
    private final UserConnectionService userConnectionService;
    private final UserService userService;

    public void createStory(List<MultipartFile> listOfImage,String authId){
        List<Story> storyList = listOfImage.stream()
                .map((multipart -> {
                    try {
                        return cloudinaryTemplate.uploader()
                                .upload(multipart.getBytes(), Collections.emptyMap());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })).map((map -> (String) map.get("url"))).map((imgURL) ->
                        Story.builder().userId(authId)
                                .timeStamp(LocalDateTime.now())
                                .imageURL(imgURL)
                                .build()).collect(Collectors.toList());

        storyRepo.saveAll(storyList);
    }

    public List<StoryModel> getConnectionStory(String authId){

        List<String> followingList = userConnectionService.getFollowingList(authId);
        return followingList
                .stream()
                .map(storyRepo::findByUserId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(storyList -> !storyList.isEmpty())
                .map(stories -> {

                    List<String> storyListURL=new ArrayList<>();
                    stories.forEach(story -> storyListURL.add(story.getImageURL()));
                    User user = userService.getUserById(stories.get(0).getUserId());
                    return StoryModel
                            .builder().userName(user.getUserName())
                            .fullName(user.getFullName())
                            .profileURL(user.getProfileURL())
                            .storyList(storyListURL)
                            .build();
                }).collect(Collectors.toList());
    }

    public void deleteStoryByUserId(String id){
        storyRepo.deleteByUserId(id);
    }
    @Scheduled(fixedRate = 3000000)
    public void deleteStory(){
        LocalDateTime twentyFourHoursAgo=LocalDateTime.now().plusHours(24);
        Query query=new Query();
        query.addCriteria(Criteria.where("timeStamp").lt(twentyFourHoursAgo));
        long count = mongoTemplate.count(query, Story.class);
        System.out.println("Number of stories older than 24 hours: " + count);
        mongoTemplate.remove(query, Story.class);
    }


}
