package com.tarento.commenthub.service.impl;

import com.fasterxml.uuid.Generators;
import com.tarento.commenthub.entity.Comment;
import com.tarento.commenthub.exception.CommentException;
import com.tarento.commenthub.repository.CommentRepository;
import com.tarento.commenthub.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Override
    public Comment addComment(Comment comment) {
        log.info("CommentServiceImpl::addComment:adding comment");
        try {
            UUID uuid = Generators.timeBasedGenerator().generate();
            String id = uuid.toString();
            comment.setId(id);
            return commentRepository.save(comment);
        } catch (Exception e) {
            throw new CommentException("ERROR01", "Failed to coment");
        }
    }

    @Override
    public Comment updateComment(Comment updatedComment) {
        log.info("CommentServiceImpl::updateComment:updating comment");
        if (updatedComment.getId() != null){
            Optional<Comment> fetchedComment = commentRepository.findById(updatedComment.getId());
            if (fetchedComment.isPresent() && fetchedComment.get().isStatus()){
                return commentRepository.save(updatedComment);
            }
        }else {
            throw new CommentException("ERROR02", "This comment is not present to edit ");
        }
        return updatedComment;
    }
}
