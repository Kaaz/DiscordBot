package discordbot.command;

public class PaginationInfo {

	private int currentPage = 0;
	private int maxPage = 1;

	public PaginationInfo(int currentPage, int maxPage) {

		this.currentPage = currentPage;
		this.maxPage = maxPage;
	}

	public boolean previousPage() {
		if (currentPage > 1) {
			currentPage--;
			return true;
		}
		return false;
	}

	public boolean nextPage() {
		if (currentPage < maxPage) {
			currentPage++;
			return true;
		}
		return false;
	}

	public int getMaxPage() {
		return maxPage;
	}

	public void setMaxPage(int maxPage) {
		this.maxPage = maxPage;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}
}
