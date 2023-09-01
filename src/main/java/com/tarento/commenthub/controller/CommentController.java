package com.tarento.commenthub.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.tarento.commenthub.constant.Constants;
import com.tarento.commenthub.dto.CommentsRequestDTO;
import com.tarento.commenthub.dto.CommentsResoponseDTO;
import com.tarento.commenthub.dto.ResponseDTO;
import com.tarento.commenthub.entity.Comment;
import com.tarento.commenthub.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("comment")
@Slf4j
public class CommentController {
    @Autowired
    private CommentService commentService;

    @PutMapping("/addOrUpdate")
    public ResponseDTO addOrUpdateComment(@RequestBody JsonNode commentData){
        return commentService.addOrupdateComment(commentData);
    }

    @GetMapping("/getAll")
    public CommentsResoponseDTO getComments(@RequestBody CommentsRequestDTO commentsRequestDTO){
        return commentService.getComments(commentsRequestDTO);
    }

    @DeleteMapping("/delete/{commentId}")
    public Comment deleteComment(@PathVariable String commentId){
        return commentService.deleteCommentById(commentId);
    }

    @GetMapping("/health")
    public String healthCheck() {
        return Constants.SUCCESS_STRING;
    }

}
