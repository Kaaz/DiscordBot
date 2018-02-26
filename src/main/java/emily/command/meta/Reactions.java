package emily.command.meta;

import emily.util.Emojibet;

public enum Reactions {
    STAR(ReactionType.USER_INPUT, Emojibet.STAR, "Starboard, See the starboard command for more info"),
    SKIP_TRACK(ReactionType.MUSIC, Emojibet.NEXT_TRACK, "Vote to skip the now playing track"),
    ;

    private final ReactionType reactionType;
    private final String emote;
    private final String description;

    Reactions(ReactionType reactionType, String emote, String description) {

        this.reactionType = reactionType;
        this.emote = emote;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getEmote() {
        return emote;
    }

    public ReactionType getReactionType() {
        return reactionType;
    }
}
