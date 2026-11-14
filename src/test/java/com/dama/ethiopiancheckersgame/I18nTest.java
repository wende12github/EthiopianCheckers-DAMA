package com.dama.ethiopiancheckersgame;

import com.dama.ethiopiancheckersgame.ui.i18n.I18n;
import com.dama.ethiopiancheckersgame.ui.i18n.Language;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class I18nTest {
    private Language originalLang;

    @BeforeEach
    public void setUp() {
        originalLang = I18n.getCurrentLanguage();
    }

    @AfterEach
    public void tearDown() {
        I18n.setLanguage(originalLang);
    }

    @Test
    public void testStrictLanguageSeparation() {
        // Amharic Mode
        I18n.setLanguage(Language.AMHARIC);
        assertEquals("የኢትዮጵያ ዳማ", I18n.get("app.title"));
        assertEquals("ጨዋታ ጀምር", I18n.get("btn.start").replace("▶", "").trim());
        assertEquals("እግረኛ", I18n.get("variant.egregna.title").replace("🛡️", "").trim());
        assertEquals("ታንከኛ", I18n.get("variant.tankegna.title").replace("⚡", "").trim());

        // Verify there is no mixed English in Amharic strings
        assertFalse(I18n.get("app.title").contains("Checkers"));
        assertFalse(I18n.get("variant.egregna.title").contains("Foot Soldier"));

        // English Mode
        I18n.setLanguage(Language.ENGLISH);
        assertEquals("Ethiopian Checkers", I18n.get("app.title"));
        assertEquals("START MATCH", I18n.get("btn.start").replace("▶", "").trim());
        assertEquals("Egregna (Foot Soldier)", I18n.get("variant.egregna.title").replace("🛡️", "").trim());
        assertEquals("Tankegna (Armored Tank)", I18n.get("variant.tankegna.title").replace("⚡", "").trim());

        // Verify there is no mixed Ge'ez in English strings
        assertFalse(I18n.get("app.title").contains("ዳማ"));
        assertFalse(I18n.get("variant.egregna.title").contains("እግረኛ"));
    }

    @Test
    public void testLanguageToggleNotification() {
        I18n.setLanguage(Language.ENGLISH);
        AtomicReference<Language> received = new AtomicReference<>();
        I18n.addListener(received::set);

        I18n.toggleLanguage();
        assertEquals(Language.AMHARIC, received.get());
        assertEquals(Language.AMHARIC, I18n.getCurrentLanguage());

        I18n.toggleLanguage();
        assertEquals(Language.ENGLISH, received.get());
        assertEquals(Language.ENGLISH, I18n.getCurrentLanguage());
    }
}
