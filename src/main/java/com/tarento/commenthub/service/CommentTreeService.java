package com.tarento.commenthub.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.tarento.commenthub.dto.CommentsRequestDTO;
import com.tarento.commenthub.entity.Comment;
import com.tarento.commenthub.entity.CommentTree;

import java.util.Optional;

public interface CommentTreeService {

    CommentTree createOrUpdateCommentTree(JsonNode commentData);

    CommentTree getCommentTreeById(String commentTreeId);

    CommentTree getCommentTree(CommentsRequestDTO commentsRequestDTO);

}
