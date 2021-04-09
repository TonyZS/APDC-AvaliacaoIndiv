package pt.unl.fct.di.apdc.avaliacaoindividual.util;

public class StateUpdateData {

	public String username;
	public String newState;
	public String tokenID;

	public StateUpdateData() {

	}

	public StateUpdateData(String username, String newState, String tokenID) {
		this.username = username;
		this.newState = newState;
		this.tokenID = tokenID;
	}

	public boolean possibleUpdate(String role, String targetRole) {
		if (!newState.equals("ENABLED") && !newState.equals("DISABLED")) {
			return false;
		} else {
			if (targetRole.equals("USER") && !role.equals("USER")) {
				return true;
			} else {
				if (targetRole.equals("GBO") && (role.equals("GA") || role.equals("SU"))) {
					return true;
				} else {
					if (targetRole.equals("Ga") && role.equals("SU")) {
						return true;
					} else {
						return false;
					}
				}
			}
		}
	}

}
