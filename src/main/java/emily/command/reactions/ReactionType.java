package emily.command.reactions;

import java.util.ArrayList;

public enum ReactionType {
    USER_INPUT("User reaction to messages", "Users reacting to messages"),
    MUSIC("Music reactions", "These reactions get placed under the now playing message");

    private final String title;
    private final String description;

    ReactionType(String title, String description) {

        this.title = title;
        this.description = description;
    }

    public ArrayList<Reactions> getReactions() {
        ArrayList<Reactions> r = new ArrayList<>();
        for (Reactions reactions : Reactions.values()) {
            if (reactions.getReactionType() == this) {
                r.add(reactions);
            }
        }
        return r;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }
}
