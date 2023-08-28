package com.tarento.commenthub.controller;

import com.tarento.commenthub.entity.Comment;
import com.tarento.commenthub.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("Comment")
@Slf4j
public class CommentController {
    @Autowired
    private CommentService commentService;

    @PostMapping("/add")
    public Comment addComment(@RequestBody Comment comment) {
        return commentService.addComment(comment);
    }
}
