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

package discordbot.core;

/**
 * bot exit codes
 * Created on 22-9-2016
 */
public enum ExitCode {
    REBOOT(100),
    STOP(101),
    NEED_MORE_SHARDS(102),
    UPDATE(200),
    GENERIC_ERROR(300),
    SHITTY_CONFIG(301),
    DISCONNECTED(302),
    UNKNOWN(-1);

    private final int code;

    ExitCode(int code) {

        this.code = code;
    }

    public static ExitCode fromCode(int exitCode) {
        for (ExitCode code : ExitCode.values()) {
            if (code.getCode() == exitCode) {
                return code;
            }
        }
        return ExitCode.UNKNOWN;
    }

    public int getCode() {
        return code;
    }
}
