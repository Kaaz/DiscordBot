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
 * Templates.command.SAY_CONTAINS_MENTION.formatGuild(channel)
 */
public final class Templates {
    final private static HashMap<String, Template> dictionary = new HashMap<>();

    public static Template getByKey(String templateKey) {
        return dictionary.get(templateKey.toLowerCase());
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
        List<String> matching = dictionary.keySet().stream().filter(s -> s.contains(contains.toLowerCase())).collect(Collectors.toList());
        if (matching.size() > 25) {
            return matching.subList(0, 25);
        }
        return matching;
    }

    public static boolean templateExists(String key) {
        return dictionary.containsKey(key.toLowerCase());
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

    public static final Template unknown_command = new Template(TemplateArgument.ARG);
    public static final Template bot_self_update_restart = new Template(TemplateArgument.ARG, TemplateArgument.ARG2);
    public static final Template announce_reboot = new Template();
    public static final Template bot_reboot_more_shards = new Template();
    public static final Template private_message_sent = new Template();
    public static final Template playmode_game_corrupt = new Template();
    public static final Template playmode_waiting_for_player = new Template();
    public static final Template playmode_not_your_turn = new Template();
    public static final Template playmode_not_a_valid_move = new Template();
    public static final Template playmode_cant_create_instance = new Template();
    public static final Template playmode_cant_register_instance = new Template();
    public static final Template playmode_created_waiting_for_player = new Template();
    public static final Template playmode_invalid_gamecode = new Template();
    public static final Template playmode_invalid_usage = new Template();
    public static final Template playmode_already_in_game = new Template();
    public static final Template playmode_not_vs_bots = new Template();
    public static final Template playmode_joined_target = new Template();
    public static final Template playmode_target_already_in_a_game = new Template();
    public static final Template playmode_not_vs_self = new Template();
    public static final Template playmode_canceled_game = new Template();
    public static final Template playmode_not_in_game = new Template();
    public static final Template playmode_entering_mode = new Template();
    public static final Template playmode_leaving_mode = new Template();
    public static final Template playmode_in_mode_warning = new Template();
    public static final Template gamble_ai_lose = new Template();
    public static final Template ud_no_results = new Template(TemplateArgument.ARG);
    public static final Template gamble_insufficient_funds = new Template(TemplateArgument.ARG, TemplateArgument.ARG2);
    public static final Template bank_transfer_minimum = new Template(TemplateArgument.ARG, TemplateArgument.ARG2);
    public static final Template bank_transfer_success = new Template(TemplateArgument.ARG, TemplateArgument.ARG2, TemplateArgument.ARG3);
    public static final Template bank_transfer_failed = new Template();
    public static final Template bank_insufficient_funds = new Template(TemplateArgument.ARG);
    public static final Template permission_missing = new Template(TemplateArgument.ARG);
    public static final Template no_permission = new Template();
    public static final Template invalid_use = new Template();
    public static final Template not_implemented_yet = new Template();
    public static final Template TEST = new Template(
            new TemplateArgument[]{TemplateArgument.USER, TemplateArgument.USER_DESCRIMINATOR, TemplateArgument.GUILD},
            new TemplateArgument[]{TemplateArgument.ARG, TemplateArgument.ARGS});
    public static final Template welcome_new_user = new Template(null, TemplateArgument.values());
    public static final Template welcome_back_user = new Template(null, TemplateArgument.values());
    public static final Template message_user_leaves = new Template(null, TemplateArgument.values());
    public static final Template welcome_bot_admin = new Template(null, TemplateArgument.values());

    final public static class todo {
        public static final Template not_your_item = new Template();
        public static final Template item_updated = new Template();
        public static final Template item_removed = new Template();
        public static final Template your_list_not_found = new Template();
        public static final Template item_add_success = new Template();
        public static final Template list_updated = new Template();
        public static final Template list_cleared = new Template();
        public static final Template user_list_not_found = new Template(TemplateArgument.USER);

    }

    final public static class command {
        public static final Template volume_changed = new Template(TemplateArgument.ARG);
        public static final Template skip_vote_success = new Template(TemplateArgument.ARG, TemplateArgument.ARG2);
        public static final Template is_blacklisted = new Template(TemplateArgument.ARG);
        public static final Template on_cooldown = new Template(TemplateArgument.ARG);
        public static final Template skip_song_skipped = new Template();
        public static final Template not_for_private = new Template();
        public static final Template not_for_public = new Template();
        public static final Template modaction_empty = new Template(TemplateArgument.ARG);
        public static final Template modaction_not_self = new Template(TemplateArgument.ARG);
        public static final Template modaction_failed = new Template(TemplateArgument.ARG, TemplateArgument.USER);
        public static final Template modaction_success = new Template(TemplateArgument.ARG, TemplateArgument.USER);
        public static final Template stop_after_track = new Template();
        public static final Template volume_invalid_parameters = new Template();
        public static final Template skip_vote_failed = new Template();
        public static final Template stop_success = new Template();
        public static final Template user_joindate_set = new Template(TemplateArgument.USER, TemplateArgument.ARG);
        public static final Template playlist_title = new Template(TemplateArgument.ARG);
        public static final Template prefix_is = new Template(TemplateArgument.ARG);
        public static final Template prefix_saved = new Template(TemplateArgument.ARG);
        public static final Template prefix_invalid = new Template(TemplateArgument.ARG);
        public static final Template skip_permanent_success = new Template();
        public static final Template play_no_results = new Template();
        public static final Template current_banned_success = new Template();
        public static final Template rotate_too_short = new Template();
        public static final Template report_no_seperator = new Template();
        public static final Template report_success = new Template();
        public static final Template report_message_too_short = new Template();
        public static final Template reddit_sub_not_found = new Template();
        public static final Template reddit_nothing = new Template();
        public static final Template poll_question_too_short = new Template();
        public static final Template invalid_use = new Template();
        public static final Template meme_invalid_type = new Template();
        public static final Template joke_wait = new Template();
        public static final Template fml_not_today = new Template();
        public static final Template joke_not_today = new Template();
        public static final Template gif_not_today = new Template();
        public static final Template catfact_not_today = new Template();
        public static final Template pm_cant_find_user = new Template();
        public static final Template pm_success = new Template();
        public static final Template SAY_CONTAINS_MENTION = new Template();
        public static final Template SAY_WHATEXACTLY = new Template();
        public static final Template case_not_found = new Template(TemplateArgument.ARG);
        public static final Template emojify_max_exceeded = new Template(TemplateArgument.ARG);
        public static final Template case_reason_modified = new Template();

        final public static class currentlyplaying {
            public static final Template nosong = new Template();

        }

        final public static class join {
            public static final Template cantfindyou = new Template();
            public static final Template already_there = new Template();
            public static final Template joinedyou = new Template();

        }

        final public static class help {
            public static final Template donno = new Template();
            public static final Template send_private = new Template();

        }

        final public static class getrole {
            public static final Template empty = new Template();
            public static final Template not_removed = new Template();
            public static final Template not_assignable = new Template();
            public static final Template removed = new Template(TemplateArgument.ROLE);
            public static final Template assigned = new Template(TemplateArgument.ROLE);
            public static final Template not_assigned = new Template(TemplateArgument.ROLE);

        }

        final public static class tag {
            public static final Template no_tags = new Template();
            public static final Template saved = new Template();
            public static final Template only_creator_can_edit = new Template();
            public static final Template no_mention = new Template();
            public static final Template nothing_to_delete = new Template();
            public static final Template delete_success = new Template();
            public static final Template only_delete_own = new Template();
            public static final Template not_set = new Template();
            public static final Template by_user_deleted = new Template(TemplateArgument.USER);

        }

        final public static class roll {
            public static final Template dice_count = new Template(TemplateArgument.ARG);
            public static final Template side_count = new Template(TemplateArgument.ARG);
            public static final Template no_dice = new Template();

        }

        final public static class bet {
            public static final Template no_bets = new Template();
            public static final Template option_not_found = new Template();
            public static final Template edit_prepare_only = new Template();
            public static final Template create_success = new Template();
            public static final Template already_preparing = new Template();
            public static final Template amount_between = new Template(TemplateArgument.ARGS, TemplateArgument.ARG2);

        }

        final public static class userrank {
            public static final Template no_rank = new Template(TemplateArgument.ARG);
            public static final Template not_exists = new Template(TemplateArgument.ARG);
            public static final Template rank = new Template(TemplateArgument.ARG, TemplateArgument.USER);

        }

        final public static class cla {
            public static final Template version_not_found = new Template(TemplateArgument.ARG);
            public static final Template type_unknown = new Template(TemplateArgument.ARG);
            public static final Template desc_short = new Template();

        }

        final public static class reload {
            public static final Template success = new Template();

        }

        final public static class reboot {
            public static final Template update = new Template();
            public static final Template success = new Template();
            public static final Template forceupdate = new Template();
            public static final Template shard = new Template(TemplateArgument.ARG);
            public static final Template shard_success = new Template(TemplateArgument.ARG);
            public static final Template shard_failed = new Template(TemplateArgument.ARG);

        }

        final public static class subscribe {
            public static final Template channel_has_no_subscriptions = new Template();
            public static final Template invalid_service = new Template();
            public static final Template success = new Template();
            public static final Template already_subscribed = new Template();
            public static final Template not_subscribed = new Template();
            public static final Template unsubscribed_success = new Template(TemplateArgument.ARG);

        }

        final public static class role_admin {
            public static final Template adding = new Template(TemplateArgument.ARG);
            public static final Template removing = new Template(TemplateArgument.ARG);

        }

        final public static class purge {
            public static final Template success = new Template(TemplateArgument.ARG);

        }

        final public static class stats {
            public static final Template not_playing_music = new Template();
            public static final Template playing_music_on = new Template(TemplateArgument.ARG);

        }

        final public static class config {
            public static final Template key_not_exists = new Template();
            public static final Template key_read_only = new Template();
            public static final Template key_modified = new Template();

        }

        final public static class blacklist {
            public static final Template command_empty = new Template();
            public static final Template reset_channel = new Template(TemplateArgument.ARG);
            public static final Template reset_all_channels = new Template();
            public static final Template reset = new Template();
            public static final Template command_not_found = new Template(TemplateArgument.ARG);
            public static final Template not_blacklistable = new Template(TemplateArgument.ARG);
            public static final Template command_disabled = new Template(TemplateArgument.ARG);
            public static final Template command_enabled = new Template(TemplateArgument.ARG);
        }

        final public static class autoreply {
            public static final Template tag_length = new Template(TemplateArgument.ARG);
            public static final Template created = new Template(TemplateArgument.ARG);
            public static final Template already_exists = new Template(TemplateArgument.ARG);
            public static final Template not_exists = new Template(TemplateArgument.ARG);
            public static final Template deleted = new Template(TemplateArgument.ARG);
            public static final Template regex_invalid = new Template();
            public static final Template regex_saved = new Template();
            public static final Template guild_invalid = new Template(TemplateArgument.ARG);
            public static final Template guild_saved = new Template(TemplateArgument.ARG);
            public static final Template response_saved = new Template();
            public static final Template tag_saved = new Template();
            public static final Template cooldown_saved = new Template();
            public static final Template no_match = new Template();
        }

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
    }

    public static class config {
        public static final Template modlog_not_found = new Template();
        public static final Template cant_find_logchannel = new Template(TemplateArgument.ARG);
        public static final Template cant_talk_in_channel = new Template(TemplateArgument.ARG);
        public static final Template cant_find_guild = new Template(TemplateArgument.ARG);
        public static final Template cant_find_user = new Template(TemplateArgument.ARG);
        public static final Template reset_success = new Template();
        public static final Template reset_warning = new Template();
    }

    final public static class playlist {
        public static final Template global_readonly = new Template();
        public static final Template setting_not_numeric = new Template(TemplateArgument.ARG);
        public static final Template setting_updated = new Template(TemplateArgument.ARG, TemplateArgument.ARG2);
        public static final Template setting_invalid = new Template(TemplateArgument.ARG, TemplateArgument.ARG2);
        public static final Template title_updated = new Template(TemplateArgument.ARG);
        public static final Template music_already_added = new Template(TemplateArgument.ARG, TemplateArgument.ARG2);
        public static final Template music_added = new Template(TemplateArgument.ARG, TemplateArgument.ARG2);
        public static final Template music_removed = new Template(TemplateArgument.ARG, TemplateArgument.ARG2);
        public static final Template music_removed_all = new Template(TemplateArgument.ARG);

    }

    final public static class reaction {
        public static final Template playlist_item_added_private = new Template(TemplateArgument.ARG, TemplateArgument.ARG2);
        public static final Template playlist_item_removed_private = new Template(TemplateArgument.ARG, TemplateArgument.ARG2);

    }

    final public static class music {

        public static final Template skip_mode = new Template(TemplateArgument.ARG);
        public static final Template playlist_changed = new Template(TemplateArgument.ARG);
        public static final Template playlist_using = new Template(TemplateArgument.ARG);
        public static final Template failed_playlist_empty = new Template(TemplateArgument.ARG);
        public static final Template not_added_to_queue = new Template(TemplateArgument.ARG);
        public static final Template added_to_queue = new Template(TemplateArgument.ARG);
        public static final Template streaming_from_url = new Template();
        public static final Template skip_admin_only = new Template();
        public static final Template not_while_admin_listening = new Template();
        public static final Template state_resumed = new Template();
        public static final Template failed_to_start = new Template();
        public static final Template started_playing_random = new Template();
        public static final Template no_valid_youtube_key = new Template();
        public static final Template no_users_in_channel = new Template();
        public static final Template file_error = new Template();
        public static final Template state_paused = new Template();
        public static final Template not_same_voicechannel = new Template();
        public static final Template state_not_started = new Template();
        public static final Template channel_autotitle_stop = new Template();
        public static final Template channel_autotitle_start = new Template();
        public static final Template queue_cleared = new Template();
        public static final Template clear_mode = new Template(TemplateArgument.ARG);
        public static final Template repeat_mode = new Template();
        public static final Template repeat_mode_stopped = new Template();
        public static final Template source_location = new Template(TemplateArgument.ARG);
        public static final Template not_voted = new Template(TemplateArgument.ARG);
        public static final Template your_vote = new Template(TemplateArgument.ARG, TemplateArgument.ARG2);
        public static final Template join_no_permission = new Template(TemplateArgument.ARG);
        public static final Template join_channel_full = new Template(TemplateArgument.ARG);
        public static final Template required_role_not_found = new Template(TemplateArgument.ROLE);
        public static final Template no_one_listens_i_leave = new Template();
        public static final Template queue_is_empty = new Template(TemplateArgument.GUILD);
    }

    final public static class error {
        public static final Template command_private_only = new Template();
        public static final Template command_public_only = new Template();
    }
}
