package kkkb1114.sampleproject.infectionapp.database.MyProfile;

public class MyProfile {
    public String name;
    public int gender;
    public String birthDate;
    public String weight;
    public String purpose;
    public String infection;

    public MyProfile(String name, int gender, String birthDate, String weight, String purpose, String infection){
        this.name = name;
        this.gender = gender;
        this.birthDate = birthDate;
        this.weight = weight;
        this.purpose = purpose;
        this.infection = infection;
    }

    @Override
    public String toString() {
        return "MyProfile{" +
                "name='" + name + '\'' +
                ", gender=" + gender +
                ", birthDate='" + birthDate + '\'' +
                ", weight='" + weight + '\'' +
                ", purpose='" + purpose + '\'' +
                ", infection='" + infection + '\'' +
                '}';
    }
}
