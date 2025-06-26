package com.epam.gym.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class TokenBlacklistServiceTest {

    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUp() {
        tokenBlacklistService = new TokenBlacklistService();
    }

    @Nested
    @DisplayName("blacklistToken() method tests")
    class BlacklistTokenTests {

        @Test
        @DisplayName("Should successfully blacklist a valid token")
        void blacklistToken_WithValidToken_ShouldAddToBlacklist() {
            // Given
            String token = "valid-jwt-token-123";

            // When
            tokenBlacklistService.blacklistToken(token);

            // Then
            assertTrue(tokenBlacklistService.isTokenBlacklisted(token));
            assertEquals(1, tokenBlacklistService.getBlacklistedTokenCount());
        }

        @Test
        @DisplayName("Should handle null token gracefully")
        void blacklistToken_WithNullToken_ShouldAddToBlacklist() {
            // Given
            String token = null;

            // When
            tokenBlacklistService.blacklistToken(token);

            // Then
            assertTrue(tokenBlacklistService.isTokenBlacklisted(token));
            assertEquals(1, tokenBlacklistService.getBlacklistedTokenCount());
        }

        @Test
        @DisplayName("Should handle empty token")
        void blacklistToken_WithEmptyToken_ShouldAddToBlacklist() {
            // Given
            String token = "";

            // When
            tokenBlacklistService.blacklistToken(token);

            // Then
            assertTrue(tokenBlacklistService.isTokenBlacklisted(token));
            assertEquals(1, tokenBlacklistService.getBlacklistedTokenCount());
        }

        @Test
        @DisplayName("Should handle whitespace-only token")
        void blacklistToken_WithWhitespaceToken_ShouldAddToBlacklist() {
            // Given
            String token = "   ";

            // When
            tokenBlacklistService.blacklistToken(token);

            // Then
            assertTrue(tokenBlacklistService.isTokenBlacklisted(token));
            assertEquals(1, tokenBlacklistService.getBlacklistedTokenCount());
        }

        @Test
        @DisplayName("Should not add duplicate tokens")
        void blacklistToken_WithDuplicateToken_ShouldNotIncrementCount() {
            // Given
            String token = "duplicate-token";

            // When
            tokenBlacklistService.blacklistToken(token);
            tokenBlacklistService.blacklistToken(token);
            tokenBlacklistService.blacklistToken(token);

            // Then
            assertTrue(tokenBlacklistService.isTokenBlacklisted(token));
            assertEquals(1, tokenBlacklistService.getBlacklistedTokenCount());
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "jwt.token.with.dots",
                "token-with-dashes",
                "token_with_underscores",
                "TokenWithMixedCase",
                "token123WithNumbers",
                "very-long-token-that-might-represent-a-real-jwt-token-with-lots-of-characters-and-information"
        })
        @DisplayName("Should blacklist various token formats")
        void blacklistToken_WithVariousTokenFormats_ShouldSucceed(String token) {
            // When
            tokenBlacklistService.blacklistToken(token);

            // Then
            assertTrue(tokenBlacklistService.isTokenBlacklisted(token));
        }
    }

    @Nested
    @DisplayName("isTokenBlacklisted() method tests")
    class IsTokenBlacklistedTests {

        @Test
        @DisplayName("Should return true for blacklisted token")
        void isTokenBlacklisted_WithBlacklistedToken_ShouldReturnTrue() {
            // Given
            String token = "blacklisted-token";
            tokenBlacklistService.blacklistToken(token);

            // When
            boolean result = tokenBlacklistService.isTokenBlacklisted(token);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return false for non-blacklisted token")
        void isTokenBlacklisted_WithNonBlacklistedToken_ShouldReturnFalse() {
            // Given
            String token = "clean-token";

            // When
            boolean result = tokenBlacklistService.isTokenBlacklisted(token);

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("Should return false for null token when not blacklisted")
        void isTokenBlacklisted_WithNullToken_ShouldReturnFalse() {
            // Given
            String token = null;

            // When
            boolean result = tokenBlacklistService.isTokenBlacklisted(token);

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("Should return true for null token when blacklisted")
        void isTokenBlacklisted_WithBlacklistedNullToken_ShouldReturnTrue() {
            // Given
            String token = null;
            tokenBlacklistService.blacklistToken(token);

            // When
            boolean result = tokenBlacklistService.isTokenBlacklisted(token);

            // Then
            assertTrue(result);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"", "   ", "normal-token"})
        @DisplayName("Should handle edge case tokens correctly")
        void isTokenBlacklisted_WithEdgeCaseTokens_ShouldWorkCorrectly(String token) {
            // Given - token not blacklisted initially

            // When
            boolean resultBefore = tokenBlacklistService.isTokenBlacklisted(token);
            tokenBlacklistService.blacklistToken(token);
            boolean resultAfter = tokenBlacklistService.isTokenBlacklisted(token);

            // Then
            assertFalse(resultBefore);
            assertTrue(resultAfter);
        }

        @Test
        @DisplayName("Should be case sensitive")
        void isTokenBlacklisted_ShouldBeCaseSensitive() {
            // Given
            String originalToken = "CaseSensitiveToken";
            String lowerCaseToken = "casesensitivetoken";
            String upperCaseToken = "CASESENSITIVETOKEN";

            tokenBlacklistService.blacklistToken(originalToken);

            // When & Then
            assertTrue(tokenBlacklistService.isTokenBlacklisted(originalToken));
            assertFalse(tokenBlacklistService.isTokenBlacklisted(lowerCaseToken));
            assertFalse(tokenBlacklistService.isTokenBlacklisted(upperCaseToken));
        }
    }

    @Nested
    @DisplayName("clearBlacklist() method tests")
    class ClearBlacklistTests {

        @Test
        @DisplayName("Should clear all blacklisted tokens")
        void clearBlacklist_WithMultipleTokens_ShouldClearAll() {
            // Given
            String token1 = "token1";
            String token2 = "token2";
            String token3 = "token3";

            tokenBlacklistService.blacklistToken(token1);
            tokenBlacklistService.blacklistToken(token2);
            tokenBlacklistService.blacklistToken(token3);

            assertEquals(3, tokenBlacklistService.getBlacklistedTokenCount());

            // When
            tokenBlacklistService.clearBlacklist();

            // Then
            assertEquals(0, tokenBlacklistService.getBlacklistedTokenCount());
            assertFalse(tokenBlacklistService.isTokenBlacklisted(token1));
            assertFalse(tokenBlacklistService.isTokenBlacklisted(token2));
            assertFalse(tokenBlacklistService.isTokenBlacklisted(token3));
        }

        @Test
        @DisplayName("Should handle clearing empty blacklist")
        void clearBlacklist_WithEmptyBlacklist_ShouldNotThrowException() {
            // Given - empty blacklist
            assertEquals(0, tokenBlacklistService.getBlacklistedTokenCount());

            // When & Then - should not throw exception
            assertDoesNotThrow(() -> tokenBlacklistService.clearBlacklist());
            assertEquals(0, tokenBlacklistService.getBlacklistedTokenCount());
        }

        @Test
        @DisplayName("Should allow new tokens to be blacklisted after clearing")
        void clearBlacklist_ShouldAllowNewTokensAfterClearing() {
            // Given
            String initialToken = "initial-token";
            String newToken = "new-token";

            tokenBlacklistService.blacklistToken(initialToken);
            tokenBlacklistService.clearBlacklist();

            // When
            tokenBlacklistService.blacklistToken(newToken);

            // Then
            assertFalse(tokenBlacklistService.isTokenBlacklisted(initialToken));
            assertTrue(tokenBlacklistService.isTokenBlacklisted(newToken));
            assertEquals(1, tokenBlacklistService.getBlacklistedTokenCount());
        }
    }

    @Nested
    @DisplayName("getBlacklistedTokenCount() method tests")
    class GetBlacklistedTokenCountTests {

        @Test
        @DisplayName("Should return zero for empty blacklist")
        void getBlacklistedTokenCount_WithEmptyBlacklist_ShouldReturnZero() {
            // When
            int count = tokenBlacklistService.getBlacklistedTokenCount();

            // Then
            assertEquals(0, count);
        }

        @Test
        @DisplayName("Should return correct count for single token")
        void getBlacklistedTokenCount_WithSingleToken_ShouldReturnOne() {
            // Given
            tokenBlacklistService.blacklistToken("single-token");

            // When
            int count = tokenBlacklistService.getBlacklistedTokenCount();

            // Then
            assertEquals(1, count);
        }

        @Test
        @DisplayName("Should return correct count for multiple tokens")
        void getBlacklistedTokenCount_WithMultipleTokens_ShouldReturnCorrectCount() {
            // Given
            int expectedCount = 5;
            for (int i = 0; i < expectedCount; i++) {
                tokenBlacklistService.blacklistToken("token-" + i);
            }

            // When
            int count = tokenBlacklistService.getBlacklistedTokenCount();

            // Then
            assertEquals(expectedCount, count);
        }

        @Test
        @DisplayName("Should not count duplicate tokens multiple times")
        void getBlacklistedTokenCount_WithDuplicateTokens_ShouldNotCountDuplicates() {
            // Given
            String token = "duplicate-token";
            tokenBlacklistService.blacklistToken(token);
            tokenBlacklistService.blacklistToken(token);
            tokenBlacklistService.blacklistToken(token);

            // When
            int count = tokenBlacklistService.getBlacklistedTokenCount();

            // Then
            assertEquals(1, count);
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    class ThreadSafetyTests {

//        @Test
//        @DisplayName("Should handle concurrent blacklisting operations")
//        void concurrentBlacklisting_ShouldBeThreadSafe() throws InterruptedException {
//            // Given
//            int threadCount = 10;
//            int tokensPerThread = 100;
//            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
//
//            // When
//            CompletableFuture<Void>[] futures = IntStream.range(0, threadCount)
//                    .mapToObj(threadId -> CompletableFuture.runAsync(() -> {
//                        for (int i = 0; i < tokensPerThread; i++) {
//                            tokenBlacklistService.blacklistToken("thread-" + threadId + "-token-" + i);
//                        }
//                    }, executor))
//                    .toArray(CompletableFuture[]::new);
//
//            CompletableFuture.allOf(futures).join();
//            executor.shutdown();
//            executor.awaitTermination(5, TimeUnit.SECONDS);
//
//            // Then
//            assertEquals(threadCount * tokensPerThread, tokenBlacklistService.getBlacklistedTokenCount());
//
//            // Verify some random tokens are blacklisted
//            assertTrue(tokenBlacklistService.isTokenBlacklisted("thread-0-token-0"));
//            assertTrue(tokenBlacklistService.isTokenBlacklisted("thread-5-token-50"));
//            assertTrue(tokenBlacklistService.isTokenBlacklisted("thread-9-token-99"));
//        }
//
//        @Test
//        @DisplayName("Should handle concurrent read and write operations")
//        void concurrentReadWrite_ShouldBeThreadSafe() throws InterruptedException {
//            // Given
//            int writerThreads = 5;
//            int readerThreads = 5;
//            int operationsPerThread = 50;
//            ExecutorService executor = Executors.newFixedThreadPool(writerThreads + readerThreads);
//
//            // When - Start writer threads
//            CompletableFuture<Void>[] writerFutures = IntStream.range(0, writerThreads)
//                    .mapToObj(threadId -> CompletableFuture.runAsync(() -> {
//                        for (int i = 0; i < operationsPerThread; i++) {
//                            tokenBlacklistService.blacklistToken("writer-" + threadId + "-token-" + i);
//                        }
//                    }, executor))
//                    .toArray(CompletableFuture[]::new);
//
//            // Start reader threads
//            CompletableFuture<Void>[] readerFutures = IntStream.range(0, readerThreads)
//                    .mapToObj(threadId -> CompletableFuture.runAsync(() -> {
//                        for (int i = 0; i < operationsPerThread; i++) {
//                            // Read operations - checking both existing and non-existing tokens
//                            tokenBlacklistService.isTokenBlacklisted("writer-0-token-" + i);
//                            tokenBlacklistService.isTokenBlacklisted("non-existing-token-" + i);
//                            tokenBlacklistService.getBlacklistedTokenCount();
//                        }
//                    }, executor))
//                    .toArray(CompletableFuture[]::new);
//
//            CompletableFuture.allOf(writerFutures).join();
//            CompletableFuture.allOf(readerFutures).join();
//            executor.shutdown();
//            executor.awaitTermination(5, TimeUnit.SECONDS);
//
//            // Then
//            assertEquals(writerThreads * operationsPerThread, tokenBlacklistService.getBlacklistedTokenCount());
//        }
//
//        @Test
//        @DisplayName("Should handle concurrent clear operations")
//        void concurrentClear_ShouldBeThreadSafe() throws InterruptedException {
//            // Given
//            tokenBlacklistService.blacklistToken("token-1");
//            tokenBlacklistService.blacklistToken("token-2");
//            tokenBlacklistService.blacklistToken("token-3");
//
//            int clearThreads = 3;
//            ExecutorService executor = Executors.newFixedThreadPool(clearThreads);
//
//            // When
//            CompletableFuture<Void>[] futures = IntStream.range(0, clearThreads)
//                    .mapToObj(threadId -> CompletableFuture.runAsync(() -> {
//                        tokenBlacklistService.clearBlacklist();
//                    }, executor))
//                    .toArray(CompletableFuture[]::new);
//
//            CompletableFuture.allOf(futures).join();
//            executor.shutdown();
//            executor.awaitTermination(5, TimeUnit.SECONDS);
//
//            // Then
//            assertEquals(0, tokenBlacklistService.getBlacklistedTokenCount());
//        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle complete workflow correctly")
        void completeWorkflow_ShouldWorkCorrectly() {
            // Given
            String token1 = "workflow-token-1";
            String token2 = "workflow-token-2";
            String token3 = "workflow-token-3";

            // When & Then - Step 1: Initial state
            assertEquals(0, tokenBlacklistService.getBlacklistedTokenCount());
            assertFalse(tokenBlacklistService.isTokenBlacklisted(token1));

            // Step 2: Blacklist tokens
            tokenBlacklistService.blacklistToken(token1);
            tokenBlacklistService.blacklistToken(token2);
            tokenBlacklistService.blacklistToken(token3);

            assertEquals(3, tokenBlacklistService.getBlacklistedTokenCount());
            assertTrue(tokenBlacklistService.isTokenBlacklisted(token1));
            assertTrue(tokenBlacklistService.isTokenBlacklisted(token2));
            assertTrue(tokenBlacklistService.isTokenBlacklisted(token3));

            // Step 3: Clear blacklist
            tokenBlacklistService.clearBlacklist();

            assertEquals(0, tokenBlacklistService.getBlacklistedTokenCount());
            assertFalse(tokenBlacklistService.isTokenBlacklisted(token1));
            assertFalse(tokenBlacklistService.isTokenBlacklisted(token2));
            assertFalse(tokenBlacklistService.isTokenBlacklisted(token3));

            // Step 4: Blacklist again after clear
            tokenBlacklistService.blacklistToken(token1);

            assertEquals(1, tokenBlacklistService.getBlacklistedTokenCount());
            assertTrue(tokenBlacklistService.isTokenBlacklisted(token1));
            assertFalse(tokenBlacklistService.isTokenBlacklisted(token2));
        }

        @Test
        @DisplayName("Should handle large number of tokens efficiently")
        void largeScale_ShouldHandleEfficientlyy() {
            // Given
            int tokenCount = 1000;

            // When
            for (int i = 0; i < tokenCount; i++) {
                tokenBlacklistService.blacklistToken("large-scale-token-" + i);
            }

            // Then
            assertEquals(tokenCount, tokenBlacklistService.getBlacklistedTokenCount());

            // Verify random tokens
            assertTrue(tokenBlacklistService.isTokenBlacklisted("large-scale-token-0"));
            assertTrue(tokenBlacklistService.isTokenBlacklisted("large-scale-token-500"));
            assertTrue(tokenBlacklistService.isTokenBlacklisted("large-scale-token-999"));
            assertFalse(tokenBlacklistService.isTokenBlacklisted("large-scale-token-1000"));
        }
    }
}