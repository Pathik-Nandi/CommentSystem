package com.tarento.commenthub.service;

import com.tarento.commenthub.entity.Comment;

public interface CommentService {
    Comment addComment(Comment comment);

    Comment updateComment(Comment comment);
}
