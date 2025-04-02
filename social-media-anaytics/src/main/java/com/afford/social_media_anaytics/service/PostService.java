package com.afford.social_media_anaytics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private static final String BASE_URL = "http://20.244.56.144/evaluation-service";
    private final RestTemplate restTemplate;

    /**
     * Fetches the top 5 users with the highest number of posts
     */
    public List<Map.Entry<String, Integer>> getTopUsers() {
        Map<String, String> users = restTemplate.getForObject(BASE_URL + "/users", Map.class);
        if (users == null) return Collections.emptyList();

        Map<String, Integer> userPostCounts = new HashMap<>();

        users.forEach((userId, userName) -> {
            try {
                String userPostsUrl = BASE_URL + "/users/" + userId + "/posts";
                Map<String, List<Map<String, Object>>> response = restTemplate.getForObject(userPostsUrl, Map.class);
                List<Map<String, Object>> posts = response != null ? response.get("posts") : Collections.emptyList();
                userPostCounts.put(userId, posts.size());
            } catch (Exception e) {
                log.error("Error fetching posts for user " + userId + ": " + e.getMessage());
            }
        });

        return userPostCounts.entrySet()
                .stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue())) // Sort by post count (desc)
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * Fetches latest or popular posts dynamically
     */
    public List<Map<String, Object>> getPosts(String type) {
        if (!type.equals("latest") && !type.equals("popular")) {
            throw new IllegalArgumentException("Invalid type. Accepted values: latest, popular.");
        }

        Map<String, String> users = restTemplate.getForObject(BASE_URL + "/users", Map.class);
        if (users == null) return Collections.emptyList();

        List<Map<String, Object>> allPosts = new ArrayList<>();

        users.forEach((userId, userName) -> {
            try {
                String userPostsUrl = BASE_URL + "/users/" + userId + "/posts";
                Map<String, List<Map<String, Object>>> response = restTemplate.getForObject(userPostsUrl, Map.class);
                List<Map<String, Object>> posts = response != null ? response.get("posts") : Collections.emptyList();
                allPosts.addAll(posts);
            } catch (Exception e) {
                log.error("Error fetching posts for user " + userId + ": " + e.getMessage());
            }
        });

        if (type.equals("latest")) {
            return allPosts.stream()
                    .sorted((a, b) -> Integer.compare((int) b.get("id"), (int) a.get("id")))
                    .limit(5)
                    .collect(Collectors.toList());
        } else {
            Map<Integer, Integer> postCommentCounts = new HashMap<>();
            allPosts.forEach(post -> {
                try {
                    int postId = (int) post.get("id");
                    String commentsUrl = BASE_URL + "/posts/" + postId + "/comments";
                    Map<String, List<Map<String, Object>>> response = restTemplate.getForObject(commentsUrl, Map.class);
                    List<Map<String, Object>> comments = response != null ? response.get("comments") : Collections.emptyList();
                    postCommentCounts.put(postId, comments.size());
                } catch (Exception e) {
                    log.error("Error fetching comments for post " + post.get("id") + ": " + e.getMessage());
                }
            });

            return allPosts.stream()
                    .sorted((a, b) -> Integer.compare(postCommentCounts.getOrDefault((int) b.get("id"), 0),
                            postCommentCounts.getOrDefault((int) a.get("id"), 0)))
                    .limit(5)
                    .collect(Collectors.toList());
        }
    }
}