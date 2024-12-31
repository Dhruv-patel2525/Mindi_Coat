package com.dhruv.minid_coat.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class ChatMessage {
    private String type;
    private String content;
    private String sender;
}
