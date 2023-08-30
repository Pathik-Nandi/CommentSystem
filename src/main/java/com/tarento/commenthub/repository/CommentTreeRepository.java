package com.tarento.commenthub.repository;

import com.tarento.commenthub.entity.Comment;
import com.tarento.commenthub.entity.CommentTree;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentTreeRepository extends JpaRepository<CommentTree, String> {
}
