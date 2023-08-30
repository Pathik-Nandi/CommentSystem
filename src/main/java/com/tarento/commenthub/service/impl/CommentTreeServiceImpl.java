package com.tarento.commenthub.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.uuid.Generators;
import com.tarento.commenthub.dto.CommentsRequestDTO;
import com.tarento.commenthub.entity.CommentTree;
import com.tarento.commenthub.service.CommentTreeService;

import java.util.UUID;

public class CommentTreeServiceImpl implements CommentTreeService {
    @Override
    public void createOrUpdateCommentTree(JsonNode commentData) {
        if(commentData.get("id") != null && !commentData.get("id").equals("")) {
            updateCommentTree(commentData);
        } else {
            createCommentTree(commentData);
        }
    }

    @Override
    public CommentTree getCommentTree(CommentsRequestDTO commentsRequestDTO) {
//        write implementation to get comment_tree from commentsRequestDTO
        return null;
    }

    public void createCommentTree(JsonNode commentData) {
        UUID uuid = Generators.timeBasedGenerator().generate();
        String id = uuid.toString();

    }

    public void updateCommentTree(JsonNode commentData) {

    }

}
