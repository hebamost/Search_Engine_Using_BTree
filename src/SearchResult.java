
public class SearchResult implements ISearchResult {
	private String id;
	private int rank;
	
	@Override
	public String getId() {
		return id;
	}
	
	@Override
	public void setId(String id) {
		this.id = id;
	}
	
	@Override
	public int getRank() {
		return rank;
	}
	
	@Override
	public void setRank(int rank) {
		this.rank = rank;
	}
}
