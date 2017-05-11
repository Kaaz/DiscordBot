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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * All public static Template variables are mapped to the database
 * naming goes as follows:
 * classname_variable_name -> to lower case
 * <p>
 * usage/examples in commands/etc:
 * Templates.TEST.format(User, Guild)
 * Templates.permission_missing.format("some permission")
 * Templates.command.SAY_CONTAINS_MENTION.format()
 */
public final class Templates {
    final private static HashMap<String, Template> dictionary = new HashMap<>();

    public static final Template permission_missing = new Template(TemplateArgument.ARG);
    public static final Template no_permission = new Template();
    public static final Template TEST = new Template(
            new TemplateArgument[]{TemplateArgument.USER, TemplateArgument.USER_DESCRIMINATOR, TemplateArgument.GUILD},
            new TemplateArgument[]{TemplateArgument.ARG, TemplateArgument.ARGS});
    public static final Template welcome_new_user = new Template(null, TemplateArgument.values());
    public static final Template welcome_back_user = new Template(null, TemplateArgument.values());
    public static final Template message_user_leaves = new Template(null, TemplateArgument.values());
    public static final Template welcome_bot_admin = new Template(null, TemplateArgument.values());

    public static Template getByKey(String templateKey) {
        return dictionary.get(templateKey);
    }

    final public static class command {
        public static class uptime {
            public static final Template upfor = new Template(TemplateArgument.ARG);
        }

        public static class template {
            public static final Template added = new Template();
            public static final Template added_failed = new Template();
            public static final Template invalid_option = new Template();

            public static final Template delete_success = new Template();
            public static final Template delete_failed = new Template();
            public static final Template not_found = new Template(TemplateArgument.ARG);
        }

        public static final Template invalid_use = new Template();
        public static final Template SAY_CONTAINS_MENTION = new Template();
        public static final Template SAY_WHATEXACTLY = new Template();
    }

    final public static class music {

        public static final Template no_one_listens_i_leave = new Template();
        public static final Template queue_is_empty = new Template(TemplateArgument.GUILD);
    }

    final public static class error {
        public static final Template command_private_only = new Template();
        public static final Template command_public_only = new Template();
    }

    public static int uniquePhraseCount() {
        return dictionary.keySet().size();
    }

    public static List<String> getAllKeyphrases(int itemsPerPage, int offset) {
        List<String> list = new ArrayList<>(dictionary.keySet());
        Collections.sort(list);
        return list.subList(offset, Math.min(list.size(), itemsPerPage + offset));
    }

    /**
     * returns a list of templates matching the filter
     *
     * @param contains keyphrase contains this string
     * @return list of filtered keyphrases
     */
    public static List<String> getAllKeyphrases(String contains) {
        List<String> matching = dictionary.keySet().stream().filter(s -> s.contains(contains)).collect(Collectors.toList());
        if (matching.size() > 25) {
            return matching.subList(0, 25);
        }
        return matching;
    }

    public static boolean templateExists(String key) {
        return dictionary.containsKey(key);
    }

    public static void init() {
        loadCategory("", Templates.class);
        TemplateCache.initialize();
    }

    private static void loadCategory(String prefix, Class<?> clazz) {
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
