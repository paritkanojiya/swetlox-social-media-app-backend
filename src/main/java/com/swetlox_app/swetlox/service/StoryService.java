package com.swetlox_app.swetlox.service;

import com.swetlox_app.swetlox.allenum.EntityType;
import com.swetlox_app.swetlox.allenum.MediaType;
import com.swetlox_app.swetlox.dto.like.LikeResponseDto;
import com.swetlox_app.swetlox.dto.story.StoryDto;
import com.swetlox_app.swetlox.entity.*;
import com.swetlox_app.swetlox.dto.story.StoryResponseDto;
import com.swetlox_app.swetlox.repository.StoryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class StoryService {

    private final StoryRepo storyRepo;
    private final CloudService cloudService;
    private final MongoTemplate mongoTemplate;
    private final UserConnectionService userConnectionService;
    private final UserService userService;
    private final LikeService likeService;

    public void createStory(List<MultipartFile> files, List<MediaType> mediaTypes, String authId){
        List<Story> storyList = IntStream.range(0,files.size())
                .parallel().mapToObj(i -> {
                    MultipartFile multipartFile = files.get(i);
                    MediaType mediaType = mediaTypes.get(i);
                    try {
                        Map uploaded = cloudService.upload(multipartFile, mediaType);
                        double duration=5.0;
                        if(mediaType.equals(MediaType.VIDEO)){
                            duration= (double) uploaded.get("duration");
                        }
                        return Story
                                .builder()
                                .mediaType(mediaType)
                                .mediaURL((String) uploaded.get("url"))
                                .duration(duration)
                                .userId(authId).build();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).toList();
        storyRepo.saveAll(storyList);
    }

    public List<StoryResponseDto> getConnectionStory(User authUser){
        List<UserConnection> followingList = userConnectionService.getFollowingList(authUser.getId());
        return followingList
                .stream()
                .map(UserConnection::getUserId)
                .map(storyRepo::findByUserId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(storyList -> !storyList.isEmpty())
                .map(this::entityToStoryResponseDto).collect(Collectors.toList());
    }




    public void likeStory(String storyId,String token){
        User authUser = userService.getAuthUser(token);
        Like like = Like.builder().entityId(storyId)
                .entityType(EntityType.STORY)
                .userId(authUser.getId())
                .build();
        likeService.like(like);
    }

    public Page<LikeResponseDto> getLikeResponseDtoByEntityId(String entityId,Integer pageNum){
        return likeService.getLikeByEntityId(entityId, pageNum);

    }


    public void deleteStoryByUserId(String id){
        storyRepo.deleteByUserId(id);
    }

    @Scheduled(fixedRate = 3000000)
    public void deleteStory(){
        LocalDateTime twentyFourHoursAgo=LocalDateTime.now().minusHours(24);
        Query query=new Query();
        query.addCriteria(Criteria.where("timeStamp").lt(twentyFourHoursAgo));
        long count = mongoTemplate.count(query, Story.class);
        System.out.println("Number of stories older than 24 hours: " + count);
        mongoTemplate.remove(query, Story.class);
    }

    private StoryResponseDto entityToStoryResponseDto(List<Story> storyList){
        String userId = storyList.get(0).getUserId();
        User user = userService.getUserById(userId);
        return StoryResponseDto.builder().userId(user.getId())
                .userName(user.getUserName())
                .profileURL(user.getProfileURL())
                .storyDtoList(storyList.stream().map(story -> StoryDto.builder()
                        .id(story.getId())
                        .createdAt(story.getTimeStamp())
                        .mediaURL(story.getMediaURL())
                        .mediaType(story.getMediaType())
                        .duration(story.getDuration())
                        .build()).collect(Collectors.toList()))
                .build();
    }

    public Story getStoryById(String id){
        return storyRepo.findById(id).orElseThrow(()->new RuntimeException("provided "+id+" story not found"));
    }

    public StoryResponseDto getSelfStory(User authUser){
        Optional<List<Story>> optionalStoryList = storyRepo.findByUserId(authUser.getId());
        if(optionalStoryList.isPresent()){
            List<Story> storyList = optionalStoryList.get();
            if(!storyList.isEmpty())
                return this.entityToStoryResponseDto(storyList);
        }
        return null;
    }



//    private CompletableFuture<List<Map>> uploadStoryInBatch(List<StoryRequestDto> storyRequestDtoList){
//        ExecutorService executorService = Executors.newFixedThreadPool(10);
//        List<Map> storyResponseList=new ArrayList<>();
//        List<CompletableFuture<Map>> futures=new ArrayList<>();
//        for (int i=0;i<storyRequestDtoList.size();i++){
//            int finalI = i;
//            CompletableFuture<Map> mapCompletableFuture = CompletableFuture.supplyAsync(() -> {
//                StoryRequestDto storyRequestDto = storyRequestDtoList.get(finalI);
//                try {
//                    Map uploaded = cloudService.upload(storyRequestDto.getFile(), storyRequestDto.getMediaType());
//                    synchronized (storyResponseList){
//                        storyResponseList.set(finalI,uploaded);
//                    }
//                    return uploaded;
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }, executorService);
//            futures.add(mapCompletableFuture);
//        }
//         return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenApply((v)-> storyResponseList);
//    }

}
