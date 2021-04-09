package pt.unl.fct.di.apdc.avaliacaoindividual.util;

import com.google.cloud.datastore.Entity;

public class UserData {

	public String username;
	public String email;
	public String telfFixo;
	public String telfMovel;
	public String morada;
	public String moradaC;
	public String localidade;
	public String cp;
	public String profile;
	public String state;
	public String role;

	public UserData() {

	}

	public UserData(Entity user) {
		this.username = user.getString("user_name");
		this.email = user.getString("user_email");
		this.telfFixo = user.getString("user_fixo");
		this.telfMovel = user.getString("user_movel");
		this.morada = user.getString("user_morada");
		this.moradaC = user.getString("user_moradaComp");
		this.localidade = user.getString("user_localidade");
		this.cp = user.getString("user_cp");
		this.profile = user.getString("user_profile");
		this.state = user.getString("user_state");
		this.role = user.getString("user_role");

	}

}
