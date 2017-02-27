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

public class Template {
    private String key;
    final private TemplateArgument[] arguments;

    public Template(TemplateArgument... args) {
        arguments = args;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public TemplateArgument[] getArguments() {
        return arguments;
    }

    public boolean isValidTemplate(String template) {
        if (template == null || template.isEmpty()) {
            return false;
        }
        if (arguments == null || arguments.length == 0) {
            return true;
        }
        for (TemplateArgument argument : arguments) {
            if (template.contains(argument.getPattern())) {
                return false;
            }
        }
        return true;
    }

    public String compile(Object... vars) {
        if (arguments == null || arguments.length == 0) {
            return TemplateCache.getGlobal(getKey());
        }
        String tmp = TemplateCache.getGlobal(getKey());
        TemplateVariables env = TemplateVariables.create(vars);
        for (TemplateArgument arg : arguments) {
            tmp = tmp.replace(arg.getPattern(), arg.parse(env));
        }
        return tmp;
    }
}
