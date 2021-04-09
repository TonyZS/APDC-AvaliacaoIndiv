package pt.unl.fct.di.apdc.avaliacaoindividual.util;

public class RoleUpdateData {

	public String username;
	public String newRole;

	public String tokenID;

	public RoleUpdateData() {

	}

	public RoleUpdateData(String username, String newRole, String tokenID) {
		this.username = username;
		this.newRole = newRole;

		this.tokenID = tokenID;
	}

	public boolean possibleUpdate(String role, String targetRole) {
		if (!newRole.equals("GBO") && !newRole.equals("GA") && !targetRole.equals("USER")) {
			return false;
		} else {
			if (newRole.equals("GBO") && (role.equals("GA") || role.equals("SU"))) {
				return true;
			} else {
				if (newRole.equals("GA") && role.equals("SU")) {
					return true;
				} else {
					return false;
				}
			}
		}
	}

}
