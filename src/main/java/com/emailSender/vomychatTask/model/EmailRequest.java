package com.emailSender.vomychatTask.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class EmailRequest {
    private String to;
    private String subject;
    private String content;
}
