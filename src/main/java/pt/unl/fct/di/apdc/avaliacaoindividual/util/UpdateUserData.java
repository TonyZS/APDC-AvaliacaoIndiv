package pt.unl.fct.di.apdc.avaliacaoindividual.util;

public class UpdateUserData {

	public String username;
	public String currentPassword;
	public String password;
	public String password2;
	public String email;
	public String perfil;
	public String telfFixo;
	public String telfMovel;
	public String morada;
	public String moradaComp;
	public String localidade;
	public String cp;
	public String tokenID;

	public UpdateUserData() {

	}

	public UpdateUserData(String username, String currentPassword, String password, String password2, String email,
			String perfil, String telfFixo, String telfMovel, String morada, String moradaComp, String localidade,
			String cp, String tokenID) {
		this.username = username;
		this.currentPassword = currentPassword;
		this.password = password;
		this.password2 = password;
		this.email = email;
		this.perfil = perfil;
		this.telfFixo = telfFixo;
		this.telfMovel = telfMovel;
		this.morada = morada;
		this.moradaComp = moradaComp;
		this.localidade = localidade;
		this.cp = cp;
		this.tokenID = tokenID;
	}

	public boolean validPassword() {
		return password.length() >= 10 || password.matches(".*\\d.*") || !password.contains(" ");
	}

	public boolean secondPassword() {
		return password.equals(password2);
	}

	public boolean validEmail() {
		return email.contains("@");
	}

	public boolean validPerfil() {
		return perfil.equals("Public") || perfil.equals("Private");
	}

	public boolean validFixo() {
		return telfFixo.length() == 9 && telfFixo.startsWith("2");
	}

	public boolean validMovel() {
		return telfMovel.length() == 9 && telfFixo.startsWith("9");
	}

	public boolean validCp() {
		return cp.length() == 7 && cp.charAt(4) == '-';
	}

}
