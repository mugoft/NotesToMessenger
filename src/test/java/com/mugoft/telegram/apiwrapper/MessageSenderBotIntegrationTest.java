package com.mugoft.telegram.apiwrapper;

import com.mugoft.LambdaHandler;
import com.mugoft.messengers.telegram.apiwrapper.MessageSenderBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.stream.Collectors;

import static com.mugoft.LambdaHandler.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author mugoft
 * @created 09/09/2021 - 17:33
 * @project NotesToMessenger
 */
public class MessageSenderBotIntegrationTest {
    @Test
    @Order(1)
    public void simpleMessageTest() {
        LambdaHandler handler = new LambdaHandler();
        MessageSenderBot sender = new MessageSenderBot(chatIdQuestions, handler.apiTokenMugoftBotQuestions);
        var msgText = "MessageSenderBotIntegrationTest#simpleMessageTest" + LocalDateTime.now();
        var msgResponse = sender.sendMessage(msgText, null);
        Assertions.assertNotNull(msgResponse);
        Assertions.assertEquals(msgResponse.text(), msgText);
        assertThat(msgResponse.messageId()).isNotNull().isGreaterThan(0);
    }


    @Test
    @Order(2)
    public void sendMessasgeGetUpdateAddReplyTest() throws InterruptedException {
        LambdaHandler handler = new LambdaHandler();
        MessageSenderBot sender = new MessageSenderBot(chatIdQuestions, handler.apiTokenMugoftBotQuestions);
        var msgText = "MessageSenderBotIntegrationTest#getUpdateTest" + LocalDateTime.now();
        // send 2 times
        var msgResponse = sender.sendMessage(msgText, null);
        Assertions.assertNotNull(msgResponse);
        msgResponse = sender.sendMessage(msgText, null);
        Assertions.assertNotNull(msgResponse);

        // wait a little bit until getUpdate will see the changes
        Thread.sleep(5000);
        sender = new MessageSenderBot(chatIdAnswers, handler.apiTokenMugoftBotAnswers);
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
