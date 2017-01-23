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

package discordbot.modules.profile;

import discordbot.main.Launcher;
import net.dv8tion.jda.core.entities.User;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created on 28-9-2016
 */
public abstract class ProfileImage {

	private final User user;

	public ProfileImage(User user) {
		this.user = user;
	}

	public BufferedImage getUserAvatar() throws IOException {

		URLConnection connection = new URL(getUser().getAvatarUrl() != null ? getUser().getAvatarUrl() : getUser().getDefaultAvatarUrl()).openConnection();
		connection.setRequestProperty("User-Agent", "bot emily-bot");
		BufferedImage profileImg;
		try {
			profileImg = ImageIO.read(connection.getInputStream());
		} catch (Exception ignored) {
			profileImg = ImageIO.read(Launcher.class.getClassLoader().getResource("default_profile.jpg"));
		}
		return profileImg;
	}

	public User getUser() {
		return user;
	}
}
