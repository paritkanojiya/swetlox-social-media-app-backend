package com.swetlox_app.swetlox.service;

import com.cloudinary.Cloudinary;
import com.swetlox_app.swetlox.entity.Post;
import com.swetlox_app.swetlox.entity.Reels;
import com.swetlox_app.swetlox.entity.User;
import com.swetlox_app.swetlox.model.ReelsModel;
import com.swetlox_app.swetlox.repository.ReelsRepo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.transform.sax.SAXResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReelsService {

    private final ReelsRepo reelsRepo;
    private final ModelMapper mapper;
    private final MongoTemplate mongoTemplate;
    private static final String reelDirPath="src/main/resources/static/reels";

    @PostConstruct
    public void init(){

        File file=new File(reelDirPath);
        if(!file.exists()){
            boolean mkdir = file.mkdir();
            log.info("reel dir created {}",mkdir);
        }

    }
    public ReelsModel saveReel(User authUser, MultipartFile file,String caption) throws IOException {

        Path path = Paths.get(reelDirPath, file.getOriginalFilename());
        Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
        String reelURL=reelDirPath+"/"+file.getOriginalFilename();
        File file1=new File(reelURL);
        String absoluteUrl = file1.getAbsoluteFile().toString();
        Reels reels = Reels.builder().userId(authUser.getId())
                .caption(caption)
                .createdAt(LocalDateTime.now())
                .reelsURL(absoluteUrl)
                .build();
        Reels savedReel = reelsRepo.save(reels);
        return convertReelsToReelsModel(savedReel);
    }
    private ReelsModel convertReelsToReelsModel(Reels reels){
        return mapper.map(reels,ReelsModel.class);
    }
    
    public void deleteReels(String id,String authId){

        Reels reels = reelsRepo.deleteByIdAndUserId(id, authId);
        File file=new File(reels.getReelsURL());
        if(file.delete()){
            log.info("file deleted");
        }
    }

    public void likedReels(String reelId,String userId){
        boolean isLiked=reelsRepo.existsByIdAndLikedUserListContaining(reelId,userId);
        if(isLiked){
            removeUserIdFromLikedUserList(reelId,userId);
            return;
        }
        addUserIdFromLikedUserList(reelId,userId);
    }

    public List<ReelsModel> userReels(String authId){
        return reelsRepo.findByUserId(authId).stream().map(this::convertReelsToReelsModel).collect(Collectors.toList());
    }

    public ReelsModel getReelById(String reelId){
        return reelsRepo.findById(reelId).map(this::convertReelsToReelsModel).orElse(
                null);
    }

    public Page<ReelsModel> loadReels(Integer pageNum){
        Pageable pageRequest= PageRequest.of(pageNum,10);
        Page<Reels> reelsPage = reelsRepo.findAll(pageRequest);
        List<ReelsModel> reelsModelList = reelsPage.map(this::convertReelsToReelsModel).stream().collect(Collectors.toList());
        return new PageImpl<>(reelsModelList,pageRequest,reelsPage.getTotalElements());
    }
    private void removeUserIdFromLikedUserList(String reelId, String userId) {
        Query query = new Query(Criteria.where("_id").is(reelId));
        Update update = new Update().pull("likedUserList", userId);
        mongoTemplate.updateFirst(query, update, Post.class);
    }

    private void addUserIdFromLikedUserList(String reelId, String userId) {
        Query query = new Query(Criteria.where("_id").is(reelId));
        Update update = new Update().addToSet("likedUserList", userId);
        mongoTemplate.updateFirst(query, update, Post.class);
    }
}
