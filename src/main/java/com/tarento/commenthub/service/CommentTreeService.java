package com.tarento.commenthub.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.tarento.commenthub.dto.CommentsRequestDTO;
import com.tarento.commenthub.entity.CommentTree;

public interface CommentTreeService {

    void createOrUpdateCommentTree(JsonNode commentData);

    CommentTree getCommentTree(CommentsRequestDTO commentsRequestDTO);


}
