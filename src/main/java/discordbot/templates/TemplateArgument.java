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
    USER("user", (e) -> e.USER != null ? e.USER.getName() : ""),
    USER_MENTION("user-mention", (e) -> e.USER != null ? e.USER.getAsMention() : ""),
    USER_ID("user-id", (e) -> e.USER != null ? e.USER.getId() : ""),
    USER_DESCRIMINATOR("discrim", (e) -> e.USER != null ? e.USER.getDiscriminator() : ""),
    NICKNAME("nick", (e) -> e.USER != null && e.GUILD != null ? e.GUILD.getMember(e.USER).getEffectiveName() : ""),
    GUILD("guild", (e) -> e.GUILD != null ? e.GUILD.getName() : ""),
    GUILD_ID("guild-id", (e) -> e.GUILD != null ? e.GUILD.getName() : ""),
    GUILD_USERS("guild-users", (e) -> e.GUILD != null ? Integer.toString(e.GUILD.getMembers().size()) : ""),
    CHANNEL("channel", e -> e.CHANNEL != null ? e.CHANNEL.getName() : ""),
    CHANNEL_ID("channel-id", e -> e.CHANNEL != null ? e.CHANNEL.getId() : ""),
    CHANNEL_MENTION("channel-mention", e -> e.CHANNEL != null ? e.CHANNEL.getAsMention() : ""),;

    private final String pattern;
    private final TemplateParser parser;

    TemplateArgument(String pattern, TemplateParser parser) {
        this.pattern = Config.TEMPLATE_QUOTE + pattern + Config.TEMPLATE_QUOTE;
        this.parser = parser;
    }

    public String getPattern() {
        return pattern;
    }

    public String parse(TemplateVariables vars) {
        return parser.apply(vars);
    }
}
