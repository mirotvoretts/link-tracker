package backend.academy.linktracker.ai.service;

public final class UpdatePriority {

    public static final String HIGH = "HIGH";
    public static final String MEDIUM = "MEDIUM";
    public static final String LOW = "LOW";

    private UpdatePriority() {}

    public static String max(String first, String second) {
        return weight(first) >= weight(second) ? first : second;
    }

    private static int weight(String priority) {
        return switch (priority) {
            case HIGH -> 3;
            case LOW -> 1;
            default -> 2;
        };
    }
}
