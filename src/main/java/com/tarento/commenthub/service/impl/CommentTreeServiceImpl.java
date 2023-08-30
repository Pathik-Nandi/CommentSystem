package com.tarento.commenthub.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarento.commenthub.constant.Constants;
import com.tarento.commenthub.dto.CommentsRequestDTO;
import com.tarento.commenthub.entity.CommentTree;
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
    public void createOrUpdateCommentTree(JsonNode commentData) {
        if (!commentData.get(Constants.COMMENT_TREE_ID).isEmpty()) {
            updateCommentTree(commentData);
        } else {
            createCommentTree(commentData);
        }
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

    public void createCommentTree(JsonNode commentData) {
        CommentsRequestDTO commentsRequestDTO = getCommentsRequestDTO(commentData);
        String commentTreeId = generateJwtTokenKey(commentsRequestDTO.getEntityID(), commentsRequestDTO.getEntityType(), commentsRequestDTO.getWorkflow());
        log.info("commentTreeId {}", commentTreeId);

    }

    public void updateCommentTree(JsonNode commentData) {

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
