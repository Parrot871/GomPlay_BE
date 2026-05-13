package com.example.gomplay.global.websocket.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WsMessage<T> {
    private String type;
    private T data;
}