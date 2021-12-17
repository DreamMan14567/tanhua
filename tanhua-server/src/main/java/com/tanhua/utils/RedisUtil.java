package com.tanhua.utils;


public class RedisUtil {
    //保存用户点赞目标对象数据的key
    public static final String MAP_KEY_USER_LIKED = "MAP+USER_LIKED";
    //保存用户点赞数数量的key
    public static final String MAP_KEY_USER_LIED_COUNT = "MAP_USER_LIED_COUNT";

    /**
     * 拼接点赞的用户和点赞的目标id作为key
     *
     * @param likedUserId 点赞用户
     * @param targetId    点赞目标
     * @return
     */
    public static String getLikedKey(String likedUserId, String targetId) {
        StringBuilder sb = new StringBuilder(25);
        sb.append(likedUserId)
                .append("::")
                .append(targetId);
        return sb.toString();
    }
}
