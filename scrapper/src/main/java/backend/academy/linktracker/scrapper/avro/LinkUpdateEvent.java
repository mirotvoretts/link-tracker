package backend.academy.linktracker.scrapper.avro;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificRecord;
import org.apache.avro.specific.SpecificRecordBase;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({"EQ_UNUSABLE_FOR_EQUALITY", "EQ_DOESNT_OVERRIDE_EQUALS", "unused"})
public class LinkUpdateEvent extends SpecificRecordBase implements SpecificRecord {

    private volatile long id;
    private String url;
    private String description;
    private List<Long> tgChatIds;

    private static final AtomicReference<Schema> CACHED_SCHEMA = new AtomicReference<>();

    private static Schema initializeSchema() {
        String schemaJson =
                "{\"type\":\"record\",\"name\":\"LinkUpdateEvent\",\"namespace\":\"com.example.notification\","
                        + "\"doc\":\"Event describing a link update notification\",\"fields\":["
                        + "{\"name\":\"id\",\"type\":\"long\",\"doc\":\"ID ссылки\"},"
                        + "{\"name\":\"url\",\"type\":\"string\",\"doc\":\"Ссылка\"},"
                        + "{\"name\":\"description\",\"type\":\"string\",\"doc\":\"Описание изменений\"},"
                        + "{\"name\":\"tgChatIds\",\"type\":{\"type\":\"array\",\"items\":\"long\"},\"doc\":\"ИД чатов\"}]}";
        return new Schema.Parser().parse(schemaJson);
    }

    @Override
    public Schema getSchema() {
        Schema schema = CACHED_SCHEMA.get();
        if (schema == null) {
            schema = initializeSchema();
            CACHED_SCHEMA.compareAndSet(null, schema);
        }
        return new Schema.Parser().parse(CACHED_SCHEMA.get().toString());
    }

    @SuppressWarnings("unused")
    public static Schema getClassSchema() {
        Schema schema = CACHED_SCHEMA.get();
        if (schema == null) {
            schema = initializeSchema();
            CACHED_SCHEMA.compareAndSet(null, schema);
        }
        return new Schema.Parser().parse(CACHED_SCHEMA.get().toString());
    }

    @Override
    public Object get(int field) {
        return switch (field) {
            case 0 -> id;
            case 1 -> url;
            case 2 -> description;
            case 3 -> tgChatIds;
            default -> throw new IndexOutOfBoundsException("Invalid field index: " + field);
        };
    }

    @Override
    public void put(int field, Object value) {
        switch (field) {
            case 0 -> this.id = (long) value;
            case 1 -> this.url = (String) value;
            case 2 -> this.description = (String) value;
            case 3 -> this.tgChatIds = castToList(value);
            default -> throw new IndexOutOfBoundsException("Invalid field index: " + field);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Long> castToList(Object value) {
        return (List<Long>) value;
    }
}
