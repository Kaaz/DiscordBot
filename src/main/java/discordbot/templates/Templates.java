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

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * All public static Template variables are mapped to the database
 * naming goes as follows:
 * classname_variable_name -> to lower case
 * <p>
 * usage/examples in commands/etc:
 * Templates.TEST.compile(User, Guild)
 * Templates.PERMISSION_MISSING.compile("some permission")
 * Templates.command.SAY_CONTAINS_MENTION.compile()
 */
public class Templates {
    final private static HashMap<String, Template> dictionary = new HashMap<>();

    public static Template PERMISSION_MISSING = new Template(TemplateArgument.ARG);
    public static Template TEST = new Template(
            new TemplateArgument[]{TemplateArgument.USER, TemplateArgument.USER_DESCRIMINATOR, TemplateArgument.GUILD},
            new TemplateArgument[]{TemplateArgument.ARG, TemplateArgument.ARGS});
    public static Template WELCOME_NEW_USER = new Template(null, TemplateArgument.values());
    public static Template WELCOME_BACK_USER = new Template(null, TemplateArgument.values());
    public static Template MESSAGE_USER_LEAVES = new Template(null, TemplateArgument.values());

    public static class command {
        public static Template SAY_CONTAINS_MENTION = new Template();
        public static Template SAY_WHATEXACTLY = new Template();
    }

    public static class music {

    }

    public static class misc {

    }

    public static boolean isValidTemplate(String key) {
        return dictionary.containsKey(key);
    }

    public static void init() {
        loadCategory("", Templates.class);
        TemplateCache.initialize();
    }

    public static void loadCategory(String prefix, Class<?> clazz) {
        String pre = prefix.isEmpty() ? "" : prefix + "_";
        for (Class<?> sub : clazz.getClasses()) {
            loadCategory((pre + sub.getSimpleName()).toLowerCase(), sub);
        }
        for (Field field : clazz.getFields()) {
            if (field.getType().equals(Template.class)) {
                String key = (pre + field.getName()).toLowerCase();
                try {
                    Template tmp = (Template) field.get(null);
                    tmp.setKey(key);
                    dictionary.put(key, tmp);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
