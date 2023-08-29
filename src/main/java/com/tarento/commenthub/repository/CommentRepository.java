package com.tarento.commenthub.repository;

import com.tarento.commenthub.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, String> {
    Optional<Comment> findByIdAndStatus(String id, Boolean isActive);
}
