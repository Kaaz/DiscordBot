package discordbot.handler.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class QueuedAudioTrack {

	final private String userId;
	final private AudioTrack track;

	public QueuedAudioTrack(String userId, AudioTrack track) {
		this.userId = userId;
		this.track = track;
	}

	public String getUserId() {
		return userId;
	}

	public AudioTrack getTrack() {
		return track;
	}
}
