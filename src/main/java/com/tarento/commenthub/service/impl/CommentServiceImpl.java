package com.tarento.commenthub.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.uuid.Generators;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import com.tarento.commenthub.constant.Constants;
import com.tarento.commenthub.dto.CommentsRequestDTO;
import com.tarento.commenthub.dto.CommentsResoponseDTO;
import com.tarento.commenthub.dto.ResponseDTO;
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
    public ResponseDTO addOrupdateComment(JsonNode CommentData) {
        log.info("CommentServiceImpl::addOrupdateComment:updating comment");

        //CommoentValidaion
        JsonSchema schema = jsonSchema();
        Set<ValidationMessage> validationMessages = schema.validate(CommentData);
        if (!validationMessages.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("Validation error(s): \n");
            for (ValidationMessage message : validationMessages) {
                errorMessage.append(message.getMessage()).append("\n");
            }
            throw new CommentException("ERROR", errorMessage.toString());
        }

        if (CommentData.get("commentId") != null && !CommentData.get("commentId").asText().isEmpty()) {
            log.info(CommentData.get("commentId").asText());
            Comment commentDetailsFromJson = new Comment();
            commentDetailsFromJson = fetchDetailsofComment(CommentData);
            if (commentDetailsFromJson.getCommentId() != null){
                Optional<Comment> fetchedComment = commentRepository.findById(commentDetailsFromJson.getCommentId());
                if (fetchedComment.isPresent() && fetchedComment.get().isStatus()){
                    commentRepository.save(commentDetailsFromJson);
                    String commentTreeId = objectMapper.convertValue(CommentData.get(Constants.COMMENT_TREE_ID), String.class);
                    CommentTree commentTree = commentTreeService.getCommentTreeById(commentTreeId);
                    ResponseDTO responseDTO = new ResponseDTO();
                    responseDTO.setComment(commentDetailsFromJson);
                    responseDTO.setCommentTree(commentTree);
                    return responseDTO;
                }else {
                    throw new CommentException("ERROR02"," comment not found or You are tyring to edit a deleted comment");
                }
            }
        }
        else if (CommentData.get("commentId") == null || CommentData.get("commentId").isEmpty()){
            UUID uuid = Generators.timeBasedGenerator().generate();
            String id = uuid.toString();
            ObjectNode objectComment = (ObjectNode) CommentData;
            objectComment.put("commentId",id);
            Comment commentDetailsFromJson = new Comment();
            commentDetailsFromJson = fetchDetailsofComment(CommentData);
            commentRepository.save(commentDetailsFromJson);

            ((ObjectNode) CommentData).put(Constants.COMMENT_ID, commentDetailsFromJson.getCommentId());
            CommentTree commentTree = commentTreeService.createOrUpdateCommentTree(CommentData);

            ResponseDTO responseDTO = new ResponseDTO();
            responseDTO.setComment(commentDetailsFromJson);
            responseDTO.setCommentTree(commentTree);
            return responseDTO;
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
    public CommentsResoponseDTO getComments(CommentsRequestDTO commentsRequestDTO) {
        CommentTree commentTree = commentTreeService.getCommentTree(commentsRequestDTO);
        JsonNode childNodes = commentTree.getCommentTreeJson().get(Constants.CHILD_NODES);
        List<String> childNodeList = objectMapper.convertValue(childNodes, List.class);
        List<Comment> comments = commentRepository.findAllById(childNodeList);
        CommentsResoponseDTO commentsResoponseDTO = new CommentsResoponseDTO();
        commentsResoponseDTO.setComments(comments);
        commentsResoponseDTO.setCommentTree(commentTree);
        return commentsResoponseDTO;
    }

    @Override
    public Comment deleteCommentById(String commentId) {
        log.info("CommentServiceImpl::deleteCommentById:deleting comment");
        Optional<Comment> fetchedComment =  commentRepository.findById(commentId);
        if(fetchedComment.isPresent()){
            if (fetchedComment.get().isStatus()){
                Comment comment = fetchedComment.get();
                comment.setStatus(false);
                return commentRepository.save(comment);
            }
            throw new CommentException("ERROR", "You are trying to delete a already deleted comment");
        }
        throw new CommentException("ERROR", "No such comment found");
    }


    public Comment fetchDetailsofComment(JsonNode comment){
        Comment commentFetched = new Comment();
        commentFetched.setCommentId(comment.get(Constants.COMMENT_ID).asText());
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode commentJson = objectMapper.createObjectNode();
        ObjectNode commentObjNode = (ObjectNode) commentJson;
        commentObjNode.put(Constants.COMMENT, comment.get(Constants.COMMENT));
        commentObjNode.put(Constants.FILE, comment.get(Constants.FILE));
        commentObjNode.put(Constants.COMMENT_SOURCE, comment.get(Constants.COMMENT_SOURCE));
        commentFetched.setCommentJson(commentJson);
        return commentFetched;
    }
}
