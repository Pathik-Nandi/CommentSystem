package com.tarento.commenthub.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tarento.commenthub.constant.Constants;
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

import java.util.LinkedHashMap;
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
        if (commentData.get(Constants.COMMENT_TREE_ID) != null && !commentData.get(Constants.COMMENT_TREE_ID).asText().isEmpty()) {
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
        String commentTreeId = generateJwtTokenKey(commentsRequestDTO.getEntityId(), commentsRequestDTO.getEntityType(), commentsRequestDTO.getWorkflow());
        Optional<CommentTree> optionalCommentTree = commentTreeRepository.findById(commentTreeId);
        if(optionalCommentTree.isPresent()) {
            return optionalCommentTree.get();
        }
        throw new CommentException(Constants.ERROR, "Comment Tree not found");
    }

    public CommentTree createCommentTree(JsonNode commentData) {
        CommentsRequestDTO commentsRequestDTO = getCommentsRequestDTO(commentData);
        String commentTreeId = generateJwtTokenKey(commentsRequestDTO.getEntityId(), commentsRequestDTO.getEntityType(), commentsRequestDTO.getWorkflow());

        try {
            CommentTree commentTree = new CommentTree();
            commentTree.setCommentTreeId(commentTreeId);
            JsonNode commentTreeJson = objectMapper.createObjectNode();
            ObjectNode commentTreeObjNode = (ObjectNode) commentTreeJson;
            commentTreeObjNode.put(Constants.COMMENT_TREE_ID, commentTreeId);
            commentTreeObjNode.put(Constants.ENTITY_ID, commentData.get(Constants.ENTITY_ID).asText());
            commentTreeObjNode.put(Constants.ENTITY_TYPE, commentData.get(Constants.ENTITY_TYPE).asText());
            commentTreeObjNode.put(Constants.WORKFLOW, commentData.get(Constants.WORKFLOW).asText());
            // Create an object node for a comment entry
            ObjectNode commentEntryNode = objectMapper.createObjectNode();
            commentEntryNode.put(Constants.COMMENT_ID, commentData.get(Constants.COMMENT_ID));
            commentTreeObjNode.putArray(Constants.COMMENTS).add(commentEntryNode);

            commentTreeObjNode.putArray(Constants.CHILD_NODES).add(commentData.get(Constants.COMMENT_ID));
            commentTreeObjNode.putArray(Constants.FIRST_LEVEL_NODES).add(commentData.get(Constants.COMMENT_ID));
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
                // Create an object node for a comment entry
                ObjectNode commentEntryNode = objectMapper.createObjectNode();
                commentEntryNode.put(Constants.COMMENT_ID, commentData.get(Constants.COMMENT_ID));

                if(commentData.get(Constants.HIERARCHY_PATH) != null && !commentData.get(Constants.HIERARCHY_PATH).isEmpty()) {
                    String[] hierarchyPath = objectMapper.treeToValue(commentData.get(Constants.HIERARCHY_PATH), String[].class);
                    // Find the target position based on the hierarchy path
                    JsonNode targetJsonNode = findTargetNode(commentTreeJson.get(Constants.COMMENTS), hierarchyPath, 0);
                    if(targetJsonNode.isArray()) {
                        ArrayNode targetArrayNode = (ArrayNode) targetJsonNode;
                        targetArrayNode.add(commentEntryNode);
                    } else {
                        if(targetJsonNode.get(Constants.CHILDREN) != null) {
                            ArrayNode childrenArrayNode = (ArrayNode) targetJsonNode.get(Constants.CHILDREN);
                            childrenArrayNode.add(commentEntryNode);
                        } else {
                            ObjectNode targetObjectNode = (ObjectNode) targetJsonNode;
                            targetObjectNode.putArray(Constants.CHILDREN).add(commentEntryNode);
                        }
                    }
                } else {
                    ArrayNode targetArrayNode = (ArrayNode) commentTreeJson.get(Constants.COMMENTS);
                    targetArrayNode.add(commentEntryNode);
//                    Retrieve the existing firstLevelNodes array
                    ArrayNode firstLevelNodesArray = (ArrayNode) commentTreeJson.get(Constants.FIRST_LEVEL_NODES);
//                     Add the new comment ID to the existing firstLevelNodes array
                    firstLevelNodesArray.add(commentData.get(Constants.COMMENT_ID));
                }

                // Retrieve the existing childNodes array
                ArrayNode childNodesArray = (ArrayNode) commentTreeJson.get(Constants.CHILD_NODES);

//              Add the new comment ID to the existing childNodes array
                childNodesArray.add(commentData.get(Constants.COMMENT_ID));

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
                if (childNode.isObject() && targetCommentId.equals(childNode.get(Constants.COMMENT_ID).asText()) && childNode.get(Constants.CHILDREN) != null) {
                    return findTargetNode(childNode.get(Constants.CHILDREN), hierarchyPath, index + 1);
                } else if(childNode.isObject() && targetCommentId.equals(childNode.get(Constants.COMMENT_ID).asText())){
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
        log.info("commentTreeId: {}", jwtToken);
        return jwtToken;
    }

    public CommentsRequestDTO getCommentsRequestDTO(JsonNode commentData) {
        String entityId = objectMapper.convertValue(commentData.get(Constants.ENTITY_ID), String.class);
        String entityType = objectMapper.convertValue(commentData.get(Constants.ENTITY_TYPE), String.class);
        String workflow = objectMapper.convertValue(commentData.get(Constants.WORKFLOW), String.class);
        CommentsRequestDTO commentsRequestDTO = new CommentsRequestDTO();
        commentsRequestDTO.setEntityId(entityId);
        commentsRequestDTO.setEntityType(entityType);
        commentsRequestDTO.setWorkflow(workflow);
        return commentsRequestDTO;
    }


}
