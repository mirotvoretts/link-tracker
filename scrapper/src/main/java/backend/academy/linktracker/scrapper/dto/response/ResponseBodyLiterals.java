package backend.academy.linktracker.scrapper.dto.response;

public class ResponseBodyLiterals {
    public static final String LINK_ADDED = "Ссылка успешно добавлена";
    public static final String LINK_DELETED = "Ссылка успешно убрана";
    public static final String LINK_ALREADY_EXISTS = "Ссылка уже отслеживается";

    public static final String CHAT_NOT_FOUND_OR_LINK_NOT_FOUND = "Чат не существует или ссылка не найдена";
    public static final String CHAT_REGISTERED = "Чат зарегистрирован";
    public static final String CHAT_ALREADY_REGISTERED = "Чат уже существует";
    public static final String CHAT_DELETED = "Чат успешно удалён";
    public static final String CHAT_NOT_FOUND = "Чат не существует";

    public static final String INVALID_REQUEST_PARAMETERS = "Некорректные параметры запроса";
    public static final String UNEXPECTED_ERROR = "Произошла непредвиденная ошибка";

    private ResponseBodyLiterals() {}
}
