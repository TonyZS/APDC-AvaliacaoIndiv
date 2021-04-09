	package pt.unl.fct.di.apdc.avaliacaoindividual.util;

public class NewUserData {

	public String username;
	public String password;
	public String password2;
	public String email;

	public NewUserData() {

	}

	public NewUserData(String username, String password, String password2, String email) {
		this.username = username;
		this.password = password;
		this.password2 = password2;
		this.email = email;
	}

	public boolean validRegistration() {
		return(!username.equals(null)&&!password.equals(null)&&!email.equals(null));
	}
	
	public boolean validPassword() {
		return password.length()>=10||password.matches(".*\\d.*")||!password.contains(" ");
	}
	
	public boolean validEmail() {
		return email.contains("@");
	}
	
	public boolean secondPassword() {
		return password.equals(password2);
	}

	public boolean validUsername() {
		return !username.contains(" ");
	}

}
