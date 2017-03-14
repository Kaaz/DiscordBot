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

package emily.command.administrative;

import emily.command.administrative.modactions.AbstractModActionCommand;
import emily.db.model.OModerationCase;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;

public class WarnCommand extends AbstractModActionCommand {
    @Override
    public String getDescription() {
        return "Give a user a warning";
    }

    @Override
    public String getCommand() {
        return "warn";
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    protected OModerationCase.PunishType getPunishType() {
        return OModerationCase.PunishType.WARN;
    }

    @Override
    protected Permission getRequiredPermission() {
        return Permission.KICK_MEMBERS;
    }

    @Override
    protected boolean punish(Guild guild, Member member) {
        return true;
    }
}