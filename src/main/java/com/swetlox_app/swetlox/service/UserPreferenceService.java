package com.swetlox_app.swetlox.service;

import com.swetlox_app.swetlox.entity.UserPreference;
import com.swetlox_app.swetlox.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserPreferenceService {

    private final UserPreferenceRepository userPreferenceRepository;

    public void initialSetting(String userId){
        boolean userPreferenceExist = isUserPreferenceExist(userId);
        if(!userPreferenceExist){
            UserPreference userPreference = UserPreference.builder()
                    .userId(userId)
                    .autoFollow(false)
                    .followerNotification(true)
                    .privateAccount(true)
                    .like_commentNotification(true)
                    .build();
            save(userPreference);
        }
    }



    public void save(UserPreference userPreference){
        userPreferenceRepository.save(userPreference);
    }
    public void update(UserPreference userPreference){
        userPreferenceRepository.save(userPreference);
    }

    public boolean isUserPreferenceExist(String userId){
       return userPreferenceRepository.existsByUserId(userId);
    }

    public UserPreference getUserPreferenceByUserId(String userId){
        return userPreferenceRepository.findByUserId(userId).orElseThrow(()->new RuntimeException("userPreference not found"));
    }

    public void changePrivateAccountSetting(String userId,boolean newChangeValue){
        UserPreference userPreference = getUserPreferenceByUserId(userId);
        userPreference.setPrivateAccount(newChangeValue);
        update(userPreference);
    }

    public void changeAutoFollowSetting(String userId,boolean newChangeValue){
        UserPreference userPreference = getUserPreferenceByUserId(userId);
        userPreference.setAutoFollow(newChangeValue);
        update(userPreference);
    }

    public void changeLikeCommentNotificationSetting(String userId,boolean newChangeValue){
        UserPreference userPreference = getUserPreferenceByUserId(userId);
        userPreference.setLike_commentNotification(newChangeValue);
        update(userPreference);
    }

    public void changeNewFollowerNotification(String userId,boolean newChangeValue){
        UserPreference userPreference = getUserPreferenceByUserId(userId);
        userPreference.setFollowerNotification(newChangeValue);
        update(userPreference);
    }
    
    public boolean isAutoFollowOn(String userId){
        UserPreference userPreference = getUserPreferenceByUserId(userId);
        return userPreference.isAutoFollow();
    }

    public boolean isPrivateAccount(String userId){
        UserPreference userPreference = getUserPreferenceByUserId(userId);
        return userPreference.isPrivateAccount();
    }

    public boolean isOnLikeCommentNotification(String userId){
        UserPreference userPreference = getUserPreferenceByUserId(userId);
        return userPreference.isLike_commentNotification();
    }

    public boolean isNewFollowerNotificationOn(String userId){
        UserPreference userPreference = getUserPreferenceByUserId(userId);
        return userPreference.isFollowerNotification();
    }

    public void deleteByUserId(String id) {
        userPreferenceRepository.deleteByUserId(id);
    }
}
