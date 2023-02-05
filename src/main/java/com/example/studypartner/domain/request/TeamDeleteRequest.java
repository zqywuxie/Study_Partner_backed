package com.example.studypartner.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author wuxie
 */
@Data
public class TeamDeleteRequest  implements Serializable {

    private static final long serialVersionUID = 6264200254511706559L;
    /**
     * 解散队伍Id
     */
    private Long teamId;
}
