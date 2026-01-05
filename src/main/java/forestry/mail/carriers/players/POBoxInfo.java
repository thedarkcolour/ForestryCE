package forestry.mail.carriers.players;

public record POBoxInfo(int playerLetters, int tradeLetters) {
	public boolean hasMail() {
		return this.playerLetters > 0 || this.tradeLetters > 0;
	}
}
