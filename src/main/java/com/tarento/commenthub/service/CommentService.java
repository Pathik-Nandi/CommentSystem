package com.tarento.commenthub.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.tarento.commenthub.entity.Comment;

public interface CommentService {
    Comment addComment(JsonNode comment);

    Comment updateComment(JsonNode comment);
}
