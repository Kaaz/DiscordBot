/*
 * Copyright 2017 github.com/kaaz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package discordbot.templates;

import discordbot.main.Config;

public enum TemplateArgument {
    ARG("arg1", "First input argument", e -> e.arg != null ? e.arg : ""),
    ARGS("allargs", "All input arguments", e -> e.args != null ? e.args : ""),

    USER("user", "Username", e -> e.user != null ? e.user.getName() : ""),
    USER_MENTION("user-mention", "Mentions user", e -> e.user != null ? e.user.getAsMention() : ""),
    USER_ID("user-id", "User's id", e -> e.user != null ? e.user.getId() : ""),
    USER_DESCRIMINATOR("discrim", "Discriminator of the user", e -> e.user != null ? e.user.getDiscriminator() : ""),

    NICKNAME("nick", "Nickname of user", e -> e.user != null && e.guild != null ? e.guild.getMember(e.user).getEffectiveName() : ""),
    GUILD("guild", "Guild name", e -> e.guild != null ? e.guild.getName() : ""),
    GUILD_ID("guild-id", "Guild's id", e -> e.guild != null ? e.guild.getName() : ""),
    GUILD_USERS("guild-users", "Sums guild members", e -> e.guild != null ? Integer.toString(e.guild.getMembers().size()) : ""),

    CHANNEL("channel", "Channel name", e -> e.channel != null ? e.channel.getName() : ""),
    CHANNEL_ID("channel-id", "Channel id", e -> e.channel != null ? e.channel.getId() : ""),
    CHANNEL_MENTION("channel-mention", "Mentions channel", e -> e.channel != null ? e.channel.getAsMention() : ""),

    ROLE("role", "Role name", e -> e.role != null ? e.role.getName() : ""),
    ROLE_ID("role-id", "Role's id", e -> e.role != null ? e.role.getId() : ""),
    ROLE_MENTION("role-mention", "mentions the role", e -> e.role != null ? e.role.isMentionable() ? e.role.getAsMention() : e.role.getName() : ""),;

    private final String pattern;
    private final TemplateParser parser;
    private final String description;

    TemplateArgument(String pattern, String description, TemplateParser parser) {
        this.pattern = Config.TEMPLATE_QUOTE + pattern + Config.TEMPLATE_QUOTE;
        this.parser = parser;
        this.description = description;
    }

    public String getPattern() {
        return pattern;
    }

    public String parse(TemplateVariables vars) {
        return parser.apply(vars);
    }

    public String getDescription() {
        return description;
    }
}
