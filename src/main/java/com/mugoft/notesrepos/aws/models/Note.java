package com.mugoft.notesrepos.aws.models;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

/**
 * @author mugoft
 * @created 10/09/2021 - 21:34
 * @project NotesToMessenger
 */

@DynamoDbBean
public class Note {
    protected Long note_id;
    protected Long mod;
    protected String question;
    protected String answer;

     public static Builder builder() {
        return new Builder();
    }

    /**
     * NOTE: Setters and getters are still needed because of serialization
     *
     */
    @DynamoDbPartitionKey
    public Long getNote_id() {
        return note_id;
    }

    public static String getNoteIdName() {
        return "note_id";
    }

    public void setNote_id(Long note_id) {
        this.note_id = note_id;
    }

    public Long getMod() {
        return mod;
    }

    public void setMod(Long mod) {
        this.mod = mod;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public static class Builder {
        // we initialize Note immidiately as it is simple DTO model, without heavy operations inside constructor
        private Note note = new Note();

        public Builder withNoteId(Long noteId) {
            note.setNote_id(noteId);
            return this;
        }

        public Builder withMod(Long mod) {
            note.setMod(mod);
            return this;
        }

        public Builder withQuestion(String question) {
            note.setQuestion(question);
            return this;
        }

        public Builder withAnswer(String answer) {
            note.setAnswer(answer);
            return this;
        }

        public Note build() {
            return note;
        }
    }

    @Override
    public String toString() {
        return  "note_id="+note_id + " mod="+mod + " question="+question + " answer="+answer;
    }
}
