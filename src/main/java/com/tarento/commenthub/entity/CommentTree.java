package com.tarento.commenthub.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "comment_tree")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class CommentTree {

    @Id
    private String commentTreeId;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private JsonNode commentTreeJson;
}
