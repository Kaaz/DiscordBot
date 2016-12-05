package discordbot.guildsettings.defaults;

import discordbot.guildsettings.AbstractGuildSetting;

/**
 * Made by nija123098 on 12/4/2016
 */
public class SettingAdminVolume extends AbstractGuildSetting {
    @Override
    public String getKey() {
        return "admin_volume";
    }

    @Override
    public String getDefault() {
        return "false";
    }

    @Override
    public String[] getDescription() {
        return new String[]{"Require a guild admin to change the volume",
                "false -> allow all users to change the bot's volume",
                "true -> only allow guild admins to change the bot's volume"};
    }

    @Override
    public boolean isValidValue(String input) {
        try {
            Boolean.parseBoolean(input);
            return true;
        }catch (Exception ignored){
            return false;
        }
    }
}
