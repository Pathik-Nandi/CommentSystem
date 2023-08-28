package com.tarento.commenthub.repository;

import com.tarento.commenthub.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, String> {
}
