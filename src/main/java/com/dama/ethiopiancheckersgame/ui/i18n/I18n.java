package com.dama.ethiopiancheckersgame.ui.i18n;

import java.util.*;
import java.util.function.Consumer;

/**
 * Clean Internationalization (i18n) Manager for Ethiopian Checkers.
 * Manages strict language separation between Amharic (አማርኛ) and English.
 */
public final class I18n {
    private static Language currentLanguage = Language.AMHARIC;
    private static final List<Consumer<Language>> listeners = new ArrayList<>();

    private static final Map<String, String> EN = new HashMap<>();
    private static final Map<String, String> AM = new HashMap<>();

    static {
        // App Header
        init("app.title", "Ethiopian Checkers", "የኢትዮጵያ ዳማ");
        init("app.subtitle", "Tournament Edition", "የውድድር እትም");

        // Rule Variants
        init("section.rules", "1. Select Rule Variant", "1. የደንብ አይነት ይምረጡ");
        init("variant.egregna.title", "🛡️ Egregna (Foot Soldier)", "🛡️ እግረኛ");
        init("variant.egregna.desc", "• Regular: Move & Capture forward only\n• King: 1-step Short King in all 4 diagonals",
                                     "• መደበኛ: ወደፊት ብቻ ይንቀሳቀሳል፣ ወደፊት ብቻ ይበላል\n• ዳማ: በ 4ቱም አቅጣጫ 1 እርምጃ ብቻ");
        init("variant.tankegna.title", "⚡ Tankegna (Armored Tank)", "⚡ ታንከኛ");
        init("variant.tankegna.desc", "• Regular: 4-Way multi-directional jump\n• King: Long-range Flying King (Bishop slide)",
                                      "• መደበኛ: በ 4ቱም አቅጣጫ ይበላል\n• ዳማ: በማንኛውም ርቀት የሚበር ዳማ");

        // Game Modes
        init("section.modes", "2. Select Game Mode", "2. የጨዋታ አይነት ይምረጡ");
        init("mode.ai", "🤖 Solo Play (vs AI)", "🤖 ነጠላ ተጫዋች (ከኮምፒውተር ጋር)");
        init("mode.pass", "👥 Pass & Play (Local)", "👥 ሁለት ተጫዋቾች (በአንድ መሣሪያ)");
        init("mode.online", "🌐 Online Multiplayer", "🌐 የመስመር ላይ ጨዋታ");

        // AI Difficulty
        init("ai.difficulty", "AI Difficulty:", "የኮምፒውተር ደረጃ:");
        init("diff.easy", "Easy", "ቀላል");
        init("diff.medium", "Medium", "መካከለኛ");
        init("diff.hard", "Hard", "ከባድ");

        // Player Labels
        init("player.1", "Player 1 (Red):", "ተጫዋች 1 (ቀይ):");
        init("player.2", "Player 2 (Black):", "ተጫዋች 2 (ጥቁር):");
        init("player.computer", "Computer", "ኮምፒውተር");
        init("player.online_opponent", "Online Opponent", "የመስመር ላይ ተጋጣሚ");

        // Online Matchmaking
        init("online.title", "🌐 Online Matchmaking", "🌐 የመስመር ላይ ግጥሚያ");
        init("online.quick_match", "⚡ Quick Match", "⚡ ፈጣን ግጥሚያ");
        init("online.create_room", "🔒 Create Room", "🔒 ክፍል ፍጠር");
        init("online.join", "Join", "ግባ");
        init("online.code_prompt", "Code (e.g. 4821)", "ኮድ (ለምሳሌ 4821)");
        init("online.hint", "Select Quick Match or enter a room code.", "ፈጣን ግጥሚያ ይምረጡ ወይም የክፍል ኮድ ያስገቡ።");
        init("online.searching", "Searching for opponent", "ተጫዋች በመፈለግ ላይ");
        init("online.room_created", "Room #%s created. Waiting for friend", "ክፍል #%s ተፈጥሯል። ጓደኛ በመጠበቅ ላይ");
        init("online.cancel", "✕ Cancel", "✕ ሰርዝ");
        init("online.play_ai", "🤖 Play with AI", "🤖 ከኮምፒውተር ጋር ተጫወት");
        init("online.disconnected", "Opponent disconnected.", "ተጋጣሚ ተቋርጧል።");

        // Themes
        init("theme.label", "Theme:", "መልክ:");
        init("theme.customize", "🎨 Customize...", "🎨 አብጅ...");
        init("theme.wood", "Classic Wood", "እንጨት");
        init("theme.heritage", "Ethiopian Heritage", "የኢትዮጵያ ቅርስ");
        init("theme.tournament", "Modern Tournament", "ውድድር");
        init("theme.dark", "Midnight Dark", "ጨለማ");
        init("theme.emerald", "Forest Emerald", "አረንጓዴ");

        // Buttons
        init("btn.start", "▶  START MATCH", "▶  ጨዋታ ጀምር");
        init("btn.new_game", "▶  New Game", "▶  አዲስ ጨዋታ");
        init("btn.review_board", "🔍  Review Board", "🔍  ቦርዱን ገምግም");
        init("btn.review_game", "🔍  Review Game", "🔍  ጨዋታውን ገምግም");
        init("btn.undo", "⤺ Undo Move", "⤺ እንቅስቃሴ መልስ");
        init("btn.draw", "🤝 Offer Draw", "🤝 አቻ ጠይቅ");
        init("btn.theme", "🎨 Theme / Colors", "🎨 መልክ / ቀለማት");
        init("btn.restart", "↻ Restart Game", "↻ እንደገና ጀምር");
        init("btn.menu", "☰ Main Menu", "☰ ዋና ማውጫ");

        // Gameplay In-Game Stats & Banners
        init("game.pieces_remaining", "Pieces remaining: ", "የቀሩ ጠጠሮች: ");
        init("game.kings", "Kings (Dama): ", "ዳማዎች: ");
        init("game.turn", "Turn: ", "ተራ: ");
        init("game.ai_thinking", "AI Thinking...", "ኮምፒውተር በማሰብ ላይ...");
        init("game.draw_banner", "Match Drawn (Draw)", "አቻ ተጠናቀቀ (አቻ)");
        init("game.game_over", "Game Over!", "ጨዋታው ተጠናቀቀ!");
        init("game.concluded", "GAME CONCLUDED", "ውድድሩ ተጠናቋል");
        init("game.review_mode", "🔍 Review Mode", "🔍 የግምገማ ሁኔታ");

        // Draw Dialogs
        init("dialog.draw_offered_title", "Draw Offer", "የአቻ ጥያቄ");
        init("dialog.draw_offered_header", "Opponent Offered a Draw", "ተጋጣሚ አቻ ጠይቋል");
        init("dialog.draw_offered_body", "A draw has been offered. Do you accept?", "የአቻ ጥያቄ ቀርቧል። ይቀበላሉ?");
        init("dialog.draw_declined_title", "Draw Offer Declined", "የአቻ ጥያቄ ውድቅ ተደርጓል");
        init("dialog.draw_declined_ai", "The Computer evaluated the position and determined there is still a chance to find a winner. The draw offer was declined.",
                                         "ኮምፒውተሩ የቦርዱን ሁኔታ ገምግሞ አሸናፊ የመለየት ዕድል እንዳለ አረጋግጧል። የአቻ ጥያቄው ውድቅ ተደርጓል።");
        init("dialog.draw_sent_title", "Draw Offer Sent", "የአቻ ጥያቄ ተልኳል");
        init("dialog.draw_sent_body", "Draw offer sent to online opponent. Waiting for their response...",
                                      "የአቻ ጥያቄ ወደ ተጋጣሚ ተልኳል። ምላሽ በመጠበቅ ላይ...");

        // Language Toggle
        init("lang.label", "Language / ቋንቋ:", "ቋንቋ:");
    }

    private static void init(String key, String en, String am) {
        EN.put(key, en);
        AM.put(key, am);
    }

    public static String get(String key) {
        Map<String, String> map = (currentLanguage == Language.AMHARIC) ? AM : EN;
        return map.getOrDefault(key, key);
    }

    public static String get(String key, Object... args) {
        String template = get(key);
        try {
            return String.format(template, args);
        } catch (Exception e) {
            return template;
        }
    }

    public static Language getCurrentLanguage() {
        return currentLanguage;
    }

    public static void setLanguage(Language language) {
        if (language != null && language != currentLanguage) {
            currentLanguage = language;
            for (Consumer<Language> listener : new ArrayList<>(listeners)) {
                listener.accept(language);
            }
        }
    }

    public static void toggleLanguage() {
        setLanguage(currentLanguage == Language.AMHARIC ? Language.ENGLISH : Language.AMHARIC);
    }

    public static void addListener(Consumer<Language> listener) {
        listeners.add(listener);
    }

    public static void removeListener(Consumer<Language> listener) {
        listeners.remove(listener);
    }
}
