package com.swetlox_app.swetlox.service;

import com.swetlox_app.swetlox.allenum.MediaType;
import com.swetlox_app.swetlox.allenum.MessageStatus;
import com.swetlox_app.swetlox.dto.message.*;
import com.swetlox_app.swetlox.entity.ChatRoom;
import com.swetlox_app.swetlox.entity.Message;
import com.swetlox_app.swetlox.repository.MessageRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepo messageRepo;
    private final ChatRoomService chatRoomService;
    private final SimpMessagingTemplate messagingTemplate;
    private final CloudService cloudService;


    @Transactional
    public void sendMessage(MessageRequestDTO messageRequestDTO) throws IOException {
        String recipientId = messageRequestDTO.getRecipient();
        String senderId = messageRequestDTO.getSender();
        String chatId1=chatRoomService.getChatId(senderId,recipientId);
        String chatId2=chatRoomService.getChatId(recipientId,senderId);
        Optional<ChatRoom> optionalChatRoom = chatRoomService.findChatRoom(chatId1, chatId2);

        if(optionalChatRoom.isPresent()){
            ChatRoom chatRoom = optionalChatRoom.get();
            Message message = saveMessage(messageRequestDTO, chatRoom.getChatId());
            MessageSendDto messageSendDto = entityToMessageSendDto(message);
            messagingTemplate.convertAndSend("/user/chat/message/"+recipientId,messageSendDto);
            return;
        }

        throw new RuntimeException("not found chatroom");
    }

    private Message saveMessage(MessageRequestDTO messageRequestDTO,String chatId) throws IOException {
        switch (messageRequestDTO.getMediaType()){
            case MediaType.TEXT -> {
                Message message = getEntityForTextMessage(messageRequestDTO, chatId);
                return messageRepo.save(message);
            }
            case MediaType.IMAGE,MediaType.VIDEO,MediaType.VOICE-> {
                Message message = getEntityForImageOrVideoOrAudioMessage(messageRequestDTO, chatId);
                return messageRepo.save(message);
            }
            default -> throw new RuntimeException("provided media type not supported");
        }
    }

    private Media getMediaObject(Message message){
        switch (message.getMediaType()){
            case MediaType.TEXT -> {
                return getTextMediaObject(message);
            }
            case MediaType.IMAGE -> {
                return getImageMediaObject(message);
            }
            case MediaType.VIDEO -> {
                return getVideoMediaObject(message);
            }
            case MediaType.VOICE -> {
                return getVoiceMediaObject(message);
            }
            default -> throw new RuntimeException("provided media type not supported");
        }
    }

    private Map uploadMedia(MultipartFile file, MediaType mediaType) throws IOException {
        return cloudService.upload(file,mediaType);
    }

    private Message getEntityForTextMessage(MessageRequestDTO messageRequestDTO,String chatId){
        return Message.builder()
                .sender(messageRequestDTO.getSender())
                .recipient(messageRequestDTO.getRecipient())
                .chatRoomId(chatId)
                .content(messageRequestDTO.getContent())
                .mediaType(messageRequestDTO.getMediaType())
                .status(MessageStatus.SENT)
                .build();
    }

    private Message getEntityForImageOrVideoOrAudioMessage(MessageRequestDTO messageRequestDTO,String chatId) throws IOException {
        Map uploaded = uploadMedia(messageRequestDTO.getMedia(),messageRequestDTO.getMediaType());
        return Message.builder()
                .sender(messageRequestDTO.getSender())
                .recipient(messageRequestDTO.getRecipient())
                .chatRoomId(chatId)
                .content(messageRequestDTO.getContent())
                .mediaType(messageRequestDTO.getMediaType())
                .status(MessageStatus.SENT)
                .mediaURL((String) uploaded.get("url"))
                .build();
    }

    public List<MessageSendDto> loadMessage(String authId,String recipientId){
        String chatId1= chatRoomService.getChatId(authId,recipientId);
        String chatId2= chatRoomService.getChatId(recipientId,authId);
        Optional<ChatRoom> chatRoom = chatRoomService.findChatRoom(chatId1, chatId2);
        if(chatRoom.isPresent()){
            List<Message> messageList = messageRepo.findByChatRoomId(chatRoom.get().getChatId());
            return messageList.stream().map(this::entityToMessageSendDto).toList();
        }
        return Collections.emptyList();
    }

    private MessageSendDto entityToMessageSendDto(Message message){
        Media mediaObject = getMediaObject(message);
        return MessageSendDto.builder()
                .messageId(message.getId())
                .timeStamp(LocalDateTime.now())
                .senderId(message.getSender())
                .recipientId(message.getRecipient())
                .status(message.getStatus())
                .mediaType(message.getMediaType())
                .media(mediaObject)
                .build();
    }

    private Media getTextMediaObject(Message message){
        return TextMedia.builder().caption(message.getContent()).build();
    }

    private Media getVideoMediaObject(Message message){
        return VideoMedia.builder().videoURL(message.getMediaURL())
                .caption(message.getContent())
                .build();
    }

    private Media getVoiceMediaObject(Message message){
        return VoiceMedia.builder().voiceURL(message.getMediaURL())
                .caption(message.getContent())
                .build();
    }

    private Media getImageMediaObject(Message message){
        return ImageMedia.builder().imageURL(message.getMediaURL())
                .caption(message.getContent())
                .build();
    }


    public void deleteAllMessageByUserId(String id) {
        messageRepo.deleteBySender(id);
        messageRepo.deleteByRecipient(id);
    }
}
