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

package emily.templates;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import emily.main.BotContainer;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.RoleImpl;
import net.dv8tion.jda.core.entities.impl.TextChannelImpl;
import net.dv8tion.jda.core.entities.impl.UserImpl;

import java.util.HashMap;

public class TemplateVariables {
    public static final TemplateVariables EMPTY = new TemplateVariables();
    public User user = null;
    public TextChannel channel = null;
    public Guild guild = null;
    public Role role = null;
    public String args = null;
    public String arg = null;

    private static final HashMap<Class, TemplateVariableParser> mapper = new HashMap<>();

    static {
        init();
    }


    private static void init() {
        mapper.put(User.class, (var, object) -> var.user = (User) object);
        mapper.put(UserImpl.class, (var, object) -> var.user = (User) object);
        mapper.put(TextChannel.class, (var, object) -> var.channel = (TextChannel) object);
        mapper.put(TextChannelImpl.class, (var, object) -> var.channel = (TextChannel) object);
        mapper.put(Guild.class, (var, object) -> var.guild = (Guild) object);
        mapper.put(GuildImpl.class, (var, object) -> var.guild = (Guild) object);
        mapper.put(Role.class, (var, object) -> var.role = (Role) object);
        mapper.put(RoleImpl.class, (var, object) -> var.role = (Role) object);

        mapper.put(String.class, (var, object) -> {
            var.args = (String) object;
            var.arg = (String) object;
        });
        mapper.put(String[].class, (var, object) -> var.args = Joiner.on(" ").join((String[]) object));
    }

    public static TemplateVariables create(Object... vars) {
        if (vars == null || vars.length == 0) {
            return EMPTY;
        }
        TemplateVariables tmp = new TemplateVariables();
        for (Object var : vars) {
            if (var == null) {
                continue;
            }
            if (mapper.containsKey(var.getClass())) {
                mapper.get(var.getClass()).apply(tmp, var);
            } else {
                BotContainer.LOGGER.warn("[template] UNMAPPED TYPE: " + var.getClass().getSimpleName());
            }
        }
        return tmp;
    }

    private interface TemplateVariableParser {
        void apply(TemplateVariables var, Object o);
    }

}
