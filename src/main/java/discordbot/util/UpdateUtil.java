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

package discordbot.util;

import discordbot.main.Launcher;
import discordbot.main.ProgramVersion;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 22-9-2016
 */
public class UpdateUtil {
    private static final Pattern versionPattern = Pattern.compile("<version>([0-9]+\\.[0-9]+\\.[0-9]+)</version>");

    public static ProgramVersion getLatestVersion() {
        String request = HttpHelper.doRequest("https://raw.githubusercontent.com/Kaaz/DiscordBot/master/pom.xml");
        Matcher matcher = versionPattern.matcher(request);
        if (matcher.find()) {
            return ProgramVersion.fromString(matcher.group(1));
        }
        return Launcher.getVersion();
    }
}