package com.tarento.commenthub.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.uuid.Generators;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import com.tarento.commenthub.dto.CommentsRequestDTO;
import com.tarento.commenthub.dto.CommentsResoponseDTO;
import com.tarento.commenthub.entity.Comment;
import com.tarento.commenthub.entity.CommentTree;
import com.tarento.commenthub.exception.CommentException;
import com.tarento.commenthub.repository.CommentRepository;
import com.tarento.commenthub.service.CommentService;
import com.tarento.commenthub.service.CommentTreeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentTreeService commentTreeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Comment addComment(JsonNode comment) {
        log.info("CommentServiceImpl::addComment:adding comment");

        commentTreeService.createOrUpdateCommentTree(comment);

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
    public Comment addOrupdateComment(JsonNode updatedComment) {
        log.info("CommentServiceImpl::addOrupdateComment:updating comment");

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

        if (updatedComment.get("commentId") != null && !updatedComment.get("commentId").asText().isEmpty()) {
            log.info(updatedComment.get("commentId").asText());
            Comment commentDetailsFromJson = new Comment();
            commentDetailsFromJson = fetchDetailsofComment(updatedComment);
            if (commentDetailsFromJson.getCommentId() != null){
                Optional<Comment> fetchedComment = commentRepository.findById(commentDetailsFromJson.getCommentId());
                if (fetchedComment.isPresent() && fetchedComment.get().isStatus()){
                    return commentRepository.save(commentDetailsFromJson);
                }else {
                    throw new CommentException("ERROR02"," comment not found or You are tyring to edit a deleted comment");
                }
            }
        }
        else if (updatedComment.get("commentId").isEmpty() || updatedComment.get("commentId") == null){
            UUID uuid = Generators.timeBasedGenerator().generate();
            String id = uuid.toString();
            ObjectNode objectComment = (ObjectNode) updatedComment;
            objectComment.put("commentId",id);
            Comment commentDetailsFromJson = new Comment();
            commentDetailsFromJson = fetchDetailsofComment(updatedComment);
            return commentRepository.save(commentDetailsFromJson);
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
    public CommentsResoponseDTO getComments(CommentsRequestDTO commentsRequestDTO) {
        CommentTree commentTree = commentTreeService.getCommentTree(commentsRequestDTO);
        JsonNode childNodes = commentTree.getCommentTreeJson().get("childNodes");
        List<String> childNodeList = objectMapper.convertValue(childNodes, List.class);
        List<Comment> comments = commentRepository.findAllById(childNodeList);
        CommentsResoponseDTO commentsResoponseDTO = new CommentsResoponseDTO();
        commentsResoponseDTO.setComments(comments);
        commentsResoponseDTO.setCommentTree(commentTree);
        return commentsResoponseDTO;
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
        commentFetched.setCommentId(comment.get("commentId").asText());
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
