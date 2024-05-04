package com.password.manager.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.password.manager.model.Address;
import com.password.manager.model.Name;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class UserCreation {

    @JsonProperty("sUserName")
    private String userName;

    @JsonProperty("sEmailId")
    private String emailId;

    @JsonProperty("sPassword")
    private String password;

    @JsonProperty("oApplicantName")
    private Name applicantName;

    @JsonProperty("sDateOfBirth")
    private String dateOfBirth;

    @JsonProperty("sGender")
    private String gender;

    @JsonProperty("sPhoneNumber")
    private String phoneNumber;

    @JsonProperty("aAddress")
    private List<Address> address;

    @JsonProperty("bAccountActive")
    private boolean accountActive;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Name getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(Name applicantName) {
        this.applicantName = applicantName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<Address> getAddress() {
        return address;
    }

    public void setAddress(List<Address> address) {
        this.address = address;
    }

    public boolean isAccountActive() {
        return accountActive;
    }

    public void setAccountActive(boolean accountActive) {
        this.accountActive = accountActive;
    }
}
