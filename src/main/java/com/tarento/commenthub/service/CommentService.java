package com.tarento.commenthub.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.tarento.commenthub.dto.CommentsRequestDTO;
import com.tarento.commenthub.dto.CommentsResoponseDTO;
import com.tarento.commenthub.entity.Comment;

public interface CommentService {
    Comment addComment(JsonNode comment);

    Comment addOrupdateComment(JsonNode comment);

    Comment getCommentById(String id);

    CommentsResoponseDTO getComments(CommentsRequestDTO commentsRequestDTO);

    String deleteCommentById(String commentId);
}
