package com.tankbattle.config;

import com.tankbattle.model.TileType;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

public class LevelConfigLoader implements LevelLoader {
    private static final Logger logger = Logger.getLogger(LevelConfigLoader.class.getName());

    @Override
    public LevelData loadLevel(int levelNumber) throws LevelLoadException {
        String resourcePath = "/levels/level" + levelNumber + ".json";
        try {
            InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is == null) {
                throw new LevelLoadException(LevelLoadException.ErrorType.MISSING_FILE,
                        "Level file not found: " + resourcePath);
            }
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            return parseLevelData(sb.toString(), levelNumber);
        } catch (LevelLoadException e) {
            throw e;
        } catch (Exception e) {
            throw new LevelLoadException(LevelLoadException.ErrorType.INVALID_FORMAT,
                    "Failed to load level " + levelNumber + ": " + e.getMessage());
        }
    }

    private LevelData parseLevelData(String json, int levelNumber) {
        TileType[][] mapLayout = new TileType[GameConfig.MAP_SIZE][GameConfig.MAP_SIZE];
        int totalEnemies = 10;
        int normalCount = 7;
        int fastCount = 2;
        int armoredCount = 1;

        try {
            String mapStr = extractJsonArray(json, "map");
            String[] rows = mapStr.split(";");
            if (rows.length != GameConfig.MAP_SIZE) {
                throw new LevelLoadException(LevelLoadException.ErrorType.INVALID_FORMAT,
                        "Map must have " + GameConfig.MAP_SIZE + " rows");
            }
            for (int row = 0; row < GameConfig.MAP_SIZE; row++) {
                String[] cols = rows[row].split(",");
                if (cols.length != GameConfig.MAP_SIZE) {
                    throw new LevelLoadException(LevelLoadException.ErrorType.INVALID_FORMAT,
                            "Map row must have " + GameConfig.MAP_SIZE + " columns");
                }
                for (int col = 0; col < GameConfig.MAP_SIZE; col++) {
                    mapLayout[row][col] = tileFromCode(Integer.parseInt(cols[col].trim()));
                }
            }
            totalEnemies = extractJsonInt(json, "totalEnemies", 10);
            normalCount = extractJsonInt(json, "normalCount", 7);
            fastCount = extractJsonInt(json, "fastCount", 2);
            armoredCount = extractJsonInt(json, "armoredCount", 1);
        } catch (LevelLoadException e) {
            throw e;
        } catch (Exception e) {
            logger.warning("Failed to parse level JSON, using defaults: " + e.getMessage());
        }

        if (normalCount + fastCount + armoredCount != totalEnemies) {
            throw new LevelLoadException(LevelLoadException.ErrorType.INVALID_FORMAT,
                    "Enemy count mismatch: normal+fast+armored != total");
        }
        if (totalEnemies < 5 || totalEnemies > 20) {
            throw new LevelLoadException(LevelLoadException.ErrorType.INVALID_FORMAT,
                    "totalEnemies must be between 5 and 20");
        }

        return new LevelData(levelNumber, mapLayout, totalEnemies, normalCount, fastCount, armoredCount);
    }

    private TileType tileFromCode(int code) {
        switch (code) {
            case 1: return TileType.BRICK;
            case 2: return TileType.STEEL;
            case 3: return TileType.WATER;
            case 4: return TileType.FOREST;
            case 5: return TileType.BASE;
            default: return TileType.EMPTY;
        }
    }

    private String extractJsonArray(String json, String key) {
        String searchKey = "\"" + key + "\":\"";
        int start = json.indexOf(searchKey);
        if (start < 0) return "";
        start += searchKey.length();
        int end = json.indexOf("\"", start);
        if (end < 0) return "";
        return json.substring(start, end);
    }

    private int extractJsonInt(String json, String key, int defaultVal) {
        try {
            String searchKey = "\"" + key + "\":";
            int start = json.indexOf(searchKey);
            if (start < 0) return defaultVal;
            start += searchKey.length();
            StringBuilder num = new StringBuilder();
            while (start < json.length()) {
                char c = json.charAt(start);
                if (c == ',' || c == '}' || c == ' ') break;
                num.append(c);
                start++;
            }
            return Integer.parseInt(num.toString().trim());
        } catch (Exception e) {
            return defaultVal;
        }
    }

    @Override
    public boolean levelExists(int levelNumber) {
        String resourcePath = "/levels/level" + levelNumber + ".json";
        return getClass().getResourceAsStream(resourcePath) != null;
    }
}