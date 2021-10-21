package com.doopp.findroute.pojo.message;

import lombok.Data;

@Data
public class SocketMsg {

    String action;

    String roomName;

    Long roomId;

    String senderName;

    Long senderId;

    String receiverName;

    Long receiverId;

    String message;
}
