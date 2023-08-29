package com.tarento.commenthub.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.uuid.Generators;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import com.tarento.commenthub.entity.Comment;
import com.tarento.commenthub.exception.CommentException;
import com.tarento.commenthub.repository.CommentRepository;
import com.tarento.commenthub.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Override
    public Comment addComment(JsonNode comment) {
        log.info("CommentServiceImpl::addComment:adding comment");

        //CommoentValidaion
        JsonSchema schema = jsonSchema();
        Set<ValidationMessage> validationMessages = schema.validate(comment);
        if (!validationMessages.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("Validation error(s): \n");
            for (ValidationMessage message : validationMessages) {
                errorMessage.append(message.getMessage()).append("\n");
            }
            throw new CommentException("ERROR", errorMessage.toString());
        }
        try {
            UUID uuid = Generators.timeBasedGenerator().generate();
            String id = uuid.toString();
            Comment commentObj = new Comment();
            commentObj.setId(id);
            commentObj.setCommentJson(comment);
            return commentRepository.save(commentObj);
        } catch (Exception e) {
            throw new CommentException("ERROR01", "Failed to coment");
        }
    }

    @Override
    public Comment updateComment(JsonNode updatedComment) {
        log.info("CommentServiceImpl::updateComment:updating comment");

        //CommoentValidaion
        JsonSchema schema = jsonSchema();
        Set<ValidationMessage> validationMessages = schema.validate(updatedComment);
        if (!validationMessages.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("Validation error(s): \n");
            for (ValidationMessage message : validationMessages) {
                errorMessage.append(message.getMessage()).append("\n");
            }
            throw new CommentException("ERROR", errorMessage.toString());
        }
        Comment comment = new Comment();
        if (comment.getId() != null){
            Optional<Comment> fetchedComment = commentRepository.findById(comment.getId());
            if (fetchedComment.isPresent() && fetchedComment.get().isStatus()){
                return commentRepository.save(comment);
            }
        }else {
            throw new CommentException("ERROR02", "This comment is not present to edit ");
        }
        return comment;
    }

    private JsonSchema jsonSchema() {
        try {
            JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance();
            InputStream schemaStream = schemaFactory.getClass().getResourceAsStream("/commentValidation.json");
            return schemaFactory.getSchema(schemaStream);
        } catch (Exception e) {
            throw new CommentException("ERROR", "Failed to load JSON Schema: " + e.getMessage());
        }
    }
}
