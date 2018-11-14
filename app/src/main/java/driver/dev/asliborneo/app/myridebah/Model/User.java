package driver.dev.asliborneo.app.myridebah.Model;

public class User {
    private String name;
    private String email;

    public User(String name, String email, String password, String phone, String rates, String avatarurl) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.rates = rates;
        this.avatarurl = avatarurl;
    }

    private String password;
    private String phone;
    private String rates;

    public String getRates() {
        return rates;
    }

    public void setRates(String rates) {
        this.rates = rates;
    }

    public String getAvatarurl() {
        return avatarurl;
    }

    public void setAvatarurl(String avatarurl) {
        this.avatarurl = avatarurl;
    }

    private String avatarurl;


    public User(){

    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
