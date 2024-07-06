package com.example.studypartner.domain.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class TeamQuitRequest implements Serializable {


    private static final long serialVersionUID = 1534680337735119724L;
    /**
     * id
     */
    private Long teamId;

}