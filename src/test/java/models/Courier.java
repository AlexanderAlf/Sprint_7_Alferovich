package models;

public class Courier {
    private final String login;
    private final String password;
    private final String firstName;

    public Courier(String login, String password, String firstName) {
        this.login = login;
        this.password = password;
        this.firstName = firstName;
    }

    public Courier(String login, String password) {
        this.login = login;
        this.password = password;
        this.firstName = null;
    }

    public String getLogin() { return login; }
    public String getPassword() { return password; }
    public String getFirstName() { return firstName; }
}