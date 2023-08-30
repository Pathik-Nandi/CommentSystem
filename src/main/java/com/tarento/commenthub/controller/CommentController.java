package com.tarento.commenthub.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.tarento.commenthub.dto.CommentsRequestDTO;
import com.tarento.commenthub.dto.CommentsResoponseDTO;
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

//    @PostMapping("/add")
//    public Comment addComment(@RequestBody JsonNode comment) {
//        return commentService.addComment(comment);
//    }

    @PutMapping("/addOrUpdate")
    public Comment addOrUpdateComment(@RequestBody JsonNode comment){
        return commentService.addOrupdateComment(comment);
    }

    @GetMapping("/get/{commentId}")
    public Comment getCommentbyId(@PathVariable String commentId){
        return commentService.getCommentById(commentId);
    }

    @GetMapping("/getAll")
    public CommentsResoponseDTO getComments(@RequestBody CommentsRequestDTO commentsRequestDTO){
        return commentService.getComments(commentsRequestDTO);
    }

    @DeleteMapping("/delete/{commentId}")
    public String deleteCommentById(@PathVariable String commentId){
        return commentService.deleteCommentById(commentId);
    }
}
