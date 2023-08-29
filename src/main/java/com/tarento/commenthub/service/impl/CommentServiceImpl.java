package com.tarento.commenthub.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
            ObjectNode objectComment = (ObjectNode) comment;
            objectComment.put("id",id);
            Comment commentDetailsFromJson = new Comment();
            commentDetailsFromJson = fetchDetailsofComment(comment);
            return commentRepository.save(commentDetailsFromJson);
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
        Comment commentDetailsFromJson = new Comment();
        commentDetailsFromJson = fetchDetailsofComment(updatedComment);
        if (commentDetailsFromJson.getId() != null){
            Optional<Comment> fetchedComment = commentRepository.findById(commentDetailsFromJson.getId());
            if (fetchedComment.isPresent() && fetchedComment.get().isStatus()){
                return commentRepository.save(commentDetailsFromJson);
            }
        }else {
            throw new CommentException("ERROR02", "This comment is not present to edit ");
        }
        return null;
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

    @Override
    public Comment getCommentById(String id) {
        log.info("CommentServiceImpl::getCommentById:fetching comment");
        Optional<Comment> fetchedComment =  commentRepository.findById(id);
        if(fetchedComment.isEmpty() ){
            if(!fetchedComment.get().isStatus()) {
                throw new CommentException("ERROR", "Comment is not found");
            }else {
                throw new CommentException("ERROR", "Its a deleted comment");
            }
        }
        return fetchedComment.get();
    }

    @Override
    public String deleteCommentById(String commentId) {
        log.info("CommentServiceImpl::deleteCommentById:deleting comment");
        Optional<Comment> fetchedComment =  commentRepository.findById(commentId);
        if(fetchedComment.isPresent()){
            if (fetchedComment.get().isStatus()){
                Comment comment = new Comment();
                comment = fetchedComment.get();
                comment.setStatus(false);
                commentRepository.save(comment);
            }else{
                throw new CommentException("ERROR03", "You are trying to delete a already deleted comment");
            }
        }else {
            throw new CommentException("ERROR04", "No such comment found");
        }
        return null;
    }

    public Comment fetchDetailsofComment(JsonNode comment){
        Comment commentFetched = new Comment();
        commentFetched.setId(comment.get("id").asText());
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode commentJson = objectMapper.createObjectNode();
        ObjectNode commentObjNode = (ObjectNode) commentJson;
        commentObjNode.put("comment", comment.get("comment"));
        commentObjNode.put("file", comment.get("file"));
        commentObjNode.put("commentSource", comment.get("commentSource"));
        commentFetched.setCommentJson(commentJson);
        return commentFetched;
    }
}
