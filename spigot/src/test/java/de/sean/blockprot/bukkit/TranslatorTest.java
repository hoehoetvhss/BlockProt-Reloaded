/*
 * Copyright (C) 2021 - 2026 spnda
 * Modifications Copyright (C) 2025 - 2026 Zaynr (Zar)
 * This file is part of BlockProt Reloaded <https://github.com/VictorGugug/BlockProt-Reloaded>.
 * Based on BlockProt <https://github.com/spnda/BlockProt>.
 *
 * BlockProt is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BlockProt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BlockProt.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.sean.blockprot.bukkit;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TranslatorTest {

    private static ServerMock server;

    @BeforeAll
    static void setup() throws Exception {
        server = MockBukkit.mock();
        java.io.File tempDir = java.io.File.createTempFile("bp_translator_test", "");
        tempDir.delete();
        tempDir.mkdirs();
        tempDir.deleteOnExit();

        org.bukkit.configuration.file.YamlConfiguration mainYaml = new org.bukkit.configuration.file.YamlConfiguration();
        mainYaml.set("language", "translations_en.yml");
        mainYaml.save(new java.io.File(tempDir, "config.yml"));

        de.sean.blockprot.bukkit.config.DefaultConfig config = new de.sean.blockprot.bukkit.config.DefaultConfig(mainYaml, tempDir);
        var field = BlockProt.class.getDeclaredField("defaultConfig");
        field.setAccessible(true);
        field.set(null, config);

        try (var stream = TranslatorTest.class.getResourceAsStream("/lang/translations_en.yml")) {
            if (stream != null) {
                var langConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(
                    new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8));
                Translator.loadFromConfigs(langConfig, langConfig);
            }
        }
    }

    @AfterAll
    static void teardown() {
        Translator.resetTranslations();
        MockBukkit.unmock();
    }

    @Test
    void allTranslationKeysHaveDefaultMapping() {
        for (TranslationKey key : TranslationKey.values()) {
            String result = Translator.get(key);
            assertNotNull(result, "TranslationKey." + key + " returned null");
        }
    }

    @Test
    void translatorReturnsNonEmptyForCommonKeys() {
        String noPermission = Translator.get(TranslationKey.MESSAGES__NO_PERMISSION);
        assertFalse(noPermission.isBlank(), "MESSAGES__NO_PERMISSION should not be blank");
    }

    @Test
    void translatorFallsBackGracefully() {
        String result = Translator.get(TranslationKey.MESSAGES__LOCK_ON_PLACE_SUCCESS);
        assertNotNull(result);
    }

    @Test
    void koreanTranslationHasAllKeys() throws Exception {
        try (var stream = TranslatorTest.class.getResourceAsStream("/lang/translations_ko.yml")) {
            assertNotNull(stream, "translations_ko.yml should exist");
            var koConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(
                new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8));
            for (TranslationKey key : TranslationKey.values()) {
                assertTrue(koConfig.contains(key.toString(), true),
                    "translations_ko.yml is missing key: " + key);
            }
        }
    }
}
