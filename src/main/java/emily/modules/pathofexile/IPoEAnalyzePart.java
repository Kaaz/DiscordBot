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

package emily.modules.pathofexile;

import emily.modules.pathofexile.obj.PoEItem;

/**
 * Path of exile
 */
public interface IPoEAnalyzePart {

    /**
     * wheneter or not the paragraph is analyzeable by the class
     *
     * @return yes or no
     */
    boolean canAnalyze(String text);

    /**
     * @param item the PoEitem to fill the data and return
     * @param text the text to analyze
     * @return the item filled with the extra data the analyzer can
     */
    PoEItem analyze(PoEItem item, String text);
}
