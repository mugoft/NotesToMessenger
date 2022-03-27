package com.mugoft.telegram.apiwrapper;//package com.mugoft.telegram.apiwrapper;

import com.mugoft.notesrepos.aws.DynamoDbHelper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static com.mugoft.LambdaHandler.*;

public class DynamoDbHelperUnitTest extends AbstractDynamoDbUnitTest {

    @Order(0)
    @Test
    public void getNextNoteMainChatTest() {
        var notesStatus = DynamoDbHelper.getNotesStatusAskedTimeMin(enhancedClient, chatIdQuestions, tableNameNotesStatus);
        Assertions.assertThat(notesStatus).isNotNull();

        var note = DynamoDbHelper.getNote(enhancedClient, tableNameNotes, notesStatus.getNote_id());
        Assertions.assertThat(note.getNote_id()).isEqualTo(noteRecordTest.getNote_id());
    }

    @Order(1)
    @Test
    public void getNoteTest() {
        var note = DynamoDbHelper.getNote(enhancedClient, tableNameNotes, noteRecordTest.getNote_id());
        Assertions.assertThat(note).isNotNull();
    }

    @Order(10)
    @Test
    public void updateNotesStatusTest() {
        Long time = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

        notesStatusRecordTest.setLast_asked_time(time);

        var note = DynamoDbHelper.updateNotesStatus(enhancedClient, tableNameNotesStatus, notesStatusRecordTest);
        Assertions.assertThat(note).isNotNull();
        Assertions.assertThat(note.getLast_asked_time()).isEqualTo(time);
    }
}