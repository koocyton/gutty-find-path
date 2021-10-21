package com.doopp.findroute.pojo.vo;

import lombok.Data;

@Data
public class RoomUser {

    private static final long serialVersionUID = 5163L;

    private Long id;

    private String nickname;

    private String portrait;

    private boolean isOnline;
}
