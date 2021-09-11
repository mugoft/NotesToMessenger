package com.mugoft.notesrepos.aws.models;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * @author mugoft
 * @created 10/09/2021 - 21:41
 * @project NotesToMessenger
 */

@DynamoDbBean
public class NotesStatus {
    private Long note_id;
    private Long chat_id;
    private Long last_asked_time;


    @DynamoDbPartitionKey
    public Long getChat_id() {
        return chat_id;
    }

    public static String getChatIdName() {
        return "chat_id";
    }

    public void setChat_id(Long chat_id) {
        this.chat_id = chat_id;
    }

    @DynamoDbSortKey
    public Long getNote_id() {
        return note_id;
    }

    public static String getNoteIdName() {
        return "note_id";
    }

    public void setNote_id(Long note_id) {
        this.note_id = note_id;
    }


    public Long getLast_asked_time() {
        return last_asked_time;
    }

    public void setLast_asked_time(Long last_asked_time) {
        this.last_asked_time = last_asked_time;
    }

    @Override
    public String toString() {
        return "chat_id=" + chat_id + " note_id="+note_id + " last_asked_time="+last_asked_time;
    }
}
