package com.tarento.commenthub.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.tarento.commenthub.dto.CommentsRequestDTO;
import com.tarento.commenthub.dto.CommentsResoponseDTO;
import com.tarento.commenthub.dto.ResponseDTO;
import com.tarento.commenthub.entity.Comment;

public interface CommentService {
    ResponseDTO addOrupdateComment(JsonNode comment);

    CommentsResoponseDTO getComments(CommentsRequestDTO commentsRequestDTO);

    Comment deleteCommentById(String commentId);

}
