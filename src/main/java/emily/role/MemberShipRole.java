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

package emily.role;

import java.awt.*;

/**
 * Created on 19-9-2016
 */
public class MemberShipRole {

    private final String name;
    private final Color color;
    private final long membershipTime;
    private final boolean hoisted;

    public MemberShipRole(String name, Color color, long membershipTime) {
        this.name = name;
        this.color = color;
        this.hoisted = false;
        this.membershipTime = membershipTime;
    }

    public MemberShipRole(String name, Color color, long membershipTime, boolean hoisted) {
        this.name = name;
        this.color = color;
        this.hoisted = hoisted;
        this.membershipTime = membershipTime;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public long getMembershipTime() {
        return membershipTime;
    }

    public boolean isHoisted() {
        return hoisted;
    }
}
