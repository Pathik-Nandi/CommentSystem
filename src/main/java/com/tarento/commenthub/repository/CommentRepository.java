package com.tarento.commenthub.repository;

import com.tarento.commenthub.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, String> {


    Optional<Comment> findByCommentIdAndStatus(String id, Boolean isActive);

    @Query(value = "SELECT created_date FROM comment WHERE comment_id = ?1", nativeQuery = true)
    LocalDateTime getCreatedDateByCommentId(String commentId);


}
