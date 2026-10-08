package com.example.farmerbackend.repository;

import com.example.farmerbackend.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // Get full conversation between two users (ordered oldest-first for display)
    @Query("SELECT m FROM ChatMessage m WHERE " +
           "(m.senderId = :userId1 AND m.receiverId = :userId2) OR " +
           "(m.senderId = :userId2 AND m.receiverId = :userId1) " +
           "ORDER BY m.createdAt ASC")
    List<ChatMessage> findConversation(@Param("userId1") Long userId1,
                                       @Param("userId2") Long userId2);

    // Get the most recent message for each conversation involving a user
    @Query(value = "SELECT cm.* FROM chat_messages cm " +
           "WHERE cm.createdAt IN (" +
           "  SELECT MAX(m.createdAt) FROM chat_messages m " +
           "  WHERE m.senderId = :userId OR m.receiverId = :userId " +
           "  GROUP BY CASE WHEN m.senderId = :userId THEN m.receiverId ELSE m.senderId END" +
           ") ORDER BY cm.createdAt DESC", nativeQuery = true)
    List<ChatMessage> findRecentConversations(@Param("userId") Long userId);

    // Count unread messages for a user
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.receiverId = :userId AND m.isRead = false")
    long countUnread(@Param("userId") Long userId);

    // Mark all messages in a conversation as read
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE " +
           "m.receiverId = :userId AND m.senderId = :otherUserId AND m.isRead = false")
    void markConversationAsRead(@Param("userId") Long userId,
                                @Param("otherUserId") Long otherUserId);
}