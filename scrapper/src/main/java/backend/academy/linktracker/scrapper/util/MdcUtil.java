package backend.academy.linktracker.scrapper.util;

import org.slf4j.MDC;

public class MdcUtil {

    private static final String CHAT_ID = "chatId";
    private static final String LINK_ID = "linkId";
    private static final String URL = "url";
    private static final String OWNER = "owner";
    private static final String REPO = "repo";
    private static final String PR_NUMBER = "prNumber";
    private static final String ISSUE_NUMBER = "issueNumber";
    private static final String CHANGE_TYPE = "changeType";
    private static final String BATCH_NUMBER = "batchNumber";
    private static final String THREAD_NAME = "threadName";

    private MdcUtil() {}

    public static void putChatId(long chatId) {
        MDC.put(CHAT_ID, String.valueOf(chatId));
    }

    public static void putLinkId(long linkId) {
        MDC.put(LINK_ID, String.valueOf(linkId));
    }

    public static void putUrl(String url) {
        MDC.put(URL, url);
    }

    public static void putOwner(String owner) {
        MDC.put(OWNER, owner);
    }

    public static void putRepo(String repo) {
        MDC.put(REPO, repo);
    }

    public static void putPrNumber(int prNumber) {
        MDC.put(PR_NUMBER, String.valueOf(prNumber));
    }

    public static void putIssueNumber(int issueNumber) {
        MDC.put(ISSUE_NUMBER, String.valueOf(issueNumber));
    }

    public static void putChangeType(String changeType) {
        MDC.put(CHANGE_TYPE, changeType);
    }

    public static void putBatchNumber(int batchNumber) {
        MDC.put(BATCH_NUMBER, String.valueOf(batchNumber));
    }

    public static void putThreadName(String threadName) {
        MDC.put(THREAD_NAME, threadName);
    }

    public static void putAll(String chatId, String linkId, String url) {
        if (chatId != null) {
            MDC.put(CHAT_ID, chatId);
        }
        if (linkId != null) {
            MDC.put(LINK_ID, linkId);
        }
        if (url != null) {
            MDC.put(URL, url);
        }
    }

    public static void clear() {
        MDC.clear();
    }

    public static void clearChatId() {
        MDC.remove(CHAT_ID);
    }

    public static void clearLinkId() {
        MDC.remove(LINK_ID);
    }

    public static void clearUrl() {
        MDC.remove(URL);
    }

    public static void clearOwner() {
        MDC.remove(OWNER);
    }

    public static void clearRepo() {
        MDC.remove(REPO);
    }

    public static void clearPrNumber() {
        MDC.remove(PR_NUMBER);
    }

    public static void clearIssueNumber() {
        MDC.remove(ISSUE_NUMBER);
    }

    public static void clearChangeType() {
        MDC.remove(CHANGE_TYPE);
    }

    public static void clearBatchNumber() {
        MDC.remove(BATCH_NUMBER);
    }

    public static void clearThreadName() {
        MDC.remove(THREAD_NAME);
    }
}
