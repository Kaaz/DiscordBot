package novaz.util.obj;

public class SCFile {
	public String artist = "";
	public String title = "";
	public String filename = "";
	public String id = "";

	@Override
	public String toString() {
		return "SCFile{" +
				"artist='" + artist + '\'' +
				", title='" + title + '\'' +
				", filename='" + filename + '\'' +
				'}';
	}
}
