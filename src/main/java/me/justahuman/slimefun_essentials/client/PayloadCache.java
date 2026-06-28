package me.justahuman.slimefun_essentials.client;

import me.justahuman.slimefun_essentials.SlimefunEssentials;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * 服务端 Payload 数据的客户端持久化缓存。
 * <p>
 * 缓存目录：{@code <game>/config/slimefun_essentials_cache/}
 * <p>
 * 缓存作为资源包模式的补充：
 * <ul>
 *   <li>每次服务端 Payload 到达后，按 (type, key) 覆盖写入对应缓存文件</li>
 *   <li>同一 (type, key) 的下次发送会直接覆盖旧文件</li>
 *   <li>加载时读取该 type 下所有缓存文件并反序列化</li>
 * </ul>
 */
public final class PayloadCache {
    private static final Path CACHE_DIR = FabricLoader.getInstance().getConfigDir().resolve("slimefun_essentials_cache");

    public enum Type {
        ITEMS("items"),
        RECIPE_CATEGORIES("recipe_categories"),
        COMPONENT_TYPES("component_types"),
        RECIPE_DISPLAYS("recipe_displays");

        private final String dirName;

        Type(String dirName) {
            this.dirName = dirName;
        }

        public String dirName() {
            return dirName;
        }
    }

    private PayloadCache() {}

    public static Path typeDir(Type type) {
        return CACHE_DIR.resolve(type.dirName());
    }

    public static Path cacheFile(Type type, String key) {
        return typeDir(type).resolve(safeKey(key) + ".bin");
    }

    /**
     * 将一个 Payload 的原始字节按 (type, key) 覆盖写入缓存文件。
     */
    public static void write(Type type, String key, byte[] data) {
        try {
            Files.createDirectories(typeDir(type));
            Files.write(cacheFile(type, key), data);
        } catch (IOException e) {
            SlimefunEssentials.LOGGER.error("Failed to write payload cache: {}/{}", type, key, e);
        }
    }

    /**
     * 读取指定 (type, key) 的缓存字节。若不存在返回 null。
     */
    public static byte[] read(Type type, String key) {
        Path file = cacheFile(type, key);
        if (!Files.exists(file)) {
            return null;
        }
        try {
            return Files.readAllBytes(file);
        } catch (IOException e) {
            SlimefunEssentials.LOGGER.error("Failed to read payload cache: {}/{}", type, key, e);
            return null;
        }
    }

    /**
     * 列出指定 type 下所有缓存文件并返回 (key, bytes) 列表。
     */
    public static List<CacheEntry> readAll(Type type) {
        Path dir = typeDir(type);
        if (!Files.exists(dir)) {
            return List.of();
        }
        List<CacheEntry> result = new ArrayList<>();
        try (Stream<Path> paths = Files.list(dir)) {
            paths.filter(Files::isRegularFile).forEach(file -> {
                String fileName = file.getFileName().toString();
                if (!fileName.endsWith(".bin")) {
                    return;
                }
                String key = fileName.substring(0, fileName.length() - 4);
                try {
                    result.add(new CacheEntry(key, Files.readAllBytes(file)));
                } catch (IOException e) {
                    SlimefunEssentials.LOGGER.error("Failed to read cache file: {}", file, e);
                }
            });
        } catch (IOException e) {
            SlimefunEssentials.LOGGER.error("Failed to list cache dir: {}", dir, e);
        }
        return result;
    }

    /**
     * 判断指定 type 下是否存在任何缓存。
     */
    public static boolean hasAny(Type type) {
        Path dir = typeDir(type);
        if (!Files.exists(dir)) {
            return false;
        }
        try (Stream<Path> paths = Files.list(dir)) {
            return paths.anyMatch(Files::isRegularFile);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 清空所有缓存。
     */
    public static void clear() {
        for (Type type : Type.values()) {
            Path dir = typeDir(type);
            if (!Files.exists(dir)) {
                continue;
            }
            try (Stream<Path> paths = Files.list(dir)) {
                paths.forEach(file -> {
                    try {
                        Files.deleteIfExists(file);
                    } catch (IOException e) {
                        SlimefunEssentials.LOGGER.error("Failed to delete cache file: {}", file, e);
                    }
                });
            } catch (IOException e) {
                SlimefunEssentials.LOGGER.error("Failed to clear cache dir: {}", dir, e);
            }
        }
    }

    private static String safeKey(String key) {
        return key.replaceAll("[^a-zA-Z0-9_\\-.]", "_");
    }

    public record CacheEntry(String key, byte[] data) {}
}
