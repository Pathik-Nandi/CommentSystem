package com.tarento.commenthub.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tarento.commenthub.constant.Constants;
import com.tarento.commenthub.dto.Comments;
import com.tarento.commenthub.dto.CommentsRequestDTO;
import com.tarento.commenthub.entity.CommentTree;
import com.tarento.commenthub.repository.CommentTreeRepository;
import com.tarento.commenthub.service.CommentTreeService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
public class CommentTreeServiceImpl implements CommentTreeService {

    @Value("${jwt.secret.key}")
    private String jwtSecretKey;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommentTreeRepository commentTreeRepository;

    @Override
    public CommentTree createOrUpdateCommentTree(JsonNode commentData) {
        if (commentData.get(Constants.COMMENT_TREE_ID) != null && !commentData.get(Constants.COMMENT_TREE_ID).isEmpty()) {
            return updateCommentTree(commentData);
        } else {
            return createCommentTree(commentData);
        }
    }

    @Override
    public CommentTree getCommentTreeById(String commentTreeId) {
        Optional<CommentTree> optionalCommentTree = commentTreeRepository.findById(commentTreeId);
        if(optionalCommentTree.isPresent()) {
            return optionalCommentTree.get();
        }
        return null;
    }

    @Override
    public CommentTree getCommentTree(CommentsRequestDTO commentsRequestDTO) {
        String commentTreeId = generateJwtTokenKey(commentsRequestDTO.getEntityID(), commentsRequestDTO.getEntityType(), commentsRequestDTO.getWorkflow());
        Optional<CommentTree> optionalCommentTree = commentTreeRepository.findById(commentTreeId);
        if(optionalCommentTree.isPresent()) {
            return optionalCommentTree.get();
        }
        return null;
    }

    public CommentTree createCommentTree(JsonNode commentData) {
        CommentsRequestDTO commentsRequestDTO = getCommentsRequestDTO(commentData);
        String commentTreeId = generateJwtTokenKey(commentsRequestDTO.getEntityID(), commentsRequestDTO.getEntityType(), commentsRequestDTO.getWorkflow());
        log.info("commentTreeId {}", commentTreeId);
        CommentTree commentTree = new CommentTree();
        commentTree.setCommentTreeId(commentTreeId);
        ObjectNode commentTreeJson = null;
        commentTreeJson.put("commentTreeId", commentTreeId);
        commentTreeJson.put("entityId", commentData.get("entityId").asText());
        commentTreeJson.put("entityType", commentData.get("entityType").asText());
        commentTreeJson.put("workflow", commentData.get("workflow").asText());

        Comments comments = new Comments();
        comments.setCommentId(commentData.get(Constants.COMMENT_ID).asText());

        ObjectNode commentsNode = objectMapper.valueToTree(comments);
        commentTreeJson.put("comments", commentsNode);
        List<String> childNodes = new ArrayList<>();
        childNodes.add(commentData.get(Constants.COMMENT_ID).asText());
        ObjectNode childNodesObject = objectMapper.valueToTree(childNodes);

        commentTreeJson.put("childNodes", childNodesObject);

        commentTree.setCommentTreeJson(commentTreeJson);
        return commentTreeRepository.save(commentTree);
    }

    public CommentTree updateCommentTree(JsonNode commentData) {
        CommentTree commentTree;
        String commentTreeId = commentData.get(Constants.COMMENT_TREE_ID).asText();
        Optional<CommentTree> optCommentTree = commentTreeRepository.findById(commentTreeId);
        if(optCommentTree.isPresent()) {
            commentTree = optCommentTree.get();
            JsonNode commentTreeJson = commentTree.getCommentTreeJson();
            Comments myEntity = objectMapper.convertValue(commentTreeJson.get("comments"), Comments.class);


        }
        return null;
    }

    public String generateJwtTokenKey(String entityId, String entityType, String workflow) {
        log.info("generating JwtTokenKey");
        String jwtToken = "";
        if (StringUtils.isNotBlank(entityId)
                && StringUtils.isNotBlank(entityType)
                && StringUtils.isNotBlank(workflow)) {
            LinkedHashMap<String, String> payload = new LinkedHashMap<>();
            payload.put(Constants.ENTITY_ID, entityId);
            payload.put(Constants.ENTITY_TYPE, entityType);
            payload.put(Constants.WORKFLOW, workflow);

            jwtToken = JWT.create().withPayload(payload).sign(Algorithm.HMAC256(jwtSecretKey));
        }
        return jwtToken;
    }

    public CommentsRequestDTO getCommentsRequestDTO(JsonNode commentData) {
        String entityId = objectMapper.convertValue(commentData.get("entityId"), String.class);
        String entityType = objectMapper.convertValue(commentData.get("entityType"), String.class);
        String workflow = objectMapper.convertValue(commentData.get("workflow"), String.class);
        CommentsRequestDTO commentsRequestDTO = new CommentsRequestDTO();
        commentsRequestDTO.setEntityID(entityId);
        commentsRequestDTO.setEntityType(entityType);
        commentsRequestDTO.setWorkflow(workflow);
        return commentsRequestDTO;
    }

}
