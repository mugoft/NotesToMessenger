package com.mugoft.telegram.apiwrapper;//package com.mugoft.telegram.apiwrapper;

import com.mugoft.notesrepos.aws.DynamoDbHelper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static com.mugoft.telegram.apiwrapper.MessageSenderBotIntegrationTest.groupIdChannelTest;

public class DynamoDbHelperUnitTest extends AbstractDynamoDbUnitTest {
//    @Test
//    public void simpleReadFromNotesTest() {
//        DynamoDbReader dynamoReader = new DynamoDbReader();
//        try (DynamoDbClient client = DynamoDbClient.builder().build()) {
//            HashMap<String, AttributeValue> keyToGet = new HashMap<>();
//            keyToGet.put("note_id", AttributeValue.builder().n("1535693932765").build());
//            var noteMap = dynamoReader.getItem(client, TABLE_NAME_NOTES, keyToGet);
//            Map<String, String> noteMapProcessed = new HashMap<>();
//            for (var keyset : noteMap.entrySet()) {
//                noteMapProcessed.put(keyset.getKey(), dynamoReader.getNSValueFromAttributeValue(keyset.getValue()));
//            }
//
//            Assertions.assertThat(noteMapProcessed).isNotNull().isNotEmpty();
//            Gson gson = new Gson();
//            JsonElement jsonElement = gson.toJsonTree(noteMapProcessed);
//            Note note = gson.fromJson(jsonElement, Note.class);
//            Assertions.assertThat(note.getNote_id()).isEqualTo(1535693932765L);
//        }
//    }
//
//    @Test
//    public void simpleReadFromNotesStatusTest() {
//        DynamoDbReader dynamoReader = new DynamoDbReader();
//
//        try (DynamoDbClient client = DynamoDbClient.builder().build()) {
//            HashMap<String, AttributeValue> keyToGet = new HashMap<>();
//            keyToGet.put(NotesStatus.getChatIdName(), AttributeValue.builder().n("1001581998900").build());
//            keyToGet.put(NotesStatus.getNoteIdName(), AttributeValue.builder().n("1535693932765").build());
//            var noteMap = dynamoReader.getItem(client, TABLE_NAME_NOTES_STATUS, keyToGet);
//            Map<String, String> noteMapProcessed = new HashMap<>();
//            for (var keyset : noteMap.entrySet()) {
//                noteMapProcessed.put(keyset.getKey(), dynamoReader.getNSValueFromAttributeValue(keyset.getValue()));
//            }
//
//            Assertions.assertThat(noteMapProcessed).isNotNull().isNotEmpty();
//            Gson gson = new Gson();
//            JsonElement jsonElement = gson.toJsonTree(noteMapProcessed);
//            Note note = gson.fromJson(jsonElement, Note.class);
//            Assertions.assertThat(note.getNote_id()).isEqualTo(1535693932765L);
//        }
//    }


    @Order(0)
    @Test
    public void getNextNoteMainChatTest() {
        var notesStatus = DynamoDbHelper.getNotesStatusAskedTimeMin(enhancedClient, groupIdChannelTest, TABLE_NAME_NOTES_STATUS_TEST);
        Assertions.assertThat(notesStatus).isNotNull();

        var note = DynamoDbHelper.getNote(enhancedClient, TABLE_NAME_NOTES_TEST, notesStatus.getNote_id());
        Assertions.assertThat(note.getNote_id()).isEqualTo(noteRecordTest.getNote_id());
    }

    @Order(1)
    @Test
    public void getNoteTest() {
        var note = DynamoDbHelper.getNote(enhancedClient, TABLE_NAME_NOTES_TEST, noteRecordTest.getNote_id());
        Assertions.assertThat(note).isNotNull();
    }

    @Order(10)
    @Test
    public void updateNotesStatusTest() {
        Long time = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

        notesStatusRecordTest.setLast_asked_time(time);

        var note = DynamoDbHelper.updateNotesStatus(enhancedClient, TABLE_NAME_NOTES_STATUS_TEST, notesStatusRecordTest);
        Assertions.assertThat(note).isNotNull();
        Assertions.assertThat(note.getLast_asked_time()).isEqualTo(time);
    }
}