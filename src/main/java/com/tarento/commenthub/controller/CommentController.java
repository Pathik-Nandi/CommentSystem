package com.tarento.commenthub.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.tarento.commenthub.entity.Comment;
import com.tarento.commenthub.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("Comment")
@Slf4j
public class CommentController {
    @Autowired
    private CommentService commentService;

    @PostMapping("/add")
    public Comment addComment(@RequestBody JsonNode comment) {
        return commentService.addComment(comment);
    }

    @PutMapping("/update")
    public Comment updateComment(@RequestBody JsonNode comment){
        return commentService.updateComment(comment);
    }
}
