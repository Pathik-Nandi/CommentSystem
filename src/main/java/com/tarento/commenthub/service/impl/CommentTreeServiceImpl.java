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
import com.tarento.commenthub.exception.CommentException;
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
        log.info("commentTreeId: {}", commentTreeId);

        try {
            CommentTree commentTree = new CommentTree();
            commentTree.setCommentTreeId(commentTreeId);
            JsonNode commentTreeJson = objectMapper.createObjectNode();
            ObjectNode commentTreeObjNode = (ObjectNode) commentTreeJson;
            commentTreeObjNode.put(Constants.COMMENT_TREE_ID, commentTreeId);
            commentTreeObjNode.put(Constants.ENTITY_ID, commentData.get(Constants.ENTITY_ID).asText());
            commentTreeObjNode.put(Constants.ENTITY_TYPE, commentData.get(Constants.ENTITY_TYPE).asText());
            commentTreeObjNode.put(Constants.WORKFLOW, commentData.get(Constants.WORKFLOW).asText());
            commentTreeObjNode.putArray(Constants.COMMENTS).add(commentData.get(Constants.COMMENT_ID));
            commentTreeObjNode.putArray(Constants.CHILD_NODES).add(commentData.get(Constants.COMMENT_ID));
            commentTree.setCommentTreeJson(commentTreeObjNode);
            return commentTreeRepository.save(commentTree);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommentException(Constants.ERROR, e.getMessage());
        }

    }

    public CommentTree updateCommentTree(JsonNode commentData) {
        CommentTree commentTree;
        String commentTreeId = commentData.get(Constants.COMMENT_TREE_ID).asText();
        Optional<CommentTree> optCommentTree = commentTreeRepository.findById(commentTreeId);
        if(optCommentTree.isPresent()) {
            commentTree = optCommentTree.get();
            JsonNode commentTreeJson = commentTree.getCommentTreeJson();

            try {
                String[] hierarchyPath = objectMapper.treeToValue(commentTreeJson.get(Constants.HIERARCHY_PATH), String[].class);
                String newCommentId = objectMapper.treeToValue(commentData.get(Constants.COMMENT_ID), String.class);

                // Find the target position based on the hierarchy path
                ObjectNode targetNode = (ObjectNode) findTargetNode(commentTreeJson.get(Constants.COMMENTS), hierarchyPath, 0);

                // Add the new comment
                ObjectNode newComment = objectMapper.createObjectNode();
                newComment.put(Constants.COMMENT_ID, newCommentId);
                targetNode.putArray(Constants.COMMENTS).add(newComment);
                return commentTreeRepository.save(commentTree);
            } catch (Exception e) {
                e.printStackTrace();
                throw new CommentException(Constants.ERROR, e.getMessage());
            }

        }
        return null;
    }

    public static JsonNode findTargetNode(JsonNode currentNode, String[] hierarchyPath, int index) {
        if (index >= hierarchyPath.length) {
            return currentNode;
        }

        String targetCommentId = hierarchyPath[index];
        if (currentNode.isArray()) {
            for (JsonNode childNode : currentNode) {
                if (childNode.isObject() && targetCommentId.equals(childNode.get(Constants.COMMENT_ID).asText())) {
                    return findTargetNode(childNode, hierarchyPath, index + 1);
                }
            }
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
        String entityId = objectMapper.convertValue(commentData.get(Constants.COMMENT_ID), String.class);
        String entityType = objectMapper.convertValue(commentData.get(Constants.ENTITY_TYPE), String.class);
        String workflow = objectMapper.convertValue(commentData.get(Constants.WORKFLOW), String.class);
        CommentsRequestDTO commentsRequestDTO = new CommentsRequestDTO();
        commentsRequestDTO.setEntityID(entityId);
        commentsRequestDTO.setEntityType(entityType);
        commentsRequestDTO.setWorkflow(workflow);
        return commentsRequestDTO;
    }


}
