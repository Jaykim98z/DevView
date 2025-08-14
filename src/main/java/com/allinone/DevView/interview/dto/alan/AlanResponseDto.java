package com.allinone.DevView.interview.dto.alan;

public record AlanResponseDto(Action action, String content) {
    public static record Action(String name, String speak) {}
}
