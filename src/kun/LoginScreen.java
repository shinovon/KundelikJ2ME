package kun;

public class LoginScreen extends ListScreen implements ButtonHandler {

	private TextInput usernameField;
	private TextInput passwordField;
	private Button loginBtn;
	private boolean loggingIn;

	LoginScreen() {
		super("Вход", null);
		add(usernameField = new TextInput("Юзернейм"));
		add(passwordField = new TextInput("Пароль"));
		add(loginBtn = new Button("Вход", this));
	}

	public void handleButton(Button button) {
		if(loggingIn || button != loginBtn) return;
		loggingIn = true;
		try {
			Kun.auth(usernameField.getText(), passwordField.getText());
			ui.loggedIn();
		} catch (Exception e) {
			e.printStackTrace();
		}
		loggingIn = false;
	}

}
