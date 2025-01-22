package com.swetlox_app.swetlox.repository;

import com.swetlox_app.swetlox.allenum.ConnectionRequestStatus;
import com.swetlox_app.swetlox.entity.ConnectionRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConnectionRequestRepo extends MongoRepository<ConnectionRequest,String> {
    Optional<ConnectionRequest> findBySenderIdAndReceiverId(String authUserId, String requestId);

    List<ConnectionRequest> findByReceiverIdAndStatus(String id, ConnectionRequestStatus connectionRequestStatus);

    boolean existsBySenderIdAndReceiverIdAndStatus(String senderId,String receiverId,ConnectionRequestStatus connectionRequestStatus);
}
