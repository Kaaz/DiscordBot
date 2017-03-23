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

package emily.modules.gif;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OverlayData {
    private final Map<Integer, List<OverlayEvent>> events;
    private final String filename;
    private final int requiredActors;

    public OverlayData(String filename, int requiredActors) {
        this.filename = filename;
        this.requiredActors = requiredActors;
        this.events = new HashMap<>();
    }
}
