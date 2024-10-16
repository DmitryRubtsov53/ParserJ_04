package dn.rubtsov.parserj_04.processor;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaMessageConsumer {

    @KafkaListener(topics = "testJson", groupId = "test_groupId")
    public void consume(String message) {
        //DBUtils.insertRecords(ParserJson.parseJson(message));
    }
}
