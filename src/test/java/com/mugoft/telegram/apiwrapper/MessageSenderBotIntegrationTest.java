package com.mugoft.telegram.apiwrapper;

import com.mugoft.messengers.telegram.apiwrapper.MessageSenderBot;
import com.mugoft.notesrepos.aws.ParameterStoreHelper;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author mugoft
 * @created 09/09/2021 - 17:33
 * @project NotesToMessenger
 */
public class MessageSenderBotIntegrationTest {

    public static Long groupIdChannelTest = 1001581998900L;
    public static String apiTokenQuestionBotTest = "";

    public static Long groupIdGroupTest = 1001589619062L;
    public static String apiTokenAnswerBotTest = "";

    private static final String API_TOKEN_MUGOFT_BOT_QUESTIONS_KEY = "API_TOKEN_MUGOFT_BOT_QUESTIONS_TEST";
    private static final String API_TOKEN_MUGOFT_BOT_ANSWERS_KEY = "API_TOKEN_MUGOFT_BOT_ANSWERS_TEST";


    @BeforeAll
    public static void init() {
        apiTokenQuestionBotTest = ParameterStoreHelper.getParameter(API_TOKEN_MUGOFT_BOT_QUESTIONS_KEY);
        apiTokenAnswerBotTest = ParameterStoreHelper.getParameter(API_TOKEN_MUGOFT_BOT_ANSWERS_KEY);
    }

    @Test
    @Order(1)
    public void simpleMessageTest() {
        MessageSenderBot sender = new MessageSenderBot(groupIdChannelTest, apiTokenQuestionBotTest);
        var msgText = "MessageSenderBotIntegrationTest#simpleMessageTest" + LocalDateTime.now();
        var msgResponse = sender.sendMessage(msgText, null);
        Assertions.assertNotNull(msgResponse);
        Assertions.assertEquals(msgResponse.text(), msgText);
        assertThat(msgResponse.messageId()).isNotNull().isGreaterThan(0);
    }


    @Test
    @Order(2)
    public void sendMessasgeGetUpdateAddReplyTest() throws InterruptedException {
        MessageSenderBot sender = new MessageSenderBot(groupIdChannelTest, apiTokenQuestionBotTest);
        var msgText = "MessageSenderBotIntegrationTest#getUpdateTest" + LocalDateTime.now();
        // send 2 times
        var msgResponse = sender.sendMessage(msgText, null);
        Assertions.assertNotNull(msgResponse);
        msgResponse = sender.sendMessage(msgText, null);
        Assertions.assertNotNull(msgResponse);

        // wait a little bit until getUpdate will see the changes
        Thread.sleep(5000);
        sender = new MessageSenderBot(groupIdGroupTest, apiTokenAnswerBotTest);
        var updateResponse = sender.getUpdate();
        Assertions.assertNotNull(updateResponse);
        assertThat(updateResponse.updates()).isNotNull();
        assertThat(updateResponse.updates().size()).isGreaterThan(0);
        var messages = updateResponse.updates().stream().map(Update::message).filter(msg -> msg.text().equals(msgText)).collect(Collectors.toList());
        assertThat(messages).isNotNull();
        assertThat(messages.size()).isGreaterThan(0);
        var message = messages.stream().max(Comparator.comparing(Message::messageId));
        Assertions.assertTrue(message.isPresent());
        assertThat(message.get().text()).isEqualTo(msgText);

        var msgReplyResponse = sender.sendMessage("MessageSenderBotIntegrationTest#getUpdateTest::reply", message.get().messageId());
        assertThat(msgReplyResponse.replyToMessage().messageId()).isEqualTo(message.get().messageId());


    }
}
